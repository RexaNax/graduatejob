package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件分享实体类
 * 
 * 【原理讲解】
 * 1. shareCode: 8位随机字符串，用于生成分享链接
 * 2. expireTime: 过期时间戳，0表示永不过期
 * 3. viewCount: 统计分享链接被访问的次数
 */
@Data
@TableName("lfs_file_share")
public class FileShare {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 文件ID */
    private Long fileId;
    
    /** 分享码（唯一） */
    private String shareCode;
    
    /** 过期时间戳（毫秒），0表示永不过期 */
    private Long expireTime;
    
    /** 访问次数 */
    private Integer viewCount;
    
    private Date createTime;
    
    private Date updateTime;
}
