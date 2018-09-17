package kr.devta.amessage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainService extends Service {
    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//            Manager.print("MainService (Network Chat Reader - Real Time): " + dataSnapshot.getKey() + ", " + dataSnapshot.getValue().toString());
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

            if (friendInfo == null) { // 친구가 아니면
                String name = friendPhone;
                ArrayList<FriendInfo> contacts = Manager.getContacts(getApplicationContext());
                for (FriendInfo curFriendInfo : contacts) if (friendPhone.equals(curFriendInfo.getPhone())) {
                    name = curFriendInfo.getName();
                    break;
                }

                friendInfo = new FriendInfo(name, friendPhone);
                Manager.addChatList(friendInfo);
            }

            boolean actived = false;
            if (ChatActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) {
                if (ChatActivity.adapter.getFriendInfo().getPhone().equals(friendInfo.getPhone())) {
                    ChatActivity.adapter.addItem(chatInfo).refresh();
                    actived = true;
                }
            } else if (MainActivity.getActivityStatus().equals(ActivityStatus.RESUMED)) {
                MainActivity.updateUI();
            }
            Manager.addChat(-1, friendInfo, chatInfo, actived);
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
        Manager.mainServiceUpdateTimeThreadFlag = true;
        FirebaseDatabase.getInstance().getReference().child("Chats").child(Manager.getMyPhone(getApplicationContext())).addChildEventListener(childEventListener);
        Manager.print("MainService.onCreate() -> Database Init Successful");

        /////////////////////////////////////////////
        /// ||| Make impossible to task kill ||| ///
        /////////////////////////////////////////////
        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Manager.getSharedPreferences(Manager.NAME_NOTIFICATION_CHANNEL).getBoolean(Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_ID, false)) {
                notificationManager.createNotificationChannel(new NotificationChannel(Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_ID, Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));
                Manager.getSharedPreferences(Manager.NAME_NOTIFICATION_CHANNEL).edit().putBoolean(Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_ID, true).apply();
            }
            notificationBuilder.setChannelId(Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_ID);
        }

        startForeground(startId, notificationBuilder.build());

        /////////////////////////////////////////////
        ////////////// ||| Run ||| //////////////////
        /////////////////////////////////////////////
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager.print("Start MainService.Thread, Flag: " + ((Manager.mainServiceUpdateTimeThreadFlag) ? "True" : "False"));

                while (Manager.mainServiceUpdateTimeThreadFlag) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference().child("Users");

                    String phone = Manager.getMyPhone(getApplicationContext());
                    String date = String.valueOf(Manager.getCurrentTimeMills());

                    reference.child(phone).setValue(date);
                    try {
                        Thread.sleep(Manager.NETWORK_REQUEST_WAITING_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Manager.print("Stop MainService.Thread");
            }
        }).start();

        return super.onStartCommand(intent, flags, startId); // < = > return 1
//        START_STICKY ( = 1): 재시작 (intent = null)
//        START_NOT_STICKY ( = 2): 재시작 안함
//        START_REDELIVER_INTENT ( = 3): 재시작 (intent = 유지)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Manager.mainServiceUpdateTimeThreadFlag = false;
        FirebaseDatabase.getInstance().getReference().child("Chats").child(Manager.getMyPhone(getApplicationContext())).removeEventListener(childEventListener);
        Manager.print("MainService.onDestroy() -> Database Destroy Successful");
    }
}
