package com.example.opscopilot.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轻量级本地 ID 生成器。
 * 通过“毫秒时间戳 + 同毫秒内自增序号”生成 long 类型主键，满足当前单体应用的简单唯一性需求。
 */
public final class IdGenerator {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static volatile long lastMillis;

    private IdGenerator() {
    }

    /**
     * 生成下一个 ID。
     * 高位使用当前毫秒时间，低 12 位使用毫秒内序列号。
     */
    public static synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now != lastMillis) {
            lastMillis = now;
            SEQUENCE.set(0);
        }
        return (now << 12) | (SEQUENCE.getAndIncrement() & 0xFFF);
    }
}
