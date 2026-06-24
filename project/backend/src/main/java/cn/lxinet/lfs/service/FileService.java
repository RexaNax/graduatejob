package cn.lxinet.lfs.service;

import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.config.PdfWatermarkConfig;
import cn.lxinet.lfs.convert.FileConvert;
import cn.lxinet.lfs.convert.TransProgressConvert;
import cn.lxinet.lfs.dto.UploadChunkDto;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.entity.FileTrash;
import cn.lxinet.lfs.entity.FileTrashDetail;
import cn.lxinet.lfs.entity.TransFile;
import cn.lxinet.lfs.entity.TransProgress;
import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileInTrash;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.enums.FileType;
import cn.lxinet.lfs.event.FileDeleteEvent;
import cn.lxinet.lfs.event.TransEvent;
import cn.lxinet.lfs.mapper.FileMapper;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.utils.FileUtil;
import cn.lxinet.lfs.utils.PdfUtil;
import cn.lxinet.lfs.utils.VideoUtil;
import cn.lxinet.lfs.vo.FileTreeVo;
import cn.lxinet.lfs.vo.FileVo;
import cn.lxinet.lfs.vo.TransProgressVo;
import cn.lxinet.lfs.vo.UploadVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.schild.jave.info.MultimediaInfo;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService extends ServiceImpl<FileMapper, File> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @Value("${config.trash-recycle-days}")
    private Integer trashRetainDays;

    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private TransTemplateService transTemplateService;
    @Autowired
    private TransFileService transFileService;
    @Autowired
    private FileTrashDetailService fileTrashDetailService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private TransProgressService transProgressService;
    @Autowired
    private FileTrashService fileTrashService;
    @Autowired
    private PdfWatermarkConfig pdfWatermarkConfig;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CurrentUserService currentUserService;

    private boolean shouldFilterByCurrentUser() {
        return currentUserService.hasValidToken() && !currentUserService.isAdmin();
    }

    private void appendOwnershipFilter(LambdaQueryWrapper<File> wrapper) {
        if (shouldFilterByCurrentUser()) {
            wrapper.eq(File::getUserId, currentUserService.getCurrentUserId());
        }
    }

    public File requireAccessibleFile(Long fileId) {
        return getAccessibleFileOrThrow(fileId);
    }

    private File getAccessibleFileOrThrow(Long fileId) {
        File file = getById(fileId);
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        assertAccessibleFile(file);
        return file;
    }

    private void assertAccessibleFile(File file) {
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        if (shouldFilterByCurrentUser()) {
            Assert.isTrue(Objects.equals(file.getUserId(), currentUserService.getCurrentUserId()), ErrorCode.FILE_NOT_EXIST);
        }
    }

    private void assertTargetDirAccessible(Long dirId) {
        if (dirId == null || dirId == 0L) {
            return;
        }
        File dir = getAccessibleFileOrThrow(dirId);
        Assert.isTrue(dir.getIsDir() == 1, ErrorCode.FILE_DIR_NOT_EXIST);
        Assert.isTrue(Objects.equals(dir.getInTrash(), FileInTrash.NO.getValue()), ErrorCode.FILE_DIR_NOT_EXIST);
    }

    public UploadVo uploadinit(Long dirId, String fileName, String md5) {
        assertTargetDirAccessible(dirId);
        File file = findByMd5(md5);
        if (file == null) {
            return new UploadVo(createUploadId());
        }
        java.io.File localFile = new java.io.File(fileConfig.getLocalFileDir() + file.getPath());
        if (!localFile.exists()) {
            return new UploadVo(createUploadId());
        }
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        Long fileId = fastUpload(dirId, file, fileName, suffix);
        return new UploadVo(true, fileId);
    }

    private String createUploadId() {
        String uploadId = UUID.randomUUID().toString();
        redisService.set(fileConfig.getUploadIdKey(uploadId), uploadId, 24 * 60 * 60);
        LOGGER.info("Upload init complete, uploadId={}", uploadId);
        return uploadId;
    }

    public Long upload(UploadChunkDto chunk) throws IOException {
        Assert.isTrue(redisService.exists(fileConfig.getUploadIdKey(chunk.getUploadId())), ErrorCode.UPLOADID_VOID);
        assertTargetDirAccessible(chunk.getDirId());
        if (chunk.getChunkTotal() > 1) {
            return uploadChunk(chunk);
        }
        String fileName = chunk.getFileName();
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        String localPath = fileConfig.getUploadFilePath(suffix);
        java.io.File file = new java.io.File(localPath);
        chunk.getFile().transferTo(file);
        redisService.del(chunk.getUploadId());
        return uploadSuccess(chunk.getDirId(), chunk.getUploadId(), chunk.getMd5(), localPath, fileName, file.length(), suffix);
    }

    public Long uploadChunk(UploadChunkDto chunk) throws IOException {
        assertTargetDirAccessible(chunk.getDirId());
        String uploadId = chunk.getUploadId();
        chunk.getFile().transferTo(fileConfig.getTempChunkFile(uploadId, chunk.getChunkNumber()));
        Long fileId = 0L;
        if (fileConfig.getLocalTempChunkNum(uploadId).intValue() == chunk.getChunkTotal().intValue()) {
            fileId = mergeFile(chunk.getDirId(), uploadId, chunk.getMd5(), chunk.getFileName());
            redisService.del(chunk.getUploadId());
        }
        return fileId;
    }

    private Long mergeFile(Long dirId, String uploadId, String md5, String fileName) {
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        byte[] buff = new byte[1024];
        int len;
        FileInputStream in = null;
        BufferedOutputStream outputStream = null;
        Long fileId = 0L;
        try {
            java.io.File tempDirFile = new java.io.File(fileConfig.getUploadTempDir(uploadId));
            int chunkTotal = tempDirFile.list().length;
            String localPath = fileConfig.getUploadFilePath(suffix);
            java.io.File targetFile = new java.io.File(localPath);
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            for (int i = 0; i < chunkTotal; i++) {
                java.io.File tempFile = fileConfig.getTempChunkFile(uploadId, i + 1);
                in = new FileInputStream(tempFile);
                while ((len = in.read(buff, 0, 1024)) > 0) {
                    outputStream.write(buff, 0, len);
                    outputStream.flush();
                }
                in.close();
                tempFile.delete();
            }
            tempDirFile.delete();
            fileId = uploadSuccess(dirId, uploadId, md5, localPath, fileName, targetFile.length(), suffix);
        } catch (Exception e) {
            LOGGER.error("Merge upload failed, uploadId={}", uploadId, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                LOGGER.error("Close stream failed, uploadId={}", uploadId, e);
            }
        }
        return fileId;
    }

    private Long fastUpload(Long dirId, File oldFile, String fileName, String suffix) {
        assertTargetDirAccessible(dirId);
        String localPath = fileConfig.getLocalFileDir() + oldFile.getPath();
        Date lastUpdateTime = transTemplateService.getLastUpdatedTime();
        if (oldFile.getTransStatus().equals(FileTransStatus.TRANS_SUCCESS.getStatus())
                && lastUpdateTime != null
                && lastUpdateTime.before(oldFile.getCreateTime())) {
            Long fileId = saveFile(dirId, oldFile.getMd5(), fileName, oldFile.getFileSize(), suffix, oldFile.getPath(),
                    oldFile.getDuration(), FileTransStatus.TRANS_SUCCESS);
            QueryWrapper<TransFile> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("file_size", "suffix", "md5", "path").eq("file_id", oldFile.getId());
            List<TransFile> transList = transFileService.list(queryWrapper);
            transList.forEach(trans -> {
                TransFile transFile = new TransFile(fileId, trans.getMd5(), trans.getFileSize(), trans.getSuffix(), trans.getPath());
                transFileService.save(transFile);
                Long time = System.currentTimeMillis();
                transProgressService.save(new TransProgress(
                        fileId,
                        trans.getSuffix().replace(".", ""),
                        100d,
                        transFile.getId(),
                        FileTransStatus.TRANS_SUCCESS.getStatus(),
                        time,
                        time
                ));
            });
            if (FileUtil.isVideo(suffix)) {
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_VIDEO,
                        FileType.VIDEO, localPath, oldFile.getMd5(), oldFile.getDuration()));
            } else if (FileUtil.isDocument(suffix)) {
                String pdfLocalPath = FileUtil.isPdf(suffix) ? fileConfig.getLocalFileDir() + oldFile.getPath() : "";
                if (!transList.isEmpty()) {
                    pdfLocalPath = fileConfig.getLocalFileDir() + transList.get(0).getPath();
                }
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_PDF,
                        FileType.DOCUMENT, pdfLocalPath, oldFile.getMd5()));
            }
            return fileId;
        }
        return uploadSuccess(dirId, "", oldFile.getMd5(), localPath, fileName, oldFile.getFileSize(), suffix, oldFile.getDuration());
    }

    private Long uploadSuccess(Long dirId, String uploadId, String md5, String localPath, String fileName,
                               Long fileSize, String suffix, Long duration) {
        String filePath = localPath.replace(fileConfig.getLocalFileDir(), "").replace("\\", "/");
        Long fileId = saveFile(dirId, md5, fileName, fileSize, suffix, filePath, duration);
        toTranscode(fileId, md5, duration, localPath, suffix);
        if (StringUtils.isNotBlank(uploadId)) {
            redisService.del(fileConfig.getUploadIdKey(uploadId));
        }
        return fileId;
    }

    private Long uploadSuccess(Long dirId, String uploadId, String md5, String localPath, String fileName,
                               Long fileSize, String suffix) {
        return uploadSuccess(dirId, uploadId, md5, localPath, fileName, fileSize, suffix, getDuration(localPath, suffix));
    }

    private Long getDuration(String localPath, String suffix) {
        if (!FileUtil.isVideo(suffix)) {
            return 0L;
        }
        MultimediaInfo mediaInfo = VideoUtil.getVideoInfo(localPath);
        return mediaInfo == null ? 0L : mediaInfo.getDuration() / 1000;
    }

    private void toTranscode(Long fileId, String md5, Long duration, String localPath, String suffix) {
        if (FileUtil.isVideo(suffix)) {
            applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_VIDEO,
                    FileType.VIDEO, localPath, md5, duration));
            applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.TO_MP4,
                    FileType.VIDEO, localPath));
        } else if (FileUtil.isDocument(suffix)) {
            if (FileUtil.isPdf(suffix)) {
                updateTrans(fileId, FileTransStatus.NO_NEED_TRANS.getStatus());
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_PDF,
                        FileType.DOCUMENT, localPath, md5));
            } else {
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.TO_PDF,
                        FileType.DOCUMENT, localPath, md5));
            }
        } else {
            updateTrans(fileId, FileTransStatus.NO_SUPPORT_TRANS.getStatus());
        }
    }

    @Transactional
    public void manualTranscode(Long fileId) {
        File file = getAccessibleFileOrThrow(fileId);
        Assert.isTrue(Objects.equals(file.getInTrash(), FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        Assert.isTrue(!Objects.equals(file.getTransStatus(), FileTransStatus.NO_NEED_TRANS.getStatus()), ErrorCode.FILE_NOT_NEED_TRANS);
        Assert.isTrue(!Objects.equals(file.getTransStatus(), FileTransStatus.NO_SUPPORT_TRANS.getStatus()), ErrorCode.FILE_NOT_SUPPORT_TRANS);
        if (System.currentTimeMillis() - file.getCreateTime().getTime() < 2 * 60 * 60 * 1000) {
            Assert.isTrue(!Objects.equals(file.getTransStatus(), FileTransStatus.TRANS.getStatus()),
                    ErrorCode.TRANS_IN_PROGRESS_CANNOT_TRANS_AGAIN);
        }
        transFileService.deleteByFileId(fileId);
        transProgressService.deleteByFileId(fileId);
        updateTrans(fileId, FileTransStatus.TRANS.getStatus());
        toTranscode(fileId, file.getMd5(), file.getDuration(), fileConfig.getLocalFileDir() + file.getPath(), file.getSuffix());
    }

    public Page<FileVo> listByPage(Long dirId, Integer fileType, String key, Long current, Long size) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getInTrash, FileInTrash.NO.getValue());
        appendOwnershipFilter(wrapper);
        if (StringUtils.isBlank(key) && fileType == 0) {
            wrapper.eq(File::getDirId, dirId);
        } else {
            if (StringUtils.isNotBlank(key)) {
                wrapper.like(File::getName, key);
            }
            if (fileType > 0) {
                wrapper.eq(File::getFileType, fileType);
            }
        }
        wrapper.orderByDesc(File::getIsDir, File::getId);
        Page<File> page = page(new Page<>(current, size), wrapper);
        Page<FileVo> voPage = new Page<>(current, size, page.getTotal());
        List<FileVo> voList = FileConvert.INSTANCE.toVoList(page.getRecords());
        if (StringUtils.isNotBlank(key) || fileType > 0) {
            Map<Long, String> dirNameMap = new HashMap<>();
            List<Long> dirIds = page.getRecords().stream().map(File::getDirId).filter(id -> id > 0).collect(Collectors.toList());
            if (!dirIds.isEmpty()) {
                QueryWrapper<File> dirQuery = new QueryWrapper<File>().select("id", "name").in("id", dirIds);
                if (shouldFilterByCurrentUser()) {
                    dirQuery.eq("user_id", currentUserService.getCurrentUserId());
                }
                List<File> dirList = list(dirQuery);
                dirNameMap = dirList.stream().collect(Collectors.toMap(File::getId, File::getName));
            }
            Map<Long, String> finalDirNameMap = dirNameMap;
            voList.forEach(vo -> vo.setDirName(finalDirNameMap.getOrDefault(vo.getDirId(), "所有文件")));
        }
        voList.forEach(vo -> {
            setTumUrl(vo);
            setVoFileType(vo);
        });
        voPage.setRecords(voList);
        return voPage;
    }

    public List<FileTreeVo> dirTree(Long dirId) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(File::getId, File::getName);
        wrapper.eq(File::getInTrash, FileInTrash.NO.getValue())
                .eq(File::getDirId, dirId)
                .eq(File::getIsDir, 1);
        appendOwnershipFilter(wrapper);
        wrapper.orderByDesc(File::getIsDir, File::getId);
        List<File> files = list(wrapper);
        List<FileTreeVo> fileTree = new ArrayList<>();
        if (files.isEmpty()) {
            return fileTree;
        }
        List<Long> fileIds = files.stream().map(File::getId).collect(Collectors.toList());
        LambdaQueryWrapper<File> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.select(File::getId, File::getName, File::getDirId);
        subWrapper.eq(File::getInTrash, FileInTrash.NO.getValue())
                .in(File::getDirId, fileIds)
                .eq(File::getIsDir, 1);
        appendOwnershipFilter(subWrapper);
        subWrapper.orderByDesc(File::getIsDir, File::getId);
        List<File> subFiles = list(subWrapper);
        files.forEach(file -> fileTree.add(new FileTreeVo(file.getId(), file.getName())));
        fileTree.forEach(tree -> subFiles.forEach(subTree -> {
            if (Objects.equals(subTree.getDirId(), tree.getId())) {
                tree.getChildren().add(new FileTreeVo(subTree.getId(), subTree.getName()));
            }
        }));
        fileTree.forEach(tree -> {
            tree.setLeaf(tree.getChildren().isEmpty());
            tree.getChildren().forEach(subTree -> subTree.setLeaf(subTree.getChildren().isEmpty()));
        });
        return fileTree;
    }

    public FileVo getFileVoById(Long id) {
        File file = getAccessibleFileOrThrow(id);
        Assert.isTrue(Objects.equals(file.getInTrash(), FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        FileVo fileVo = FileConvert.INSTANCE.toVo(file);
        setTumUrl(fileVo);
        setVoFileType(fileVo);
        if (FileUtil.isDocument(file.getSuffix()) || FileUtil.isImage(file.getSuffix())) {
            fileVo.setPdfWatermark(PdfUtil.getWmContent(pdfWatermarkConfig));
        }
        if (FileUtil.isVideo(file.getSuffix()) || FileUtil.isAudio(file.getSuffix())
                || FileUtil.isPdf(file.getSuffix()) || FileUtil.isImage(file.getSuffix())) {
            fileVo.setPreviewUrl(fileConfig.getPreviewUrl(fileVo.getPath()));
        } else if (Objects.equals(file.getTransStatus(), FileTransStatus.TRANS_SUCCESS.getStatus())) {
            List<TransFile> fileTransList = transFileService.list(new QueryWrapper<TransFile>().select("file_id", "path")
                    .in("file_id", Collections.singletonList(id)));
            if (!fileTransList.isEmpty()) {
                fileVo.setPreviewUrl(fileConfig.getPreviewUrl(fileTransList.get(0).getPath()));
            }
        }
        List<TransProgress> progressList = transProgressService.list(new QueryWrapper<TransProgress>()
                .select("id", "file_trans_id", "format", "progress", "trans_status", "start_time", "end_time")
                .eq("file_id", id));
        if (progressList.isEmpty()) {
            fileVo.setProgressList(new ArrayList<>());
            return fileVo;
        }
        List<TransProgressVo> progressVoList = TransProgressConvert.INSTANCE.toVoList(progressList);
        List<Long> fileTransIds = progressVoList.stream().map(TransProgressVo::getFileTransId).collect(Collectors.toList());
        List<TransFile> fileTransList = transFileService.list(new QueryWrapper<>(TransFile.class).in("id", fileTransIds));
        Map<Long, TransFile> transMap = fileTransList.stream().collect(Collectors.toMap(TransFile::getId, value -> value));
        progressVoList.forEach(vo -> {
            TransFile trans = transMap.get(vo.getFileTransId());
            if (trans == null) {
                return;
            }
            vo.setFileSize(trans.getFileSize());
            vo.setPreviewUrl(fileConfig.getPreviewUrl(trans.getPath()));
        });
        fileVo.setProgressList(progressVoList);
        return fileVo;
    }

    public List<File> getFilePathList(Long dirId) {
        List<File> dirList = new ArrayList<>();
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .select(File::getId, File::getName, File::getDirId)
                .eq(File::getId, dirId)
                .eq(File::getInTrash, FileInTrash.NO.getValue());
        appendOwnershipFilter(wrapper);
        File dir = getOne(wrapper);
        if (dir == null) {
            return dirList;
        }
        dirList.add(dir);
        getFilePathList(dirList, dir);
        Collections.reverse(dirList);
        return dirList;
    }

    public void getFilePathList(List<File> dirList, File file) {
        if (file.getDirId() == 0) {
            return;
        }
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .select(File::getId, File::getName, File::getDirId)
                .eq(File::getId, file.getDirId())
                .eq(File::getInTrash, FileInTrash.NO.getValue());
        appendOwnershipFilter(wrapper);
        File dir = getOne(wrapper);
        if (dir == null) {
            return;
        }
        dirList.add(dir);
        getFilePathList(dirList, dir);
    }

    public List<String> getDownloadUrl(List<String> ids, Integer type) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .in(File::getId, ids)
                .eq(File::getInTrash, FileInTrash.NO.getValue());
        appendOwnershipFilter(wrapper);
        List<File> files = list(wrapper);
        List<String> urls = new ArrayList<>();
        if (type == 0) {
            files.forEach(file -> urls.add(fileConfig.getDownloadUrl(file.getName(), file.getPath())));
        }
        return urls;
    }

    private Long saveFile(Long dirId, String md5, String fileName, Long fileSize, String suffix, String path, Long duration) {
        return saveFile(dirId, md5, fileName, fileSize, suffix, path, duration, FileTransStatus.TRANS);
    }

    private Long saveFile(Long dirId, String md5, String fileName, Long fileSize, String suffix, String path,
                          Long duration, FileTransStatus transStatus) {
        File file = new File(dirId, currentUserService.getCurrentUserId(), md5, fileName, fileSize, suffix, path,
                duration, transStatus.getStatus(), getFileType(suffix));
        save(file);
        return file.getId();
    }

    public File dirAdd(Long dirId, String name) {
        assertTargetDirAccessible(dirId);
        File dir = new File();
        dir.setDirId(dirId);
        dir.setUserId(currentUserService.getCurrentUserId());
        dir.setIsDir(1);
        dir.setName(name);
        dir.setTransStatus(FileTransStatus.NO_NEED_TRANS.getStatus());
        save(dir);
        return dir;
    }

    @Transactional(rollbackFor = Exception.class)
    public void trash(List<String> ids) {
        ids.forEach(idStr -> {
            Long id = Long.parseLong(idStr);
            File file = getAccessibleFileOrThrow(id);
            if (Objects.equals(file.getInTrash(), FileInTrash.YES.getValue()) || file.getDeleted() == 1) {
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, trashRetainDays);
            FileTrash fileTrash = new FileTrash(file.getId(), trashRetainDays, calendar.getTimeInMillis(), 0L);
            List<FileTrashDetail> trashDetailList = new ArrayList<>();
            fileTrashService.save(fileTrash);
            if (file.getIsDir() == 1) {
                trashDir(id, fileTrash.getId(), trashDetailList, calendar.getTimeInMillis());
            } else {
                trashFile(id);
                trashDetailList.add(new FileTrashDetail(fileTrash.getId(), id));
            }
            fileTrashDetailService.saveBatch(trashDetailList);
        });
    }

    public void trashFile(Long id) {
        update(new LambdaUpdateWrapper<File>()
                .set(File::getInTrash, FileInTrash.YES.getValue())
                .eq(File::getId, id));
    }

    public void trashDir(Long id, Long trashId, List<FileTrashDetail> trashDetailList, Long expireTime) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .eq(File::getDirId, id)
                .eq(File::getInTrash, FileInTrash.NO.getValue())
                .eq(File::getDeleted, 0);
        appendOwnershipFilter(wrapper);
        List<File> files = list(wrapper);
        files.forEach(file -> {
            if (file.getIsDir() == 1) {
                trashDir(file.getId(), trashId, trashDetailList, expireTime);
            } else {
                trashFile(file.getId());
                trashDetailList.add(new FileTrashDetail(trashId, file.getId()));
            }
        });
        trashFile(id);
        trashDetailList.add(new FileTrashDetail(trashId, id));
    }

    public void delete(List<Long> fileIds) {
        if (fileIds.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(File::getDeleted, 1).in(File::getId, fileIds);
        update(updateWrapper);
        transFileService.deleteByFileIds(fileIds);
        transProgressService.deleteByFileIds(fileIds);
        applicationContext.publishEvent(new FileDeleteEvent(this, fileIds));
    }

    public void updateTrans(Long id, Integer transStatus) {
        fileMapper.updateTrans(id, transStatus, new Date());
    }

    public void updateMd5(Long id, String md5) {
        fileMapper.updateMd5(id, md5, new Date());
    }

    public File findByMd5(String md5) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5).eq(File::getInTrash, FileInTrash.NO.getValue()).orderByDesc(File::getId);
        appendOwnershipFilter(wrapper);
        Page<File> page = page(new Page<>(1, 1), wrapper);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    public File findByMd5WithTrash(String md5) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5).orderByDesc(File::getId);
        Page<File> page = page(new Page<>(1, 1), wrapper);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    public void updateFileThum(Long fileId, Long thumId) {
        FileThum fileThum = fileThumService.getById(thumId);
        Assert.notNull(fileThum, ErrorCode.FILE_THUM_NOT_EXIST);
        updateFileThum(fileId, fileThum.getPath());
    }

    public void updateFileThum(Long fileId, String thumPath) {
        File file = getAccessibleFileOrThrow(fileId);
        Assert.isTrue(Objects.equals(file.getInTrash(), FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        update(new LambdaUpdateWrapper<File>()
                .set(File::getThumPath, thumPath)
                .set(File::getUpdateTime, new Date())
                .eq(File::getId, fileId)
                .eq(File::getInTrash, FileInTrash.NO.getValue()));
    }

    public void updateDuration(Long fileId, Long duration) {
        getAccessibleFileOrThrow(fileId);
        update(new LambdaUpdateWrapper<File>()
                .set(File::getDuration, duration)
                .set(File::getUpdateTime, new Date())
                .eq(File::getId, fileId));
    }

    public void updateName(Long fileId, String name) {
        Assert.isTrue(StringUtils.isNotEmpty(name), ErrorCode.FILE_NAME_CANNOT_EMPTY);
        Assert.isTrue(name.length() <= 200, ErrorCode.FILE_NAME_CANNOT_EXCEED_200_CHARACTERS);
        File file = getAccessibleFileOrThrow(fileId);
        Assert.isTrue(Objects.equals(file.getInTrash(), FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        update(new LambdaUpdateWrapper<File>()
                .set(File::getName, name)
                .eq(File::getId, fileId)
                .eq(File::getInTrash, FileInTrash.NO.getValue()));
    }

    public void move(List<String> fileIds, Long targetDirId) {
        assertTargetDirAccessible(targetDirId);
        List<Long> idList = fileIds.stream().map(Long::parseLong).collect(Collectors.toList());
        idList.forEach(fileId -> {
            File file = getAccessibleFileOrThrow(fileId);
            Assert.isTrue(Objects.equals(file.getInTrash(), FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        });
        if (targetDirId > 0) {
            Map<Long, Long> allParentIdMap = new HashMap<>();
            getAllParentIdMap(allParentIdMap, targetDirId);
            Assert.isTrue(!allParentIdMap.isEmpty(), ErrorCode.FILE_DIR_NOT_EXIST);
            idList.forEach(fileId -> Assert.isTrue(!allParentIdMap.containsKey(fileId), ErrorCode.FILE_DIR_MOVE_NOT_SELF_OR_SUBDIR));
        }
        LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<File>()
                .set(File::getDirId, targetDirId)
                .in(File::getId, idList)
                .eq(File::getInTrash, FileInTrash.NO.getValue());
        if (shouldFilterByCurrentUser()) {
            updateWrapper.eq(File::getUserId, currentUserService.getCurrentUserId());
        }
        update(updateWrapper);
    }

    private void getAllParentIdMap(Map<Long, Long> parentIdMap, Long id) {
        if (id == 0) {
            return;
        }
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .select(File::getId, File::getDirId)
                .eq(File::getId, id)
                .eq(File::getInTrash, FileInTrash.NO.getValue());
        appendOwnershipFilter(wrapper);
        File file = getOne(wrapper);
        if (file == null) {
            return;
        }
        parentIdMap.put(file.getId(), file.getId());
        if (file.getDirId() > 0L) {
            getAllParentIdMap(parentIdMap, file.getDirId());
        }
    }

    private void setTumUrl(FileVo vo) {
        if (FileUtil.isImage(vo.getSuffix())) {
            vo.setThumUrl(fileConfig.getPreviewUrl(vo.getPath()));
        } else {
            vo.setThumUrl(fileConfig.getPreviewUrl(vo.getThumPath()));
        }
    }

    private void setVoFileType(FileVo vo) {
        vo.setFileType(getFileType(vo.getSuffix()));
    }

    public Integer getFileType(String suffix) {
        if (FileUtil.isVideo(suffix)) {
            return FileType.VIDEO.getValue();
        }
        if (FileUtil.isAudio(suffix)) {
            return FileType.AUDIO.getValue();
        }
        if (FileUtil.isDocument(suffix)) {
            return FileType.DOCUMENT.getValue();
        }
        if (FileUtil.isImage(suffix)) {
            return FileType.IMAGE.getValue();
        }
        return FileType.OTHER.getValue();
    }

    public File getFileWithDel(Long id) {
        return fileMapper.getFileWithDel(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recycle(List<Long> idList, Long fileId) {
        File file = getAccessibleFileOrThrow(fileId);
        Assert.notNull(file, "文件异常，无法还原");
        long dirId = file.getDirId();
        long newDirId = dirId;
        if (dirId > 0) {
            File dirFile = getFileWithDel(dirId);
            if (dirFile != null && (dirFile.getDeleted() == 1 || Objects.equals(dirFile.getInTrash(), FileInTrash.YES.getValue()))) {
                newDirId = recycleDir(dirId);
                if (dirFile.getDirId() > 0) {
                    recycleDirEic(dirFile.getDirId(), newDirId);
                }
            }
        }
        LambdaUpdateWrapper<File> restoreWrapper = new LambdaUpdateWrapper<File>()
                .set(File::getInTrash, FileInTrash.NO.getValue())
                .in(File::getId, idList);
        if (shouldFilterByCurrentUser()) {
            restoreWrapper.eq(File::getUserId, currentUserService.getCurrentUserId());
        }
        update(restoreWrapper);
        update(new LambdaUpdateWrapper<File>().set(File::getDirId, newDirId).in(File::getId, fileId));
    }

    private void recycleDirEic(Long dirId, Long newDirId) {
        File parentFile = getFileWithDel(dirId);
        if (parentFile != null && (parentFile.getDeleted() == 1 || Objects.equals(parentFile.getInTrash(), FileInTrash.YES.getValue()))) {
            Long parentDirId = recycleDir(dirId);
            update(new LambdaUpdateWrapper<File>().set(File::getDirId, parentDirId).eq(File::getId, newDirId));
            recycleDirEic(parentFile.getId(), parentDirId);
        }
    }

    private Long recycleDir(Long dirId) {
        File dirFile = getFileWithDel(dirId);
        Assert.notNull(dirFile, "上级文件夹异常，无法还原");
        if (shouldFilterByCurrentUser()) {
            Assert.isTrue(Objects.equals(dirFile.getUserId(), currentUserService.getCurrentUserId()), ErrorCode.FILE_NOT_EXIST);
        }
        dirFile.setId(null);
        dirFile.setDeleted(0);
        dirFile.setInTrash(0);
        dirFile.setCreateTime(new Date());
        dirFile.setUpdateTime(new Date());
        save(dirFile);
        return dirFile.getId();
    }

    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        Long usedSize;
        Long fileCount;
        if (shouldFilterByCurrentUser()) {
            Long currentUserId = currentUserService.getCurrentUserId();
            usedSize = fileMapper.sumTotalFileSizeByUserId(currentUserId);
            fileCount = fileMapper.countFilesByUserId(currentUserId);
        } else {
            usedSize = fileMapper.sumTotalFileSize();
            fileCount = fileMapper.countFiles();
        }
        Long maxSize = 10L * 1024 * 1024 * 1024;
        stats.put("usedSize", usedSize);
        stats.put("fileCount", fileCount);
        stats.put("maxSize", maxSize);
        double usedPercent = maxSize > 0 ? (usedSize * 100.0 / maxSize) : 0;
        stats.put("usedPercent", Math.round(usedPercent * 100) / 100.0);
        stats.put("usedSizeFormat", formatFileSize(usedSize));
        stats.put("maxSizeFormat", formatFileSize(maxSize));
        return stats;
    }

    private String formatFileSize(Long size) {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        }
        if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
}
