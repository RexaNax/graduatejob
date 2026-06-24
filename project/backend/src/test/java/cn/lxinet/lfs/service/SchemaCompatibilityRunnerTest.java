package cn.lxinet.lfs.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchemaCompatibilityRunnerTest {

    @Test
    void addsOwnershipColumnIndexAndDemoUserWhenMissing() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(eq(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?"
        ), eq(Integer.class), eq("lfs_file"), eq("user_id"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(eq(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?"
        ), eq(Integer.class), eq("lfs_file"), eq("idx_user_id"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM sys_user WHERE username = ?"),
                eq(Integer.class),
                eq("demo")
        )).thenReturn(0);

        SchemaCompatibilityRunner runner = new SchemaCompatibilityRunner(jdbcTemplate);

        runner.run(null);

        verify(jdbcTemplate).execute("ALTER TABLE lfs_file ADD COLUMN user_id BIGINT(20) DEFAULT 1 COMMENT '归属用户ID'");
        verify(jdbcTemplate).update("UPDATE lfs_file SET user_id = 1 WHERE user_id IS NULL");
        verify(jdbcTemplate).execute("CREATE INDEX idx_user_id ON lfs_file(user_id)");
        verify(jdbcTemplate).update(
                "INSERT INTO sys_user(username, password, nickname, status) VALUES (?, ?, ?, 1)",
                "demo",
                SchemaCompatibilityRunner.DEMO_PASSWORD_HASH,
                "演示用户"
        );
    }

    @Test
    void onlyBackfillsNullOwnershipWhenSchemaAlreadyExists() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("lfs_file"), eq("user_id"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("lfs_file"), eq("idx_user_id"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(
                eq("SELECT COUNT(*) FROM sys_user WHERE username = ?"),
                eq(Integer.class),
                eq("demo")
        )).thenReturn(1);

        SchemaCompatibilityRunner runner = new SchemaCompatibilityRunner(jdbcTemplate);

        runner.run(null);

        verify(jdbcTemplate, never()).execute("ALTER TABLE lfs_file ADD COLUMN user_id BIGINT(20) DEFAULT 1 COMMENT '归属用户ID'");
        verify(jdbcTemplate).update("UPDATE lfs_file SET user_id = 1 WHERE user_id IS NULL");
        verify(jdbcTemplate, never()).execute("CREATE INDEX idx_user_id ON lfs_file(user_id)");
        verify(jdbcTemplate, never()).update(
                "INSERT INTO sys_user(username, password, nickname, status) VALUES (?, ?, ?, 1)",
                "demo",
                SchemaCompatibilityRunner.DEMO_PASSWORD_HASH,
                "演示用户"
        );
    }
}
