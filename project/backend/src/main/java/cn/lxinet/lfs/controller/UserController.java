package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.service.UserService;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtConfig jwtConfig;

    @PostMapping("/login")
    public Result login(String username, String password) {
        Assert.isTrue(StringUtils.isNotBlank(username), ErrorCode.PARAM_ERROR, "用户名不能为空");
        Assert.isTrue(StringUtils.isNotBlank(password), ErrorCode.PARAM_ERROR, "密码不能为空");

        User user = userService.login(username, password);
        String token = jwtConfig.genUserToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", buildUserInfo(user));
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result register(String username, String password,
                           @RequestParam(required = false) String nickname) {
        Assert.isTrue(false, ErrorCode.USER_ACCESS_RESTRICTED);
        return Result.success();
    }

    @GetMapping("/info")
    public Result info(@RequestHeader("token") String token) {
        Long userId = jwtConfig.getUserIdFromToken(token);
        Assert.notNull(userId, ErrorCode.TOKEN_INVALID);

        User user = userService.getById(userId);
        Assert.notNull(user, ErrorCode.USER_NOT_EXIST);

        return Result.success(buildUserInfo(user));
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus());
        userInfo.put("isAdmin", userService.isAdminUser(user));
        return userInfo;
    }
}
