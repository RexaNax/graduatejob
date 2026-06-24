package cn.lxinet.lfs.config;

import cn.lxinet.lfs.interceptor.GlobalInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 配置
 *
 * 讲解重点：
 * addInterceptors: 配置拦截器
 * excludePathPatterns: 排除不需要认证的路径
 *
 * 登录、分享访问等接口不需要 Token。
 * register 仍保留在白名单中，但由控制器直接拒绝自助注册请求。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> patterns = new ArrayList<>();
        patterns.add("/getToken");
        patterns.add("/user/login");
        // 保留注册白名单，交由控制器统一返回“当前系统仅开放管理员账号访问”。
        patterns.add("/user/register");
        patterns.add("/share/access/**");
        // 文件预览依赖签名校验，不走 Token 拦截。
        patterns.add("/files/**");
        patterns.add("/thum/**");
        patterns.add("/trans/**");

        registry.addInterceptor(new GlobalInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(patterns);
    }
}
