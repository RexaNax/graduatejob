-- ============================================
-- 用户表 SQL
-- 功能：简单用户登录系统
-- 
-- 【原理讲解】
-- 1. password 字段存储 BCrypt 加密后的密码
--    BCrypt 是单向加密，无法解密，只能比对
-- 2. status 字段控制用户是否可用
-- 3. 默认创建 admin 用户，密码 123456
-- ============================================

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

-- 插入默认管理员用户
-- 密码 123456 的 BCrypt 加密值
INSERT INTO sys_user (username, password, nickname, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1);
