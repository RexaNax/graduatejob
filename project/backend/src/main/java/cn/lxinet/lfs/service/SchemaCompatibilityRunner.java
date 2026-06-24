package cn.lxinet.lfs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaCompatibilityRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaCompatibilityRunner.class);

    public static final String DEMO_PASSWORD_HASH = "$2a$10$C5LAWEU.5HoX91ve2D7Iv.kJGlJuWt5P.on5sgCuIttsMasWe5ftu";

    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean hasOwnershipColumn = existsColumn("lfs_file", "user_id");
        if (!hasOwnershipColumn) {
            LOGGER.info("Schema compatibility: add lfs_file.user_id");
            jdbcTemplate.execute("ALTER TABLE lfs_file ADD COLUMN user_id BIGINT(20) DEFAULT 1 COMMENT '归属用户ID'");
        }
        jdbcTemplate.update("UPDATE lfs_file SET user_id = 1 WHERE user_id IS NULL");
        if (!existsIndex("lfs_file", "idx_user_id")) {
            LOGGER.info("Schema compatibility: create idx_user_id");
            jdbcTemplate.execute("CREATE INDEX idx_user_id ON lfs_file(user_id)");
        }
        ensureDemoUser();
    }

    private void ensureDemoUser() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Integer.class,
                "demo"
        );
        if (count != null && count > 0) {
            return;
        }
        LOGGER.info("Schema compatibility: seed demo user");
        jdbcTemplate.update(
                "INSERT INTO sys_user(username, password, nickname, status) VALUES (?, ?, ?, 1)",
                "demo",
                DEMO_PASSWORD_HASH,
                "演示用户"
        );
    }

    private boolean existsColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private boolean existsIndex(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                tableName,
                indexName
        );
        return count != null && count > 0;
    }
}
