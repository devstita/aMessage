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
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
            final Socket[] socket = {null};
            AtomicBoolean isTryingSocketConnection = new AtomicBoolean(false);

            while (Manager.mainServiceUpdateTimeThreadFlag) {
                Manager.checkUpdate(b -> {
                    if (!b) {
                        Manager.print("NOT Last Version in Service");
                        Manager.mainServiceUpdateTimeThreadFlag = false;
                    }
                });
                new Thread(() -> {
                    if (!isTryingSocketConnection.get() && Manager.checkNetworkConnect() && (socket[0] == null || !socket[0].isConnected())) {
                        try {
                            isTryingSocketConnection.set(true);
                            Manager.print("Connecting Socket");
                            socket[0] = new Socket("192.168.24.42", 8888);
                            isTryingSocketConnection.set(false);
                            Manager.print("Connected!!");

                            // TODO: Solve Send Phone Number Problem
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket[0].getOutputStream()));
                            String phone = Manager.getMyPhone(getApplicationContext());
                            bw.write(phone);
                            Manager.print("Send Phone Number");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            stopForeground(true);
            stopSelf(START_STICKY);

            Manager.print("Stop MainService.Thread");
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
