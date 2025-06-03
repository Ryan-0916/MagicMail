package com.magicrealms.magicmail.api.exception;

/**
 * @author Ryan-0916
 * @Desc 领取失败异常
 * @date 2025-05-13
 */
@SuppressWarnings("unused")
public class ReceiveException extends RuntimeException {

    public ReceiveException() {
        super();
    }

    public ReceiveException(String message) {
        super(message);
    }

    public ReceiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReceiveException(Throwable cause) {
        super(cause);
    }

}
