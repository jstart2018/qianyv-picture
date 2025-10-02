package com.jstart.qypicture.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.jstart.qypicture.enums.ResultEnum;
import com.jstart.qypicture.result.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return Result.error(e.getCode(), e.getMessage());
    }

    //Preconditions参数校验产生的异常
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> businessExceptionHandler(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return Result.error(ResultEnum.PARAMS_ERROR, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return Result.error(ResultEnum.SYSTEM_ERROR);
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return Result.error(ResultEnum.NOT_LOGIN_ERROR, "未登录，请先登录");
    }

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public Result<?> notPermissionExceptionHandler(SaTokenException e) {
        log.error("SaTokenException", e);
        return Result.error(ResultEnum.NO_AUTH_ERROR, "无权限操作");
    }


}
