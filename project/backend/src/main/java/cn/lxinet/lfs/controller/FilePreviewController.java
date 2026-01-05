package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.utils.Base64Util;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件预览控制器
 * 
 * 【原理】
 * 将静态文件服务集成到 Spring Boot 中，不再需要单独启动 8918 端口的静态文件服务。
 * 支持防盗链验证，确保文件访问安全。
 * 
 * @author zcx
 * @date 2024/01/05
 */
@RestController
public class FilePreviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePreviewController.class);

    @Value("${config.file-server.local.file-dir}")
    private String localFileDir;

    @Value("${config.file-server.local.secret}")
    private String secret;

    @Value("${config.file-server.local.st-effective-time}")
    private Integer stEffectiveTime;

    /**
     * 文件类型与 MIME 类型映射
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        // 视频
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("avi", "video/x-msvideo");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("mkv", "video/x-matroska");
        MIME_TYPES.put("m3u8", "application/vnd.apple.mpegurl");
        MIME_TYPES.put("ts", "video/mp2t");
        // 音频
        MIME_TYPES.put("mp3", "audio/mpeg");
        MIME_TYPES.put("wav", "audio/wav");
        MIME_TYPES.put("ogg", "audio/ogg");
        MIME_TYPES.put("flac", "audio/flac");
        MIME_TYPES.put("aac", "audio/aac");
        // 图片
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("bmp", "image/bmp");
        MIME_TYPES.put("svg", "image/svg+xml");
        // 文档
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        // 其他
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("rar", "application/x-rar-compressed");
    }

    /**
     * 文件预览/下载接口
     * 
     * 路径格式: /files/xxx.mp4?secret=xxx&expire=xxx
     *          /thum/xxx/1.jpg?secret=xxx&expire=xxx
     * 
     * @param secret 防盗链签名
     * @param expire 过期时间戳
     * @param oper 操作类型，down 表示下载
     * @param filename 下载时的文件名
     */
    @GetMapping(value = {"/files/**", "/thum/**", "/trans/**"})
    public ResponseEntity<Resource> preview(
            @RequestParam(required = false) String secret,
            @RequestParam(required = false) Long expire,
            @RequestParam(required = false) String oper,
            @RequestParam(required = false) String filename,
            HttpServletRequest request) {
        
        // 获取请求路径
        String requestUri = request.getRequestURI();
        String path = requestUri;
        
        LOGGER.debug("文件预览请求: path={}, secret={}, expire={}", path, secret, expire);

        // 验证防盗链
        if (!validateSafetychain(path, secret, expire)) {
            LOGGER.warn("防盗链验证失败: path={}", path);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 构建文件路径
        File file = new File(localFileDir + path);
        if (!file.exists() || !file.isFile()) {
            LOGGER.warn("文件不存在: {}", file.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        // 安全检查：防止路径穿越
        try {
            String canonicalPath = file.getCanonicalPath();
            String basePath = new File(localFileDir).getCanonicalPath();
            if (!canonicalPath.startsWith(basePath)) {
                LOGGER.warn("路径穿越攻击: {}", path);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            LOGGER.error("路径检查异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 获取文件扩展名和 MIME 类型
        String extension = getFileExtension(file.getName()).toLowerCase();
        String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

        // 构建响应
        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        
        // 支持 Range 请求（视频拖动进度）
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        
        // CORS 支持
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");

        // 下载模式
        if ("down".equals(oper) && StringUtils.isNotBlank(filename)) {
            try {
                String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                        .replace("+", "%20");
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            } catch (Exception e) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            }
        } else {
            // 内联显示
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline");
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    /**
     * 验证防盗链
     */
    private boolean validateSafetychain(String path, String secretParam, Long expire) {
        if (StringUtils.isBlank(secretParam) || expire == null) {
            return false;
        }

        // 检查是否过期
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime > expire) {
            LOGGER.debug("链接已过期: currentTime={}, expire={}", currentTime, expire);
            return false;
        }

        // 验证签名
        try {
            String expectedSecret = Base64Util.encode(DigestUtils.md5(this.secret + path + expire))
                    .replace("=", "");
            boolean valid = expectedSecret.equals(secretParam);
            if (!valid) {
                LOGGER.debug("签名不匹配: expected={}, actual={}", expectedSecret, secretParam);
            }
            return valid;
        } catch (Exception e) {
            LOGGER.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }
}
