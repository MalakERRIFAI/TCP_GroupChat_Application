package com.chatapp.client.model;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Consumer<String> onMessage = s -> {};
    private Consumer<Boolean> onOnline = b -> {};

    public void setOnMessage(Consumer<String> onMessage) { this.onMessage = onMessage; }
    public void setOnOnline(Consumer<Boolean> onOnline) { this.onOnline = onOnline; }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        onOnline.accept(true);

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessage.accept(line);
                }
            } catch (IOException ignored) {
            } finally {
                onOnline.accept(false);
            }
        }, "client-reader").start();
    }

    public void sendRaw(String line) {
        if (out != null) out.println(line);
    }

    public void setUsername(String username) {
        sendRaw("USER:" + (username == null ? "" : username));
    }

    public void sendMessage(String msg) {
        sendRaw("MSG:" + msg);
    }

    public void requestAllUsers() {
        sendRaw("CMD:allUsers");
    }

    public void quit() {
        sendRaw("QUIT");
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}