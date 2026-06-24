package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.exception.BaseException;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.service.CurrentUserService;
import cn.lxinet.lfs.vo.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

public class BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    @Resource
    protected HttpServletRequest request;

    @Resource
    protected HttpServletResponse response;

    @Resource
    protected CurrentUserService currentUserService;

    protected Long getPageNo() {
        String pageNoStr = getParam("pageNo");
        Long pageNo;
        try {
            pageNo = Long.parseLong(pageNoStr);
        } catch (Exception e) {
            pageNo = 1L;
        }
        return pageNo;
    }

    protected Long getPageSize() {
        String pageSizeStr = getParam("pageSize");
        Long pageSize;
        try {
            pageSize = Long.parseLong(pageSizeStr);
        } catch (Exception e) {
            pageSize = 10L;
        }
        return pageSize;
    }

    protected String getParam(String name) {
        return request.getParameter(name);
    }

    protected String getParam(String name, String defaultValue) {
        String value = getParam(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    protected Long getCurrentUserId() {
        return currentUserService.getCurrentUserId();
    }

    protected boolean currentUserIsAdmin() {
        return currentUserService.isAdmin();
    }

    protected String getErrorMessages(BindingResult result) {
        List<FieldError> list = result.getFieldErrors();
        if (list.size() > 0) {
            return list.get(0).getDefaultMessage();
        }
        return "";
    }

    protected boolean isAjaxRequest() {
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(header);
    }

    @ExceptionHandler
    public Result execptionHandler(Exception e) {
        Result<?> result = new Result();
        result.setCode(ErrorCode.SYSTEM_EXCEPTION.getCode());
        result.setMsg(e.getMessage() == null ? "系统异常: " + e : e.getMessage());
        LOGGER.error("系统异常", e);
        return result;
    }

    @ExceptionHandler
    public Result execptionHandler(BaseException e) {
        Result<?> result = new Result();
        if (e.getCode() == ErrorCode.PARAM_ERROR.getCode()) {
            result.setMsg(e.getMsg() != null ? e.getMsg() : ErrorCode.PARAM_ERROR.getMsg());
            result.setCode(ErrorCode.PARAM_ERROR.getCode());
        } else {
            result.setMsg(e.getMsg() != null ? e.getMsg() : ErrorCode.SYSTEM_EXCEPTION.getMsg());
            result.setCode(e.getCode() != null ? e.getCode() : ErrorCode.SYSTEM_EXCEPTION.getCode());
        }
        return result;
    }

    @ExceptionHandler
    public Result execptionHandler(BindException e) {
        Result<?> result = new Result();
        result.setMsg(getErrorMessages(e.getBindingResult()));
        result.setCode(ErrorCode.PARAM_ERROR.getCode());
        return result;
    }

    @ExceptionHandler
    public Result execptionHandler(MethodArgumentNotValidException e) {
        Result<?> result = new Result();
        result.setMsg(getErrorMessages(e.getBindingResult()));
        result.setCode(ErrorCode.PARAM_ERROR.getCode());
        return result;
    }
}
