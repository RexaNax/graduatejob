package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.service.FileService;
import cn.lxinet.lfs.service.FileThumService;
import cn.lxinet.lfs.service.FileTrashService;
import cn.lxinet.lfs.utils.MinioUtil;
import cn.lxinet.lfs.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 回收站控制器
 *
 * @author zcx
 * @date 2024/03/16
 */
@RestController
@RequestMapping("/trash")
public class TrashController extends BaseController{
    @Autowired
    private FileTrashService fileTrashService;

    @GetMapping("/list")
    public Result list(){
        Page<FileTrashVo> page = fileTrashService.listByPage(getPageNo(), getPageSize());
        return Result.success(page);
    }
    /**
     * 删除文件/文件夹
     *
     * @param ids
     * @return {@link Result}
     */
    @PostMapping("/delete")
    public Result delete(String ids){
        fileTrashService.delete(Arrays.asList(ids.split(",")));
        return Result.success();
    }

    /**
     * 回收文件/文件夹
     *
     * @param ids
     * @return {@link Result}
     */
    @PostMapping("/recycle")
    public Result recycle(String ids){
        fileTrashService.recycle(Arrays.asList(ids.split(",")));
        return Result.success();
    }

    /**
     * 清空回收站
     * 
     * @return {@link Result}
     */
    @PostMapping("/clear")
    public Result clear(){
        fileTrashService.clearAll();
        return Result.success();
    }


}
