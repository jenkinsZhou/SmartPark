package com.tourcoo.smartpark.core.control;

import android.app.Activity;
import android.view.View;

import com.tourcoo.smartpark.core.helper.navigation.NavigationViewHelper;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日16:15
 * @Email: 971613168@qq.com
 */
public interface INavigationBar {

    /**
     * Activity 全局虚拟导航栏控制
     *
     * @param activity   目标Activity
     * @param helper     NavigationViewHelper
     * @param bottomView 底部View
     * @return true 表示调用 helper 的init方法进行设置
     */
    boolean setNavigationBar(Activity activity, NavigationViewHelper helper, View bottomView);
}
