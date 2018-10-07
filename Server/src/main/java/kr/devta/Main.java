package kr.devta;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sun.istack.internal.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    public static void main(String[] args) throws IOException {
        new Main();
    }

    private Main() throws IOException {
        FileInputStream firebaseInformationJasonFileInputStream = new FileInputStream("serviceAccountKey.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(firebaseInformationJasonFileInputStream))
                .setDatabaseUrl("https://devta-amessage.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(options);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(8888);

        while (true) {
            System.out.println("Waiting..");
            Socket socket = serverSocket.accept();
            System.out.println("Connected: " + socket.getInetAddress().getHostAddress());
            threadPoolExecutor.execute(new ConnectionRunnable(socket));
        }
    }
}

class ConnectionRunnable implements Runnable {
    private Socket socket;

    public ConnectionRunnable(@NotNull Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Read phone");
            String phone = br.readLine();
            System.out.println("Phone: " + phone);

            DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference().child("InServer").child("Users").child(phone);
//            updateRef.setValueAsync("CONNECTED");
            updateRef.setValue("CONNECTED", (databaseError, databaseReference) -> {
                System.out.println("Connected Message Send Done..!!");
            });

            while (socket.isConnected());
//            updateRef.setValueAsync("DISCONNECTED");
            updateRef.setValue("DISCONNECTED", (databaseError, databaseReference) -> {
                System.out.println("Disconnected Message Send Done..!!");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
