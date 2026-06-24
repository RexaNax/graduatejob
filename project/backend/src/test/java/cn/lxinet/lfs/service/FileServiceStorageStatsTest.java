package cn.lxinet.lfs.service;

import cn.lxinet.lfs.mapper.FileMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileServiceStorageStatsTest {

    @Test
    void adminSeesGlobalStorageStats() {
        FileMapper fileMapper = mock(FileMapper.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        when(currentUserService.hasValidToken()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(true);
        when(fileMapper.sumTotalFileSize()).thenReturn(2048L);
        when(fileMapper.countFiles()).thenReturn(2L);

        FileService service = new FileService();
        setField(service, "fileMapper", fileMapper);
        setField(service, "currentUserService", currentUserService);

        Map<String, Object> stats = service.getStorageStats();

        assertEquals(2048L, stats.get("usedSize"));
        assertEquals(2L, stats.get("fileCount"));
    }

    @Test
    void demoUserSeesOwnStorageStatsOnly() {
        FileMapper fileMapper = mock(FileMapper.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        when(currentUserService.hasValidToken()).thenReturn(true);
        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(fileMapper.sumTotalFileSizeByUserId(2L)).thenReturn(1024L);
        when(fileMapper.countFilesByUserId(2L)).thenReturn(1L);

        FileService service = new FileService();
        setField(service, "fileMapper", fileMapper);
        setField(service, "currentUserService", currentUserService);

        Map<String, Object> stats = service.getStorageStats();

        assertEquals(1024L, stats.get("usedSize"));
        assertEquals(1L, stats.get("fileCount"));
        assertEquals("1.00 KB", stats.get("usedSizeFormat"));
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }
}
