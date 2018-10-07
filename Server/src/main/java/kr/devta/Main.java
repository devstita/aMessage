package kr.devta;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        new Main();
    }

    private final int PORT = 8888;
    private ServerSocket serverSocket;
    private Socket socket;

    private Main() throws IOException {
        serverSocket = new ServerSocket(PORT);
        socket = serverSocket.accept();

        System.out.println("Connected with: " + socket.getInetAddress().getHostAddress());

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String message = br.readLine();
        if (message.startsWith("CONNECTED")) {

        }

        System.out.println("Connection was Disconnected.. ");
    }
}
