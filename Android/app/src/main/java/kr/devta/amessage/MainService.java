package kr.devta.amessage;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

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
        Manager.mainServiceUpdateTimeThreadFlag = true;
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
            Manager.print("Start MainService.Thread, Flag: " + ((Manager.mainServiceUpdateTimeThreadFlag) ? "True" : "False"));
            while (Manager.mainServiceUpdateTimeThreadFlag) {
                Manager.checkUpdate(b -> {
                    if (!b) {
                        Manager.print("NOT Last Version in Service");
                        Manager.mainServiceUpdateTimeThreadFlag = false;
                    }
                });
            }
            stopForeground(true);
            stopSelf(START_STICKY);

            Manager.print("Stop MainService.Thread");
        }).start();

        new Thread(() -> {
            try {
                Socket socket = IO.socket("https://a-message.herokuapp.com");
                socket.connect();

                socket.emit("phone", Manager.getMyPhone(getApplicationContext()));
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
        Manager.mainServiceUpdateTimeThreadFlag = false;
        Manager.print("MainService.onDestroy() -> Database Destroy Successful");
    }
}
