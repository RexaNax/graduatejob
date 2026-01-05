package cn.lxinet.lfs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.lxinet.lfs.convert.FileConvert;
import cn.lxinet.lfs.dto.UploadChunkDto;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.service.FileService;
import cn.lxinet.lfs.service.FileThumService;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.utils.MinioUtil;
import cn.lxinet.lfs.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

/**
 * 文件控制器
 *
 * @author zcx
 * @date 2023/11/09
 */
@RestController
@RequestMapping("/file")
public class FileController extends BaseController{
    @Autowired
    private FileService fileService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private MinioUtil minioUtil;
    @Value("${config.file-server.type}")
    private String fileServerType;

    @PostMapping("/dirAdd")
    public Result dirAdd(Long dirId, String name){
        File dir = fileService.dirAdd(dirId, name);
        return Result.success(FileConvert.INSTANCE.toVo(dir));
    }

    @PostMapping("/rename")
    public Result rename(Long id, String name){
        fileService.updateName(id, name);
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(@RequestParam(value = "dirId", required = false, defaultValue = "0") Long dirId,
                       @RequestParam(value = "fileType", required = false, defaultValue = "0") Integer fileType,
                       @RequestParam(value = "key", required = false, defaultValue = "") String key){
        Page<FileVo> page = fileService.listByPage(dirId, fileType, key, getPageNo(), getPageSize());
        return Result.success(page);
    }

    @GetMapping("/dirTree")
    public Result dirTree(@RequestParam(value = "dirId", required = false, defaultValue = "0") Long dirId){
        List<FileTreeVo> list = fileService.dirTree(dirId);
        return Result.success(list);
    }

    @GetMapping("/detail/{fileId}")
    public Result detail(@PathVariable("fileId") Long fileId){
        FileVo fileVo = fileService.getFileVoById(fileId);
        return Result.success(fileVo);
    }

    @PostMapping("/uploadInit")
    public Result uploadInit(@RequestParam(value = "dirId", required = false, defaultValue = "0") Long dirId,
                             String fileName, String md5){
        Assert.isTrue(StringUtils.isNotEmpty(fileName), ErrorCode.PARAM_ERROR, "文件名不能为空");
        Assert.isTrue(StringUtils.isNotEmpty(md5) && md5.length() == 32, ErrorCode.PARAM_ERROR, "md5错误");
        Map<String, Object> data = new HashMap<>();
        data.put("fileServerType", fileServerType);
        if("local".equals(fileServerType)){
            UploadVo uploadVo = fileService.uploadinit(dirId, fileName, md5);
            data.put("upload", uploadVo);
            return new Result<>(data);
        }else if ("minio".equals(fileServerType)){
            String newFileName = UUID.randomUUID() + ".mp4";
            String policyUrl = minioUtil.getPolicyUrl(newFileName);
            data.put("policyUrl", policyUrl);
            data.put("fileName", newFileName);
            return new Result<>(data);
        }
        return Result.success();
    }

    @PostMapping(value="/upload")
    public Result upload(@Valid UploadChunkDto uploadChunk) throws Exception {
        Assert.isTrue(uploadChunk.getChunkTotal() >= uploadChunk.getChunkNumber(), ErrorCode.CHUNK_NUMBER_VERI_FAIL);
        Long fileId = fileService.upload(uploadChunk);
        return new Result(ErrorCode.SUCCESS.getCode(), fileId > 0 ? "合并完成" : "分片上传完成", String.valueOf(fileId));
    }

    @GetMapping("/thumList")
    public Result thumList(String md5){
        List<FileThumVo> list = fileThumService.listByMd5(md5);
        return Result.success(list);
    }

    /**
     * 更新文件缩略图
     *
     * @param fileId
     * @param thumId
     * @return {@link Result}
     */
    @PostMapping("/updateFileThum")
    public Result updateFileThum(Long fileId, Long thumId){
        fileService.updateFileThum(fileId, thumId);
        return Result.success();
    }

    /**
     * 移动文件
     *
     * @param fileIds
     * @param dirId
     * @return {@link Result}
     */
    @PostMapping("/move")
    public Result move(String fileIds, Long dirId){
        Assert.isTrue(StringUtils.isNoneBlank(fileIds));
        fileService.move(Arrays.asList(fileIds.split(",")), dirId);
        return Result.success();
    }

    /**
     * 手动转码
     *
     * @param fileId
     * @return {@link Result}
     */
    @PostMapping("/manualTranscode")
    public Result manualTranscode(Long fileId){
        fileService.manualTranscode(fileId);
        return Result.success();
    }

    /**
     * 删除文件/文件夹
     *
     * @param fileIds
     * @return {@link Result}
     */
    @PostMapping("/delete")
    public Result delete(String fileIds){
        fileService.trash(Arrays.asList(fileIds.split(",")));
        return Result.success();
    }

    @PostMapping(value="/minioUpload")
    public Result minioUpload() throws Exception {
        minioUtil.uploadFile();
        return new Result();
    }

    @PostMapping(value="/getDownloadUrl")
    public Result getDownloadUrl(String fileIds) {
        List<String> urls = fileService.getDownloadUrl(Arrays.asList(fileIds.split(",")), 0);
        return new Result(urls);
    }

    @GetMapping(value = "/filePathList")
    public Result queryPathList(Long dirId) {
        return  Result.success(fileService.getFilePathList(dirId));
    }

    /**
     * 获取存储空间统计信息
     * 
     * 【原理讲解】
     * 1. 调用 FileMapper.sumTotalFileSize() 执行 SQL: SELECT SUM(file_size) FROM lfs_file
     * 2. 返回已用空间、文件数量、最大空间限制
     * 3. 前端用进度条展示使用比例
     */
    @GetMapping("/storageStats")
    public Result storageStats() {
        Map<String, Object> stats = fileService.getStorageStats();
        return Result.success(stats);
    }
}
