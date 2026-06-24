package cn.lxinet.lfs.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilePreviewControllerTest {

    @Test
    void normalizeRequestPathStripsApiPrefixForPreviewRoutes() {
        assertEquals("/files/demo.png", FilePreviewController.normalizeRequestPath("/api/files/demo.png"));
        assertEquals("/thum/abc/1.jpg", FilePreviewController.normalizeRequestPath("/api/thum/abc/1.jpg"));
        assertEquals("/trans/video/demo.mp4", FilePreviewController.normalizeRequestPath("/api/trans/video/demo.mp4"));
    }

    @Test
    void normalizeRequestPathKeepsDirectPreviewRoutes() {
        assertEquals("/files/demo.png", FilePreviewController.normalizeRequestPath("/files/demo.png"));
        assertEquals("/thum/abc/1.jpg", FilePreviewController.normalizeRequestPath("/thum/abc/1.jpg"));
        assertEquals("/trans/video/demo.mp4", FilePreviewController.normalizeRequestPath("/trans/video/demo.mp4"));
    }
}
