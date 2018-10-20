package kr.devta.amessage;

import android.app.Notification;
import android.app.Service;
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
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        Manager.makeNotificationChannel(builder, Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_ID, Manager.MAIN_SERVICE_FOREGROUND_NOTIFICATION_CHANNEL_NAME);
        startForeground(startId, builder.build());

        /////////////////////////////////////////////
        ////////////// ||| Run ||| //////////////////
        /////////////////////////////////////////////

        new Thread(() -> {
            try {
                Socket socket = IO.socket("https://a-message.herokuapp.com");
                socket.connect();

                socket.emit("phone", Manager.getMyPhone(getApplicationContext()));
                socket.io().on(Socket.EVENT_DISCONNECT, args -> {
                    if (Manager.checkNetworkConnect()) {
                        // TODO: Socket Reconnect when Disconnected
                        socket.io().reconnection();
                    }
                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
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
