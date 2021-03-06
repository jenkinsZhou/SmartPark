package com.tourcoo.smartpark.ui.account;

import android.content.Intent;
import android.text.TextUtils;

import com.luck.picture.lib.tools.SPUtils;
import com.tourcoo.SmartParkApplication;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.ui.account.login.LoginActivity;
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
        if (userInfo != null) {
            return userInfo.isNeedResetPass();
        }
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


    public void logout() {
        userInfo = null;
        setAccessToken("");
        StackUtil.getInstance().popAll();
        Intent intent = new Intent(SmartParkApplication.getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SmartParkApplication.getContext().startActivity(intent);
    }

    public boolean isLogin() {
        return  !TextUtils.isEmpty(getAccessToken());
    }
}
