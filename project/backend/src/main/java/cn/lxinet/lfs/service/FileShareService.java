package cn.lxinet.lfs.service;

import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileShare;
import cn.lxinet.lfs.mapper.FileShareMapper;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 文件分享服务
 * 
 * 【原理讲解】
 * 1. 创建分享：生成随机分享码，存入数据库
 * 2. 访问分享：根据分享码查询，验证有效期，返回文件信息
 * 3. 分享码生成：8位随机字母数字组合
 */
@Service
public class FileShareService extends ServiceImpl<FileShareMapper, FileShare> {
    
    @Autowired
    private FileShareMapper fileShareMapper;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private FileConfig fileConfig;
    
    // 分享码字符集
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    
    /**
     * 创建文件分享
     * 
     * 【流程】
     * 1. 验证文件是否存在
     * 2. 生成唯一分享码
     * 3. 计算过期时间
     * 4. 保存分享记录
     * 5. 返回分享信息
     * 
     * @param fileId 文件ID
     * @param expireDays 有效天数，0表示永不过期
     */
    public Map<String, Object> createShare(Long fileId, Integer expireDays) {
        // 1. 验证文件存在
        File file = fileService.getById(fileId);
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        Assert.isTrue(file.getIsDir() == 0, ErrorCode.PARAM_ERROR, "文件夹不能分享");
        
        // 2. 生成分享码
        String shareCode = generateShareCode();
        
        // 3. 计算过期时间
        long expireTime = 0;
        if (expireDays != null && expireDays > 0) {
            expireTime = System.currentTimeMillis() + expireDays * 24L * 60 * 60 * 1000;
        }
        
        // 4. 保存分享记录
        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setShareCode(shareCode);
        share.setExpireTime(expireTime);
        share.setViewCount(0);
        save(share);
        
        // 5. 返回分享信息
        Map<String, Object> result = new HashMap<>();
        result.put("shareCode", shareCode);
        result.put("expireTime", expireTime);
        result.put("fileName", file.getName());
        
        return result;
    }
    
    /**
     * 访问分享链接
     * 
     * 【流程】
     * 1. 根据分享码查询分享记录
     * 2. 验证是否过期
     * 3. 增加访问次数
     * 4. 返回文件信息和下载链接
     */
    public Map<String, Object> accessShare(String shareCode) {
        // 1. 查询分享记录
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareCode, shareCode);
        FileShare share = getOne(wrapper);
        Assert.notNull(share, ErrorCode.SHARE_NOT_EXIST);
        
        // 2. 验证是否过期
        if (share.getExpireTime() > 0 && share.getExpireTime() < System.currentTimeMillis()) {
            Assert.isTrue(false, ErrorCode.SHARE_EXPIRED);
        }
        
        // 3. 增加访问次数
        fileShareMapper.incrementViewCount(share.getId());
        
        // 4. 获取文件信息
        File file = fileService.getById(share.getFileId());
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        
        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", file.getName());
        result.put("fileSize", file.getFileSize());
        result.put("suffix", file.getSuffix());
        result.put("downloadUrl", fileConfig.getDownloadUrl(file.getName(), file.getPath()));
        result.put("viewCount", share.getViewCount() + 1);
        
        return result;
    }
    
    /**
     * 生成分享码
     * 
     * 【原理】
     * 1. 从字符集中随机选取8个字符
     * 2. 检查是否重复，重复则重新生成
     * 3. 排除了容易混淆的字符（0/O, 1/l/I）
     */
    private String generateShareCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        String code = sb.toString();
        
        // 检查是否重复
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareCode, code);
        if (count(wrapper) > 0) {
            return generateShareCode(); // 递归重新生成
        }
        return code;
    }
    
    /**
     * 获取文件的分享列表
     */
    public java.util.List<FileShare> getSharesByFileId(Long fileId) {
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getFileId, fileId);
        wrapper.orderByDesc(FileShare::getCreateTime);
        return list(wrapper);
    }
    
    /**
     * 取消分享
     */
    public void cancelShare(Long shareId) {
        removeById(shareId);
    }
}
