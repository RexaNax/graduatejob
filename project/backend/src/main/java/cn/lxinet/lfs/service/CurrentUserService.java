package cn.lxinet.lfs.service;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    @Resource
    private HttpServletRequest request;

    @Resource
    private JwtConfig jwtConfig;

    public Long getCurrentUserId() {
        String token = requireValidToken();
        Long userId = jwtConfig.getUserIdFromToken(token);
        Assert.notNull(userId, ErrorCode.TOKEN_INVALID);
        return userId;
    }

    public String getCurrentUsername() {
        String token = requireValidToken();
        String username = jwtConfig.getUsernameFromToken(token);
        Assert.isTrue(StringUtils.isNotBlank(username), ErrorCode.TOKEN_INVALID);
        return username;
    }

    public boolean isAdmin() {
        return UserService.ADMIN_USERNAME.equalsIgnoreCase(getCurrentUsername());
    }

    public boolean hasValidToken() {
        try {
            return jwtConfig.parseToken(request.getHeader("token"));
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private String requireValidToken() {
        try {
            String token = request.getHeader("token");
            Assert.isTrue(jwtConfig.parseToken(token), ErrorCode.TOKEN_INVALID);
            return token;
        } catch (IllegalStateException e) {
            Assert.isTrue(false, ErrorCode.TOKEN_INVALID);
            return "";
        }
    }
}
