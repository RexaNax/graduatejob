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
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class FileShareService extends ServiceImpl<FileShareMapper, FileShare> {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    @Autowired
    private FileShareMapper fileShareMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileConfig fileConfig;

    public Map<String, Object> createShare(Long fileId, Integer expireDays) {
        File file = fileService.requireAccessibleFile(fileId);
        Assert.isTrue(file.getIsDir() == 0, ErrorCode.PARAM_ERROR, "文件夹不能分享");
        String shareCode = generateShareCode();
        long expireTime = 0;
        if (expireDays != null && expireDays > 0) {
            expireTime = System.currentTimeMillis() + expireDays * 24L * 60 * 60 * 1000;
        }

        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setShareCode(shareCode);
        share.setExpireTime(expireTime);
        share.setViewCount(0);
        save(share);

        Map<String, Object> result = new HashMap<>();
        result.put("shareCode", shareCode);
        result.put("expireTime", expireTime);
        result.put("fileName", file.getName());
        return result;
    }

    public Map<String, Object> accessShare(String shareCode) {
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareCode, shareCode);
        FileShare share = getOne(wrapper);
        Assert.notNull(share, ErrorCode.SHARE_NOT_EXIST);
        if (share.getExpireTime() > 0 && share.getExpireTime() < System.currentTimeMillis()) {
            Assert.isTrue(false, ErrorCode.SHARE_EXPIRED);
        }
        fileShareMapper.incrementViewCount(share.getId());

        File file = fileService.getById(share.getFileId());
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);

        Map<String, Object> result = new HashMap<>();
        result.put("fileName", file.getName());
        result.put("fileSize", file.getFileSize());
        result.put("suffix", file.getSuffix());
        result.put("downloadUrl", fileConfig.getDownloadUrl(file.getName(), file.getPath()));
        result.put("viewCount", share.getViewCount() + 1);
        return result;
    }

    private String generateShareCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        String code = sb.toString();
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareCode, code);
        if (count(wrapper) > 0) {
            return generateShareCode();
        }
        return code;
    }

    public List<FileShare> getSharesByFileId(Long fileId) {
        fileService.requireAccessibleFile(fileId);
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getFileId, fileId);
        wrapper.orderByDesc(FileShare::getCreateTime);
        return list(wrapper);
    }

    public void cancelShare(Long shareId) {
        FileShare share = getById(shareId);
        Assert.notNull(share, ErrorCode.SHARE_NOT_EXIST);
        fileService.requireAccessibleFile(share.getFileId());
        removeById(shareId);
    }
}
