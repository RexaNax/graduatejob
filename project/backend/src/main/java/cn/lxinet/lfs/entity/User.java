package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 * 
 * 【原理讲解】
 * 1. @TableName("sys_user") 指定对应的数据库表
 * 2. @TableId 指定主键，IdType.AUTO 表示自增
 * 3. password 存储 BCrypt 加密后的密码
 */
@Data
@TableName("sys_user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户名（登录账号） */
    private String username;
    
    /** 密码（BCrypt加密存储） */
    private String password;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
    
    /** 状态：0禁用，1启用 */
    private Integer status;
    
    private Date createTime;
    
    private Date updateTime;
}
