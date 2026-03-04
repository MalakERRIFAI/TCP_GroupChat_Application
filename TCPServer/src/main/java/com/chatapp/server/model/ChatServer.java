package com.chatapp.server.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatServer {
    private final int port;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private volatile boolean running = false;

    private Consumer<String> logSink = s -> {};
    private Consumer<Set<String>> usersSink = s -> {};

    public ChatServer(int port) {
        this.port = port;
    }

    public void setLogSink(Consumer<String> logSink) { this.logSink = logSink; }
    public void setUsersSink(Consumer<Set<String>> usersSink) { this.usersSink = usersSink; }

    public void start() {
        running = true;
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                log("Server Started on port " + port);
                while (running) {
                    log("Waiting for Client...");
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, this);
                    clients.add(handler);
                    updateUsers();
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                log("Server error: " + e.getMessage());
            }
        }, "server-main").start();
    }

    public void stop() {
        running = false;
        for (ClientHandler c : clients) c.closeSilently();
        clients.clear();
        updateUsers();
        log("Server Stopped");
    }

    void broadcast(String line) {
        for (ClientHandler c : clients) c.send(line);
    }

    void sendTo(ClientHandler target, String line) {
        target.send(line);
    }

    Set<String> activeUsernames() {
        Set<String> names = ConcurrentHashMap.newKeySet();
        for (ClientHandler c : clients) {
            String u = c.getUsername();
            if (u != null && !u.isBlank()) names.add(u);
        }
        return names;
    }

    void remove(ClientHandler handler) {
        clients.remove(handler);
        updateUsers();
    }

    void log(String msg) { logSink.accept(msg); }

    private void updateUsers() {
        usersSink.accept(activeUsernames());
    }
    public void refreshUsers() {
        usersSink.accept(activeUsernames());
    }
}
