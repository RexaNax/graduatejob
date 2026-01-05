package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.FileShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 文件分享 Mapper
 */
@Mapper
public interface FileShareMapper extends BaseMapper<FileShare> {
    
    /**
     * 增加访问次数
     */
    @Update("UPDATE lfs_file_share SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
}
