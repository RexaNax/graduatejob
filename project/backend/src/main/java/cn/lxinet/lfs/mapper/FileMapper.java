package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

@Mapper
public interface FileMapper extends BaseMapper<File> {

    @Update("update lfs_file set trans_status = #{transStatus}, update_time = #{uptime} where id = #{id}")
    void updateTrans(@Param("id") Long id, @Param("transStatus") Integer transStatus, @Param("uptime") Date uptime);

    @Update("update lfs_file set md5 = #{md5}, update_time = #{uptime} where id = #{id}")
    void updateMd5(@Param("id") Long id, @Param("md5") String md5, @Param("uptime") Date uptime);

    @Select("select * from lfs_file where id = #{id}")
    File getFileWithDel(@Param("id") Long id);

    @Select("SELECT COALESCE(SUM(file_size), 0) FROM lfs_file WHERE in_trash = 0 AND deleted = 0 AND is_dir = 0")
    Long sumTotalFileSize();

    @Select("SELECT COALESCE(SUM(file_size), 0) FROM lfs_file WHERE in_trash = 0 AND deleted = 0 AND is_dir = 0 AND user_id = #{userId}")
    Long sumTotalFileSizeByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM lfs_file WHERE in_trash = 0 AND deleted = 0 AND is_dir = 0")
    Long countFiles();

    @Select("SELECT COUNT(*) FROM lfs_file WHERE in_trash = 0 AND deleted = 0 AND is_dir = 0 AND user_id = #{userId}")
    Long countFilesByUserId(@Param("userId") Long userId);
}
