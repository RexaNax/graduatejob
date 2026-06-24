package cn.lxinet.lfs.interceptor;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.SpringContextUtil;
import cn.lxinet.lfs.vo.Result;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class GlobalInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        JwtConfig jwtConfig = SpringContextUtil.getBean(JwtConfig.class);
        String token = request.getHeader("token");

        if (!jwtConfig.parseToken(token)) {
            writeError(response, ErrorCode.TOKEN_INVALID);
            return false;
        }
        return true;
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(new Result(errorCode.getCode(), errorCode.getMsg())));
        out.flush();
        out.close();
    }
}
