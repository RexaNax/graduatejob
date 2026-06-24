package cn.lxinet.lfs.service;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentUserServiceTest {

    @Test
    void readsUserIdAndUsernameFromTokenHeader() {
        JwtConfig jwtConfig = new JwtConfig();
        TestJwtConfigSupport.fill(jwtConfig, "demo-app", "demo-secret", 3600L);
        String token = jwtConfig.genUserToken(2L, "demo");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", token);

        CurrentUserService service = new CurrentUserService();
        setField(service, "jwtConfig", jwtConfig);
        setField(service, "request", request);

        assertEquals(2L, service.getCurrentUserId());
        assertEquals("demo", service.getCurrentUsername());
        assertFalse(service.isAdmin());
    }

    @Test
    void recognizesAdminToken() {
        JwtConfig jwtConfig = new JwtConfig();
        TestJwtConfigSupport.fill(jwtConfig, "demo-app", "demo-secret", 3600L);
        String token = jwtConfig.genUserToken(1L, "admin");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", token);

        CurrentUserService service = new CurrentUserService();
        setField(service, "jwtConfig", jwtConfig);
        setField(service, "request", request);

        assertTrue(service.isAdmin());
    }

    @Test
    void throwsWhenTokenIsMissingOrInvalid() {
        JwtConfig jwtConfig = new JwtConfig();
        TestJwtConfigSupport.fill(jwtConfig, "demo-app", "demo-secret", 3600L);

        CurrentUserService service = new CurrentUserService();
        setField(service, "jwtConfig", jwtConfig);
        setField(service, "request", new MockHttpServletRequest());

        assertThrows(BaseException.class, service::getCurrentUserId);
        assertThrows(BaseException.class, service::getCurrentUsername);
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
