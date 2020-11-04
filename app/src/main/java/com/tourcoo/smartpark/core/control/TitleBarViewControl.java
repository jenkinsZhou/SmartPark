package com.tourcoo.smartpark.core.control;

import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :标题栏控制
 * @company :途酷科技
 * @date 2020年10月28日17:11
 * @Email: 971613168@qq.com
 */
public interface TitleBarViewControl {

    /**
     * 全局设置TitleBarView 属性回调
     *
     * @param titleBar
     * @param cls 包含TitleBarView的类
     * @return
     */
    boolean createTitleBarViewControl(TitleBarView titleBar, Class<?> cls);
}
