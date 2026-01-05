package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.mapper.UserMapper;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 * 
 * 【原理讲解】
 * 1. BCryptPasswordEncoder: Spring Security 提供的密码加密工具
 *    - encode(password): 加密密码
 *    - matches(raw, encoded): 比对密码
 * 2. BCrypt 特点：
 *    - 单向加密，无法解密
 *    - 每次加密结果不同（加盐）
 *    - 可以验证密码是否匹配
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private UserMapper userMapper;
    
    // BCrypt 密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 用户登录
     * 
     * 【流程】
     * 1. 根据用户名查询用户
     * 2. 验证用户是否存在
     * 3. 验证用户状态是否正常
     * 4. 使用 BCrypt 比对密码
     * 5. 返回用户信息（不含密码）
     */
    public User login(String username, String password) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);
        
        // 2. 验证用户存在
        Assert.notNull(user, ErrorCode.USER_NOT_EXIST);
        
        // 3. 验证用户状态
        Assert.isTrue(user.getStatus() == 1, ErrorCode.USER_DISABLED);
        
        // 4. 验证密码（BCrypt 比对）
        Assert.isTrue(passwordEncoder.matches(password, user.getPassword()), 
                     ErrorCode.PASSWORD_ERROR);
        
        // 5. 清除密码后返回
        user.setPassword(null);
        return user;
    }
    
    /**
     * 用户注册
     * 
     * 【流程】
     * 1. 检查用户名是否已存在
     * 2. 加密密码
     * 3. 保存用户
     */
    public User register(String username, String password, String nickname) {
        // 1. 检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existUser = getOne(wrapper);
        Assert.isTrue(existUser == null, ErrorCode.USER_EXIST);
        
        // 2. 创建用户，加密密码
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt 加密
        user.setNickname(nickname != null ? nickname : username);
        user.setStatus(1);
        
        // 3. 保存
        save(user);
        user.setPassword(null);
        return user;
    }
    
    /**
     * 根据用户名查询
     */
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }
}
