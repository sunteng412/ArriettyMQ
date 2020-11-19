package com.mrfox.arrirtty.common.exception;

/***********************
 * @author MrFox
 * @date 2020/9/13 17:12
 * @version 1.0
 * @description
 ************************/
public class CustomBusinessException extends RuntimeException{

    private final Integer code;

    private final String errorMessage;

    public CustomBusinessException(Integer code, String errorMessage) {
        super(errorMessage);
        this.code = code;
        this.errorMessage = errorMessage;
    }

    public CustomBusinessException(String errorMessage) {
        super(errorMessage);
        this.code = -1;
        this.errorMessage = errorMessage;
    }

    public CustomBusinessException(CustomBusinessException exception) {
        super(exception.getMessage());
        this.code = exception.getCode();
        this.errorMessage = exception.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
