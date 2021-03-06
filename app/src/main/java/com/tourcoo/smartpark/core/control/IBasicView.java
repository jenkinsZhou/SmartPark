package com.tourcoo.smartpark.core.control;

import android.os.Bundle;

import androidx.annotation.LayoutRes;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月26日11:33
 * @Email: 971613168@qq.com
 */
public interface IBasicView {
    /**
     * Activity或Fragment 布局xml
     *
     * @return
     */
    @LayoutRes
    int getContentLayout();

    void initView(Bundle savedInstanceState);

    default void beforeSetContentView() {

    }

    /**
     * 在初始化控件前进行一些操作
     *
     * @param savedInstanceState
     */
    default void beforeInitView(Bundle savedInstanceState) {

    }

    /**
     * 需要加载数据时重写此方法(用户可见时加载（用于懒加载）)
     */
    default void onVisibleLoadData() {

    }

    /**
     * 加载框是否可以取消
     *
     * @return boolean
     */
    default boolean loadingCancelable() {
        return false;
    }

    default void loadData(){

    }
}
