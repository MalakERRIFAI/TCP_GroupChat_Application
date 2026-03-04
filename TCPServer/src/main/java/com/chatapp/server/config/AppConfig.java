package com.chatapp.server.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties props = new Properties();

    public AppConfig() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) throw new IllegalStateException("config.properties not found");
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int port() {
        return Integer.parseInt(props.getProperty("server.port", "4444").trim());
    }
}