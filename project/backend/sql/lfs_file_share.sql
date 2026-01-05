-- ============================================
-- 文件分享表 SQL
-- 功能：生成文件分享链接
-- 
-- 【原理讲解】
-- 1. share_code: 8位随机字符串，作为分享链接的唯一标识
-- 2. expire_time: 过期时间戳，0表示永不过期
-- 3. view_count: 访问次数统计
-- ============================================

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
