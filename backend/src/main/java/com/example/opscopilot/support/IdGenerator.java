package com.example.opscopilot.support;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static volatile long lastMillis;

    private IdGenerator() {
    }

    public static synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now != lastMillis) {
            lastMillis = now;
            SEQUENCE.set(0);
        }
        return (now << 12) | (SEQUENCE.getAndIncrement() & 0xFFF);
    }
}
