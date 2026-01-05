package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.service.FileShareService;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 文件分享控制器
 * 
 * 【原理讲解】
 * 1. /share/create: 创建分享链接（需要登录）
 * 2. /share/access/{code}: 访问分享链接（不需要登录）
 * 3. /share/cancel: 取消分享（需要登录）
 */
@RestController
@RequestMapping("/share")
public class ShareController extends BaseController {
    
    @Autowired
    private FileShareService fileShareService;
    
    /**
     * 创建分享链接
     * 
     * @param fileId 文件ID
     * @param expireDays 有效天数，0或不传表示永不过期
     */
    @PostMapping("/create")
    public Result create(Long fileId, 
                        @RequestParam(required = false, defaultValue = "0") Integer expireDays) {
        Assert.notNull(fileId, "文件ID不能为空");
        
        Map<String, Object> result = fileShareService.createShare(fileId, expireDays);
        return Result.success(result);
    }
    
    /**
     * 访问分享链接
     * 
     * 【说明】
     * 此接口不需要登录，在 WebConfig 中已排除
     * 任何人都可以通过分享码访问文件
     */
    @GetMapping("/access/{code}")
    public Result access(@PathVariable("code") String code) {
        Assert.isTrue(StringUtils.isNotBlank(code), "分享码不能为空");
        
        Map<String, Object> result = fileShareService.accessShare(code);
        return Result.success(result);
    }
    
    /**
     * 取消分享
     */
    @PostMapping("/cancel")
    public Result cancel(Long shareId) {
        Assert.notNull(shareId, "分享ID不能为空");
        
        fileShareService.cancelShare(shareId);
        return Result.success();
    }
    
    /**
     * 获取文件的分享列表
     */
    @GetMapping("/list")
    public Result list(Long fileId) {
        Assert.notNull(fileId, "文件ID不能为空");
        
        return Result.success(fileShareService.getSharesByFileId(fileId));
    }
}
