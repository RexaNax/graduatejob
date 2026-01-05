package cn.lxinet.lfs.config;

import cn.lxinet.lfs.interceptor.GlobalInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 配置
 * 
 * 【原理讲解】
 * addInterceptors: 配置拦截器
 * excludePathPatterns: 排除不需要认证的路径
 * 
 * 登录、注册、分享访问等接口不需要 Token
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> patterns = new ArrayList<>();
        // 原有的 Token 获取接口
        patterns.add("/getToken");
        // 用户登录、注册接口（不需要认证）
        patterns.add("/user/login");
        patterns.add("/user/register");
        // 分享链接访问接口（不需要认证）
        patterns.add("/share/access/**");
        // 文件预览接口（使用防盗链验证，不需要 Token）
        patterns.add("/files/**");
        patterns.add("/thum/**");
        patterns.add("/trans/**");
        
        registry.addInterceptor(new GlobalInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(patterns);
    }

}
