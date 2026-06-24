CREATE TABLE IF NOT EXISTS lfs_file
(
    id           BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文件id',
    name         VARCHAR(255) DEFAULT '' COMMENT '文件名',
    is_dir       BIGINT(1)    DEFAULT '0' COMMENT '是否文件夹，0是文件，1是文件夹',
    dir_id       BIGINT(20)   DEFAULT '0' COMMENT '文件夹id',
    user_id      BIGINT(20)   DEFAULT '1' COMMENT '归属用户ID',
    file_size    BIGINT(20)   DEFAULT '0' COMMENT '文件大小，单位B',
    file_type    TINYINT(1)   DEFAULT '0' COMMENT '文件类型，0文件夹，1视频，2音频，3文档，4图片，5其他',
    md5          VARCHAR(32)  DEFAULT '' COMMENT '文件MD5',
    suffix       VARCHAR(32)  DEFAULT '' COMMENT '文件后缀',
    duration     BIGINT(20)   DEFAULT '0' COMMENT '音视频时长（秒）',
    pages        INT(11)      DEFAULT '0' COMMENT '文档页数',
    path         VARCHAR(255) DEFAULT '' COMMENT '文件相对路径',
    thum_path    VARCHAR(255) DEFAULT '' COMMENT '文件缩略图相对路径',
    trans_status TINYINT(1)   DEFAULT '0' COMMENT '文件转码状态',
    in_trash     TINYINT(1)   DEFAULT '0' COMMENT '是否在回收站',
    deleted      TINYINT(1)   DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time  DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time  DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_md5 (md5),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '文件表';
