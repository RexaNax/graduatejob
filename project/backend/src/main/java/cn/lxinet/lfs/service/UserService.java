package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.mapper.UserMapper;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public static final String ADMIN_USERNAME = "admin";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);

        Assert.notNull(user, ErrorCode.USER_NOT_EXIST);
        Assert.isTrue(user.getStatus() == 1, ErrorCode.USER_DISABLED);
        Assert.isTrue(passwordEncoder.matches(password, user.getPassword()), ErrorCode.PASSWORD_ERROR);

        user.setPassword(null);
        return user;
    }

    public User register(String username, String password, String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existUser = getOne(wrapper);
        Assert.isTrue(existUser == null, ErrorCode.USER_EXIST);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setStatus(1);

        save(user);
        user.setPassword(null);
        return user;
    }

    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    public boolean isAdminUser(User user) {
        return user != null && ADMIN_USERNAME.equalsIgnoreCase(user.getUsername());
    }
}
