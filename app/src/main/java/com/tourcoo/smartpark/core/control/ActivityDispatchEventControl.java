package com.tourcoo.smartpark.core.control;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

import com.tourcoo.smartpark.core.base.activity.BaseActivity;

/**
 * @author :JenkinsZhou
 * @description :Activity 事件派发--必须继承自{@link BaseActivity}
 * @company :途酷科技
 * @date 2020年10月28日17:42
 * @Email: 971613168@qq.com
 */
public interface ActivityDispatchEventControl {

    /**
     *
     * @param activity
     * @param event
     * @return
     */
    boolean dispatchTouchEvent(Activity activity, MotionEvent event);


    /**
     *
     * @param activity
     * @param event
     * @return
     */
    boolean dispatchGenericMotionEvent(Activity activity, MotionEvent event);

    /**
     *
     * @param activity
     * @param event
     * @return
     */
    boolean dispatchKeyEvent(Activity activity, KeyEvent event);

    /**
     *
     * @param activity
     * @param event
     * @return
     */
    boolean dispatchKeyShortcutEvent(Activity activity, KeyEvent event);

    /**
     *
     * @param activity
     * @param event
     * @return
     */
    boolean dispatchTrackballEvent(Activity activity, MotionEvent event);

    /**
     *
     * @param activity
     * @param event
     * @return
     */
    boolean dispatchPopulateAccessibilityEvent(Activity activity, AccessibilityEvent event);
}
