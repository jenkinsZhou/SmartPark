package com.tourcoo.smartpark.ui.account;

import android.text.TextUtils;

import com.luck.picture.lib.tools.SPUtils;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.util.SpUtil;
import com.tourcoo.smartpark.util.StringUtil;

/**
 * @author :JenkinsZhou
 * @description : 账户帮助类
 * @company :途酷科技
 * @date 2020年11月24日11:29
 * @Email: 971613168@qq.com
 */
public class AccountHelper {
    public static final String TAG = "AccountHelper";
    private boolean needResetPass = true;
    /**
     * 访问需要的token
     */
    private String accessToken = "";
    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_NEED_RESET_PASS = "reset_pass";
    private UserInfo userInfo;

    private static class SingletonInstance {
        private static final AccountHelper INSTANCE = new AccountHelper();
    }

    public static AccountHelper getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public String getAccessToken() {
        if (TextUtils.isEmpty(accessToken)) {
            accessToken = SPUtils.getInstance().getString(PREF_ACCESS_TOKEN, "");
        }
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = StringUtil.getNotNullValue(accessToken);
        SPUtils.getInstance().put(PREF_ACCESS_TOKEN, this.accessToken);
    }

    public boolean isNeedResetPass() {
        this.needResetPass = SPUtils.getInstance().getBoolean(PREF_NEED_RESET_PASS);
        return needResetPass;
    }

    public void setNeedResetPass(boolean needResetPass) {
        this.needResetPass = needResetPass;
        SPUtils.getInstance().put(PREF_NEED_RESET_PASS, needResetPass);
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
