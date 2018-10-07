package kr.devta;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        new Main();
    }

    private final int PORT = 8888;
    private ServerSocket serverSocket;
    private Socket socket;

    public Main() throws IOException {
        serverSocket = new ServerSocket(PORT);
        socket = serverSocket.accept();

        System.out.println("Connected with: " + socket.getInetAddress().getHostAddress());

        boolean isConnected = true;
        while (isConnected) {
            isConnected = socket.isConnected();
        }

        System.out.println("Connection was Disconnected.. ");
    }
}
