package com.tourcoo.smartpark.util;

/**
 * @author :JenkinsZhou
 * @description : 点击工具类
 * @company :途酷科技
 * @date 2021年03月01日10:40
 * @Email: 971613168@qq.com
 */
public class ClickUtils {
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
