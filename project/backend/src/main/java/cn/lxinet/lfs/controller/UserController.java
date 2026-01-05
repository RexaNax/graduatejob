package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.service.UserService;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 
 * 【原理讲解】
 * 1. /user/login: 登录接口，验证用户名密码，返回 JWT Token
 * 2. /user/register: 注册接口，创建新用户
 * 3. /user/info: 获取当前登录用户信息（需要 Token）
 * 
 * 注意：login 和 register 接口不需要 Token，需要在拦截器中排除
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    /**
     * 用户登录
     * 
     * 【流程】
     * 1. 接收用户名和密码
     * 2. 调用 UserService.login() 验证
     * 3. 验证成功后，生成 JWT Token
     * 4. 返回 Token 和用户信息
     * 
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping("/login")
    public Result login(String username, String password) {
        // 参数校验
        Assert.isTrue(StringUtils.isNotBlank(username), ErrorCode.PARAM_ERROR, "用户名不能为空");
        Assert.isTrue(StringUtils.isNotBlank(password), ErrorCode.PARAM_ERROR, "密码不能为空");
        
        // 验证登录
        User user = userService.login(username, password);
        
        // 生成 Token
        String token = jwtConfig.genUserToken(user.getId(), user.getUsername());
        
        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data);
    }
    
    /**
     * 用户注册
     * 
     * 【流程】
     * 1. 接收用户名、密码、昵称
     * 2. 检查用户名是否已存在
     * 3. 加密密码并保存用户
     * 4. 返回用户信息
     */
    @PostMapping("/register")
    public Result register(String username, String password, 
                          @RequestParam(required = false) String nickname) {
        // 参数校验
        Assert.isTrue(StringUtils.isNotBlank(username), ErrorCode.PARAM_ERROR, "用户名不能为空");
        Assert.isTrue(username.length() >= 3 && username.length() <= 20, 
                     ErrorCode.PARAM_ERROR, "用户名长度3-20个字符");
        Assert.isTrue(StringUtils.isNotBlank(password), ErrorCode.PARAM_ERROR, "密码不能为空");
        Assert.isTrue(password.length() >= 6, ErrorCode.PARAM_ERROR, "密码至少6位");
        
        // 注册用户
        User user = userService.register(username, password, nickname);
        
        // 自动登录，返回 Token
        String token = jwtConfig.genUserToken(user.getId(), user.getUsername());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        return Result.success(data);
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * 【原理】
     * 从请求头的 Token 中解析出 userId，查询用户信息
     */
    @GetMapping("/info")
    public Result info(@RequestHeader("token") String token) {
        Long userId = jwtConfig.getUserIdFromToken(token);
        Assert.notNull(userId, ErrorCode.TOKEN_INVALID);
        
        User user = userService.getById(userId);
        Assert.notNull(user, ErrorCode.USER_NOT_EXIST);
        
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 退出登录
     * 
     * 【说明】
     * JWT 是无状态的，服务端不存储 Token
     * 退出登录只需要前端删除本地存储的 Token 即可
     * 这个接口只是返回成功，实际操作在前端完成
     */
    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }
}
