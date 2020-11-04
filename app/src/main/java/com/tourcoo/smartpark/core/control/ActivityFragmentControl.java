package com.tourcoo.smartpark.core.control;

import android.app.Application;
import android.view.View;

import androidx.fragment.app.FragmentManager;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日17:37
 * @Email: 971613168@qq.com
 */
public interface ActivityFragmentControl extends INavigationBar, IStatusBar{

    /**
     * 设置背景色
     *
     * @param contentView
     * @param cls
     */
    void setContentViewBackground(View contentView, Class<?> cls);


    /**
     *
     * Activity 全局生命周期回调
     *
     * @return
     */
    Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks();


    /**
     * Fragment全局生命周期回调
     *
     * @return
     */
    FragmentManager.FragmentLifecycleCallbacks getFragmentLifecycleCallbacks();
}
