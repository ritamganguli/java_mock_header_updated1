package com.lambdatest;

import java.util.Random;

public class PortAllocator {
    private static int currentPort = generateRandomPort();
    private static final Object lock = new Object();
    private static final int EXCLUDED_PORT = 8080;
    private static final int MIN_PORT = 1000;
    private static final int MAX_PORT = 9999;

    private static int generateRandomPort() {
        Random random = new Random();
        int port;
        do {
            port = random.nextInt((MAX_PORT - MIN_PORT) + 1) + MIN_PORT;
        } while (port == EXCLUDED_PORT);
        return port;
    }

    public static int getNextPort() {
        synchronized (lock) {
            return currentPort++;
        }
    }
}
