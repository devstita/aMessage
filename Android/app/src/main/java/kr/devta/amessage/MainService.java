package kr.devta.amessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainService extends Service {

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
                // TODO : Auto Reconnect when Disconnected
                IO.Options socketOptions = new IO.Options();
                socketOptions.forceNew = true;
                socketOptions.reconnection = true;

                Socket socket = IO.socket("https://a-message.herokuapp.com", socketOptions);
                socket.connect();

                socket.io().on(Socket.EVENT_DISCONNECT, args -> {
                    if (Manager.checkNetworkConnect()) {
                        Manager.print("Disconnected..");
                    }
                }).on(Socket.EVENT_CONNECT, args -> {
                    Manager.print("Connected to Socket.IO Server");
                    socket.emit("phone", Manager.getMyPhone());
            });
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
        Manager.print("MainService.onDestroy() -> Database Destroy Successful");
    }
}
