package kr.devta.amessage;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainService extends Service {
    private DatabaseReference myDatabaseReference;
    private ChildEventListener myDatabaseReferenceEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String[] keys = dataSnapshot.getKey().split(Manager.SEPARATOR);
            String friendPhone = keys[0];
            String message = dataSnapshot.getValue().toString();
            long date = Long.valueOf(keys[1]);

            dataSnapshot.getRef().removeValue();

            FriendInfo friendInfo = null;
            ArrayList<FriendInfo> friendInfos = Manager.readChatList();
            for (FriendInfo curFriendInfo : friendInfos) {
                if (curFriendInfo.getPhone().equals(friendPhone)) {
                    friendInfo = curFriendInfo;
                    break;
                }
            }

            if (friendInfo == null) {
                String name = friendPhone;
                ArrayList<FriendInfo> contacts = Manager.getContacts(getApplicationContext());
                for (FriendInfo curFriendInfo : contacts) if (friendPhone.equals(curFriendInfo.getPhone())) {
                    name = curFriendInfo.getName();
                    break;
                }

                friendInfo = new FriendInfo(name, friendPhone);
                Manager.addChatList(friendInfo);
            }
            ChatInfo chatInfo = new ChatInfo(message, -date);

            boolean actived = false;
            if (ChatActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) {
                if (ChatActivity.adapter != null) {
                    if (ChatActivity.adapter.getFriendInfo().getPhone().equals(friendInfo.getPhone())) {
                        ChatActivity.adapter.addItem(chatInfo).refresh();
                        actived = true;
                    }
                }
            } else if (MainActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) {
                MainActivity.updateUI();
            }
            Manager.addChat(-1, friendInfo, chatInfo, actived);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private DatabaseReference versionDatabaseReference;
    private ValueEventListener versionDatabaseReferenceEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (!Manager.checkUpdate(Integer.valueOf(dataSnapshot.getValue().toString()))) {
                stopForeground(true);
                stopSelf();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO_NONE: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Manager.init(getApplicationContext());
        Manager.print("MainService.onCreate() -> Database Init Successful");

        myDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats").child(Manager.getMyPhone());
        versionDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Management/VersionCode");

        myDatabaseReference.addChildEventListener(myDatabaseReferenceEventListener);
        versionDatabaseReference.addValueEventListener(versionDatabaseReferenceEventListener);

        /////////////////////////////////////////////
        /// ||| Make impossible to task kill ||| ///
        /////////////////////////////////////////////
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), Manager.NOTIFICATION_CHANNEL_ID);
            builder = Manager.makeNotificationChannel(builder);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        startForeground(startId, builder.build());

        /////////////////////////////////////////////
        ////////////// ||| Run ||| //////////////////
        /////////////////////////////////////////////

        new Thread(() -> {
            try {
                IO.Options socketOptions = new IO.Options();
                socketOptions.forceNew = true;
                socketOptions.reconnection = true;

                Socket socket = IO.socket("https://a-message.herokuapp.com", socketOptions);
                socket.connect();

                socket.on(Socket.EVENT_CONNECT, args -> {
                    Manager.print("Connected to Socket.IO Server");
                    socket.emit("phone", Manager.getMyPhone());
                }).on(Socket.EVENT_RECONNECT, args -> {
                    Manager.print("Re-Connected to Socket.IO Server");
                    socket.emit("phone", Manager.getMyPhone());
                }).on(Socket.EVENT_DISCONNECT, args -> Manager.print("Disconnected to Socket.IO Server"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Manager.print("Error from Socket.IO");
            }
        }).start();

//        return super.onStartCommand(intent, flags, startId); // < = > return 1
        return START_STICKY;
//        START_STICKY ( = 1): 재시작 (intent = null)
//        START_NOT_STICKY ( = 2): 재시작 안함
//        START_REDELIVER_INTENT ( = 3): 재시작 (intent = 유지)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myDatabaseReference.removeEventListener(myDatabaseReferenceEventListener);
        versionDatabaseReference.removeEventListener(versionDatabaseReferenceEventListener);
        Manager.print("MainService.onDestroy() -> Database Destroy Successful");
    }
}
