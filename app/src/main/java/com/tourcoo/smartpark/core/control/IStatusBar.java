package com.tourcoo.smartpark.core.control;

import android.app.Activity;
import android.view.View;

import com.tourcoo.smartpark.core.utils.StatusViewHelper;

/**
 * @author :JenkinsZhou
 * @description :Activity 全局状态栏控制
 * @company :途酷科技
 * @date 2020年10月28日16:22
 * @Email: 971613168@qq.com
 */
public interface IStatusBar {

    /**
     * Activity 全局状态栏控制可设置部分页面属性
     *
     * @param activity 目标Activity
     * @param helper   StatusViewHelper
     * @param topView  顶部Activity
     * @return true 表示调用 helper 的init方法进行设置
     */
    boolean setStatusBar(Activity activity, StatusViewHelper helper, View topView);
}
