package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();
        Map<String, String> map = new HashMap<>();
        // 获取校验的错误
        result.getFieldErrors().forEach(item -> {
            // 获取发生错误的message
            String message = item.getDefaultMessage();
            // 获取发生错误的字段
            String field = item.getField();
            map.put(field, message);
        });
        log.error("数据校验出现问题{},异常类型{}", exception.getMessage(), exception.getClass());
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleAllException(Throwable throwable) {
        log.error("未知异常{},异常类型{}", throwable.getMessage(), throwable.getClass());
        throwable.printStackTrace();
        return R.error(BizCodeEnum.UNKNOW_EXEPTION.getCode(), BizCodeEnum.UNKNOW_EXEPTION.getMsg());
    }
}
