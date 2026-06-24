package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileShare;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileShareServiceTest {

    @Test
    void createShareRequiresAccessibleFileAndPersistsShare() {
        FileService fileService = mock(FileService.class);
        File file = new File();
        file.setId(11L);
        file.setName("cover.png");
        file.setIsDir(0);
        when(fileService.requireAccessibleFile(11L)).thenReturn(file);

        FileShareService service = spy(new FileShareService());
        setField(service, "fileService", fileService);
        doReturn(0L).when(service).count(any());
        doReturn(true).when(service).save(any(FileShare.class));

        long before = System.currentTimeMillis();
        Map<String, Object> result = service.createShare(11L, 7);
        long after = System.currentTimeMillis();

        assertEquals("cover.png", result.get("fileName"));
        assertTrue(((String) result.get("shareCode")).length() == 8);
        long expireTime = ((Number) result.get("expireTime")).longValue();
        assertTrue(expireTime >= before + 7L * 24 * 60 * 60 * 1000);
        assertTrue(expireTime <= after + 7L * 24 * 60 * 60 * 1000 + 2000);

        ArgumentCaptor<FileShare> shareCaptor = ArgumentCaptor.forClass(FileShare.class);
        verify(service).save(shareCaptor.capture());
        assertEquals(11L, shareCaptor.getValue().getFileId());
        assertNotEquals("", shareCaptor.getValue().getShareCode());
        verify(fileService).requireAccessibleFile(11L);
    }

    @Test
    void listAndCancelShareCheckFileOwnership() {
        FileService fileService = mock(FileService.class);
        File file = new File();
        file.setId(15L);
        file.setIsDir(0);
        when(fileService.requireAccessibleFile(15L)).thenReturn(file);

        FileShare share = new FileShare();
        share.setId(9L);
        share.setFileId(15L);

        FileShareService service = spy(new FileShareService());
        setField(service, "fileService", fileService);
        doReturn(List.of(share)).when(service).list((Wrapper<FileShare>) any());
        doReturn(share).when(service).getById(9L);
        doReturn(true).when(service).removeById(anyLong());

        List<FileShare> shares = service.getSharesByFileId(15L);
        service.cancelShare(9L);

        assertEquals(1, shares.size());
        assertEquals(9L, shares.get(0).getId());
        verify(fileService, times(2)).requireAccessibleFile(15L);
        verify(service).removeById(9L);
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
