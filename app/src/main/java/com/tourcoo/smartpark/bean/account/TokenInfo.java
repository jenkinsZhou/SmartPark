package com.tourcoo.smartpark.bean.account;

/**
 * @author :JenkinsZhou
 * @description : TokenInfo
 * @company :途酷科技
 * @date 2020年11月23日17:28
 * @Email: 971613168@qq.com
 */
public class TokenInfo {

    /**
     * token : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJpYXQiOjE2MDYxMjM2NDEsIm5iZiI6MTYwNjEyMzY0MSwiZXhwIjoxNjA2NzI0ODQxLCJkYXRhIjp7Im1lbWJlcklkIjo5LCJwYXJraW5nSWQiOjEwfX0.5Dv_ua1UWFrhO6Y75bLM9tqlBFux5mp4FAqCB6M8JHk
     * needUpdatePassword : false
     */

    private String token;
    private boolean needUpdatePassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isNeedUpdatePassword() {
        return needUpdatePassword;
    }

    public void setNeedUpdatePassword(boolean needUpdatePassword) {
        this.needUpdatePassword = needUpdatePassword;
    }
}
