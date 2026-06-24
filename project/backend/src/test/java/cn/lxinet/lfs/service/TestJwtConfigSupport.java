package cn.lxinet.lfs.service;

import cn.lxinet.lfs.config.JwtConfig;

import java.lang.reflect.Field;

final class TestJwtConfigSupport {

    private TestJwtConfigSupport() {
    }

    static void fill(JwtConfig jwtConfig, String appId, String appSecret, long expire) {
        setField(jwtConfig, "appId", appId);
        setField(jwtConfig, "appSecret", appSecret);
        setField(jwtConfig, "expire", expire);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }
}
