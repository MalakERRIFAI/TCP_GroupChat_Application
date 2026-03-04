package com.chatapp.server.model;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;

    private BufferedReader in;
    private PrintWriter out;

    private String username = "";
    private boolean readOnly = true;

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            send("SYS:Welcome! Please send USER:<name> (empty username = read-only).");

            String line;
            while ((line = in.readLine()) != null) {

                if (line.startsWith("USER:")) {
                    username = line.substring(5).trim();
                    readOnly = username.isBlank();

                    if (readOnly) {
                        send("SYS:READ-ONLY MODE enabled.");
                        server.log("Client connected in READ-ONLY mode");
                    } else {
                        server.log("Welcome " + username);
                        server.broadcast("SYS:" + username + " joined the chat.");
                    }

                    // Update "Connected Users" list in server UI AFTER username is set
                    server.refreshUsers();
                }

                else if (line.equals("CMD:allUsers")) {
                    server.sendTo(this, "USERS:" + String.join(",", server.activeUsernames()));
                }

                else if (line.startsWith("MSG:")) {
                    if (readOnly) {
                        send("SYS:You are in READ-ONLY MODE. Cannot send messages.");
                        continue;
                    }

                    String msg = line.substring(4);
                    String time = LocalTime.now().format(TF);
                    server.broadcast("CHAT:" + time + "|" + username + "|" + msg);
                }

                else if (line.equals("QUIT")) {
                    break;
                }
            }

        } catch (IOException e) {
            server.log("Client error: " + e.getMessage());

        } finally {
            closeSilently();
            server.remove(this);

            // ✅ Update user list after disconnect too
            server.refreshUsers();

            if (!username.isBlank()) {
                server.broadcast("SYS:" + username + " left the chat.");
            }
        }
    }

    public void send(String msg) {
        if (out != null) out.println(msg);
    }

    public void closeSilently() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }
}