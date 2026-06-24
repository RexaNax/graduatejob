package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("lfs_file")
@JsonIgnoreProperties({"updateTime", "deleted"})
public class File implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Integer isDir;
    private Long dirId;
    private Long userId;
    private Long fileSize;
    private Integer fileType;
    private String suffix;
    private String md5;
    private Long duration;
    private String path;
    private Integer transStatus;
    private Integer inTrash;
    private String thumPath;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public File() {
    }

    public File(String name, String suffix, String path, Integer transStatus) {
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.transStatus = transStatus;
    }

    public File(Long dirId, Long userId, String md5, String name, Long fileSize, String suffix, String path,
                Long duration, Integer transStatus, Integer fileType) {
        this.dirId = dirId;
        this.userId = userId;
        this.md5 = md5;
        this.name = name;
        this.fileSize = fileSize;
        this.suffix = suffix;
        this.path = path;
        this.duration = duration;
        this.transStatus = transStatus;
        this.fileType = fileType;
    }
}
