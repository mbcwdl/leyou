package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author guanlibin
 * @version 1.0
 * @create 2020/9/18 20:42
 */
@AllArgsConstructor
@Getter
public class LyException extends RuntimeException{
    private ExceptionEnum exceptionEnum;
}
