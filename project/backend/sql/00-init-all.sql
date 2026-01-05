-- ============================================
-- Docker 初始化脚本 - 完整数据库初始化
-- 文件名以 00 开头确保最先执行
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ========== 1. 基础文件表 ==========
CREATE TABLE IF NOT EXISTS lfs_file
(
    id           BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文件id',
    name         VARCHAR(255) DEFAULT '' COMMENT '文件名',
    is_dir       BIGINT(1)    DEFAULT '0' COMMENT '是否文件夹，0是文件，1是文件夹',
    dir_id       BIGINT(20)   DEFAULT '0' COMMENT '文件夹id',
    file_size    BIGINT(20)   DEFAULT '0' COMMENT '文件大小，单位B',
    file_type    TINYINT(1)   DEFAULT '0' COMMENT '文件类型，0文件夹，1视频，2音频，3文档，4图片，9其他',
    md5          VARCHAR(32)  DEFAULT '' COMMENT '文件MD5',
    suffix       VARCHAR(32)  DEFAULT '' COMMENT '文件后缀',
    duration     BIGINT(20)   DEFAULT '0' COMMENT '音视频时长（秒）',
    pages        INT(11)      DEFAULT '0' COMMENT '文档页数',
    path         VARCHAR(255) DEFAULT '' COMMENT '文件相对路径',
    thum_path    VARCHAR(255) DEFAULT '' COMMENT '文件缩略图相对路径',
    trans_status TINYINT(1)   DEFAULT '0' COMMENT '文件转码状态，0 正在转码，1 转码成功，2 部分转码成功，3 转码失败，4 不需要转码不需要转码，5 不支持转码，6 取消转码',
    in_trash     TINYINT(1)   DEFAULT '0' COMMENT '是否在回收站，0表示未进入回收站，1表示进入回收站',
    deleted      TINYINT(1)   DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time  DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time  DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_md5 (md5)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '文件表';

-- ========== 2. 回收站表 ==========
CREATE TABLE IF NOT EXISTS lfs_file_trash
(
    id           BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '回收站id',
    file_id      BIGINT(20)  DEFAULT '0' COMMENT '原文件id',
    retain_days  INT(11)     DEFAULT '0' COMMENT '保留天数',
    expire_time  BIGINT(20)  DEFAULT '0' COMMENT '过期时间，过期后删除文件',
    recycle_time BIGINT(20)  DEFAULT '0' COMMENT '回收时间，过期前回收文件',
    deleted      TINYINT(1)  DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time  DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time  DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_file_id (file_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '文件回收站记录';

CREATE TABLE IF NOT EXISTS lfs_file_trash_detail
(
    id           BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '回收站明细id',
    trash_id     BIGINT(20)  DEFAULT '0' COMMENT '回收站id',
    file_id      BIGINT(20)  DEFAULT '0' COMMENT '原文件id',
    deleted      TINYINT(1)  DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time  DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time  DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_trash_id (trash_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '文件回收站明细记录';

-- ========== 3. 转码相关表 ==========
CREATE TABLE IF NOT EXISTS lfs_trans_file
(
    id          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '转码文件id',
    file_id     BIGINT(20)   DEFAULT '0' COMMENT '原文件id',
    file_size   BIGINT(20)   DEFAULT '0' COMMENT '文件大小，单位b',
    md5         VARCHAR(32)  DEFAULT '' COMMENT '文件md5',
    suffix      VARCHAR(32)  DEFAULT '' COMMENT '文件后缀',
    duration    BIGINT(20)   DEFAULT '0' COMMENT '音视频时长（秒）',
    pages       INT(11)      DEFAULT '0' COMMENT '文档页数',
    path        VARCHAR(255) DEFAULT '' COMMENT '文件相对路径',
    deleted     TINYINT(1)   DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_file_id (file_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '转码文件表';

CREATE TABLE IF NOT EXISTS lfs_file_thum
(
    id          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '转码文件id',
    file_md5    VARCHAR(32)  DEFAULT '' COMMENT '文件md5',
    path        VARCHAR(255) DEFAULT '' COMMENT '文件相对路径',
    duration    BIGINT(20)   DEFAULT '0' COMMENT '音视频所在时长（秒）',
    pages       INT(11)      DEFAULT '0' COMMENT '文档所在页数',
    deleted     TINYINT(1)   DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_file_md5 (file_md5)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '缩略图表';

CREATE TABLE IF NOT EXISTS lfs_trans_template
(
    id                BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '转码模板id',
    name              VARCHAR(255) DEFAULT '' COMMENT '模板名称',
    width             INT(11)      DEFAULT '0' COMMENT '视频分辨率宽度，0为自动计算',
    height            INT(11)      DEFAULT '1080' COMMENT '视频分辨率高度，0为自动计算',
    format            VARCHAR(4)   DEFAULT 'mp4' COMMENT '转码输出格式',
    frame_rate        INT(11)      DEFAULT '30' COMMENT '视频帧率',
    bit_rate          INT(11)      DEFAULT '1200' COMMENT '视频比特率(kbps)',
    codec             VARCHAR(10)  DEFAULT 'h264' COMMENT '编解码器',
    audio_codec       VARCHAR(10)  DEFAULT 'aac' COMMENT '音频编解码器',
    audio_channel     TINYINT(1)   DEFAULT '2' COMMENT '音频声道',
    audio_bit_rate    INT(11)      DEFAULT '128' COMMENT '音频比特率(kbps)',
    audio_sample_rate INT(11)      DEFAULT '48000' COMMENT '音频采样率',
    status            TINYINT(1)   DEFAULT '0' COMMENT '状态，是否开启转码，0 关闭，1 开启',
    deleted           TINYINT(1)   DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time       DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time       DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '转码模板表';

CREATE TABLE IF NOT EXISTS lfs_trans_progress
(
    id            BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '转码进度id',
    file_id       BIGINT(20)    DEFAULT '0' COMMENT '转码的文件id',
    file_trans_id BIGINT(20)    DEFAULT '0' COMMENT '转码后的文件id',
    format        VARCHAR(4)    DEFAULT 'mp4' COMMENT '转码格式',
    progress      DOUBLE(11, 1) DEFAULT '0.0' COMMENT '转码进度，0-100(%)',
    trans_status  TINYINT(1)    DEFAULT '0' COMMENT '文件转码状态，0 正在转码，1 转码成功，2 部分转码成功，3 转码失败，4 不需要转码，5 不支持转码，6 取消转码',
    start_time    BIGINT(20)    DEFAULT '0' COMMENT '转码开始时间（时间戳）',
    end_time      BIGINT(20)    DEFAULT '0' COMMENT '转码结束时间（时间戳）',
    message       VARCHAR(255)  DEFAULT '' COMMENT '转码信息，如异常消息',
    deleted       TINYINT(1)    DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time   DATETIME(3)   DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time   DATETIME(3)   DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_file_id (file_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 COMMENT = '转码进度表';

-- ========== 4. 用户表（新增） ==========
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名（登录账号）',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(50)  DEFAULT '' COMMENT '昵称',
    avatar      VARCHAR(255) DEFAULT '' COMMENT '头像URL',
    status      TINYINT(1)   DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='用户表';

-- 插入默认管理员用户 (密码: 123456)
INSERT INTO sys_user (username, password, nickname, status) VALUES 
('admin', '$2a$10$C5LAWEU.5HoX91ve2D7Iv.kJGlJuWt5P.on5sgCuIttsMasWe5ftu', '管理员', 1)
ON DUPLICATE KEY UPDATE username=username;

-- ========== 5. 文件分享表（新增） ==========
CREATE TABLE IF NOT EXISTS lfs_file_share (
    id          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '分享ID',
    file_id     BIGINT(20)   NOT NULL COMMENT '文件ID',
    share_code  VARCHAR(32)  NOT NULL COMMENT '分享码（唯一）',
    expire_time BIGINT(20)   DEFAULT 0 COMMENT '过期时间戳（毫秒），0表示永不过期',
    view_count  INT(11)      DEFAULT 0 COMMENT '访问次数',
    create_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_time DATETIME(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_share_code (share_code),
    KEY idx_file_id (file_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='文件分享表';

-- ========== 完成 ==========
SELECT 'Database initialization completed!' AS message;

