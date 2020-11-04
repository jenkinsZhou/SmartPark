package com.tourcoo.smartpark.core.retrofit;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日9:52
 * @Email: 971613168@qq.com
 */
public class DataNullException extends Exception {

    public int errorCode;

    public DataNullException() {
        this("");
    }

    public DataNullException(String message) {
        this(message, -1);
    }

    public DataNullException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DataNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNullException(Throwable cause) {
        super(cause);
    }
}
