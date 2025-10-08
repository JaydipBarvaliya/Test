package com.td.esig.api.config;

import java.lang.reflect.Field;

final class TestUtils {
    private TestUtils() {}

    static void injectField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
}