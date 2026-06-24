package cn.lxinet.lfs.service;

import cn.lxinet.lfs.convert.FileTrashConvert;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileTrash;
import cn.lxinet.lfs.entity.FileTrashDetail;
import cn.lxinet.lfs.mapper.FileTrashMapper;
import cn.lxinet.lfs.vo.FileTrashVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileTrashService extends ServiceImpl<FileTrashMapper, FileTrash> {
    @Autowired
    private FileTrashMapper fileTrashMapper;
    @Autowired
    @Lazy
    private FileService fileService;
    @Autowired
    private FileTrashDetailService fileTrashDetailService;
    @Autowired
    private CurrentUserService currentUserService;

    public Page<FileTrashVo> listByPage(long current, long size) {
        Page<FileTrash> page = page(new Page<>(current, size), new LambdaQueryWrapper<FileTrash>().orderByDesc(FileTrash::getId));
        List<FileTrash> visibleTrash = page.getRecords().stream()
                .filter(this::canAccessTrash)
                .collect(Collectors.toList());
        Page<FileTrashVo> voPage = new Page<>(current, size, visibleTrash.size());
        if (visibleTrash.isEmpty()) {
            return voPage;
        }
        List<FileTrashVo> voList = FileTrashConvert.INSTANCE.toVoList(visibleTrash);
        List<Long> fileIds = voList.stream().map(FileTrashVo::getFileId).collect(Collectors.toList());
        List<File> fileList = fileService.list(new LambdaQueryWrapper<File>()
                .select(File::getId, File::getName, File::getIsDir, File::getFileSize, File::getFileType)
                .in(File::getId, fileIds));
        Map<Long, File> fileMap = new HashMap<>();
        fileList.forEach(file -> fileMap.put(file.getId(), file));
        voList.removeIf(vo -> !fileMap.containsKey(vo.getFileId()));
        voList.forEach(vo -> {
            File file = fileMap.get(vo.getFileId());
            vo.setFileName(file.getName());
            vo.setFileSize(file.getFileSize());
            vo.setIsDir(file.getIsDir());
            vo.setFileType(file.getFileType());
        });
        voPage.setTotal(voList.size());
        voPage.setRecords(voList);
        return voPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        List<FileTrash> list = list(new LambdaQueryWrapper<FileTrash>().in(FileTrash::getId, ids)).stream()
                .filter(this::canAccessTrash)
                .collect(Collectors.toList());
        list.forEach(trash -> {
            LambdaQueryWrapper<FileTrashDetail> wrapper = new LambdaQueryWrapper<FileTrashDetail>().eq(FileTrashDetail::getTrashId, trash.getId());
            List<FileTrashDetail> detailList = fileTrashDetailService.list(wrapper);
            List<Long> fileIds = detailList.stream().map(FileTrashDetail::getFileId).collect(Collectors.toList());
            fileService.delete(fileIds);
            fileTrashDetailService.delete(detailList.stream().map(FileTrashDetail::getId).collect(Collectors.toList()));
        });
        if (list.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<FileTrash> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(FileTrash::getDeleted, 1).in(FileTrash::getId, list.stream().map(FileTrash::getId).collect(Collectors.toList()));
        update(updateWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recycle(List<String> ids) {
        List<FileTrash> list = list(new LambdaQueryWrapper<FileTrash>().in(FileTrash::getId, ids)).stream()
                .filter(this::canAccessTrash)
                .collect(Collectors.toList());
        list.forEach(trash -> {
            LambdaQueryWrapper<FileTrashDetail> wrapper = new LambdaQueryWrapper<FileTrashDetail>().eq(FileTrashDetail::getTrashId, trash.getId());
            List<FileTrashDetail> detailList = fileTrashDetailService.list(wrapper);
            List<Long> fileIds = detailList.stream().map(FileTrashDetail::getFileId).collect(Collectors.toList());
            fileService.recycle(fileIds, trash.getFileId());
            fileTrashDetailService.delete(detailList.stream().map(FileTrashDetail::getId).collect(Collectors.toList()));
            LambdaUpdateWrapper<FileTrashDetail> updateDetailWrapper = new LambdaUpdateWrapper<>();
            updateDetailWrapper.set(FileTrashDetail::getDeleted, 1)
                    .in(FileTrashDetail::getId, detailList.stream().map(FileTrashDetail::getId).collect(Collectors.toList()));
            fileTrashDetailService.update(updateDetailWrapper);
        });
        if (list.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<FileTrash> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(FileTrash::getDeleted, 1).in(FileTrash::getId, list.stream().map(FileTrash::getId).collect(Collectors.toList()));
        update(updateWrapper);
    }

    public List<FileTrash> getExpireList() {
        return list(new LambdaQueryWrapper<FileTrash>().le(FileTrash::getExpireTime, System.currentTimeMillis()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearAll() {
        List<FileTrash> list = list(new LambdaQueryWrapper<FileTrash>().eq(FileTrash::getDeleted, 0)).stream()
                .filter(this::canAccessTrash)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return;
        }
        List<String> ids = list.stream().map(t -> String.valueOf(t.getId())).collect(Collectors.toList());
        delete(ids);
    }

    private boolean canAccessTrash(FileTrash trash) {
        if (!currentUserService.hasValidToken() || currentUserService.isAdmin()) {
            return true;
        }
        File file = fileService.getFileWithDel(trash.getFileId());
        return file != null && file.getUserId() != null && file.getUserId().equals(currentUserService.getCurrentUserId());
    }
}
