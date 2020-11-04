package com.tourcoo.smartpark.core.control;

import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :标题栏接口
 * @company :途酷科技
 * @date 2020年10月28日15:53
 * @Email: 971613168@qq.com
 */
public interface ITitleView {

    /**
     * 子类回调setTitleBar之前执行用于app设置全局Base控制统一TitleBarView
     *
     * @param titleBar
     */
    default void beforeSetTitleBar(TitleBarView titleBar) {

    }

    /**
     * 一般用于最终实现子类设置TitleBarView 其它属性
     *
     * @param titleBar
     */
    void setTitleBar(TitleBarView titleBar);
}
