package com.tourcoo.smartpark.bean;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月13日16:21
 * @Email: 971613168@qq.com
 */
public class BaseResult<T> {
    /**
     * code : 1
     * errMsg : 操作成功
     * data : {}
     */

    private int code;
    private String errMsg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
