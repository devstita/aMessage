package kr.devta.amessage;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainService extends Service {
    boolean threadFlag;

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Manager.print("MainService (Network Chat Reader - Real Time): " + dataSnapshot.getKey() + ", " + dataSnapshot.getValue().toString());
            String key = dataSnapshot.getKey();
            String friendPhone = key.split(Manager.SEPARATOR)[0];
//            long date = Long.valueOf(dataSnapshot.getValue().toString().split(Manager.DATE_SEPARATOR)[0]);
//            String message = dataSnapshot.getValue().toString().split(Manager.DATE_SEPARATOR)[1];
            long date = Long.valueOf(dataSnapshot.getValue().toString().substring(0, Manager.CIPHER_OF_DATE));
            String message = dataSnapshot.getValue().toString().substring(Manager.CIPHER_OF_DATE + Manager.DATE_SEPARATOR.length(),
                    dataSnapshot.getValue().toString().length());
            ChatInfo chatInfo = new ChatInfo(message, (-1 * date));

            ArrayList<FriendInfo> friendInfos = Manager.readChatList();
            FriendInfo friendInfo = null;
            for (FriendInfo curFriendInfo : friendInfos) if (curFriendInfo.getPhone().equals(friendPhone)) {
                friendInfo = new FriendInfo(curFriendInfo.getName(), curFriendInfo.getPhone());
                break;
            }

            if (friendInfo == null) friendInfo = new FriendInfo(friendPhone, friendPhone); // 친구가 아니면

            if (ChatActivity.status != null && ChatActivity.status.equals(ActivityStatus.RESUMED))
                if (ChatActivity.adapter.getFriendInfo().getPhone().equals(friendInfo.getPhone()))
                    ChatActivity.adapter.addItem(chatInfo).refresh();
            Manager.addChat(-1, friendInfo, chatInfo);
            FirebaseDatabase.getInstance().getReference().child("Chats").child(Manager.getMyPhone(MainService.this.getApplicationContext())).child(dataSnapshot.getKey()).removeValue();
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

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        threadFlag = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Manager.init(getApplicationContext());
        FirebaseDatabase.getInstance().getReference().child("Chats").child(Manager.getMyPhone(getApplicationContext())).addChildEventListener(childEventListener);
        Manager.print("MainService.onCreate() -> Database Init Successful");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager.print("Start MainService.Thread");
                while (threadFlag) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference().child("Users");

                    String phone = Manager.getMyPhone(getApplicationContext());
                    String date = String.valueOf(Manager.getCurrentTimeMills());

                    reference.child(phone).setValue(date);
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Manager.print("Stop MainService.Thread");
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        threadFlag = false;
        FirebaseDatabase.getInstance().getReference().child("Chats").child(Manager.getMyPhone(getApplicationContext())).removeEventListener(childEventListener);
        Manager.print("MainService.onDestroy() -> Database Destroy Successful");
    }
}
