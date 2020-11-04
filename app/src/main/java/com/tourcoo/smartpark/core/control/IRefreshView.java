package com.tourcoo.smartpark.core.control;

import android.view.View;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

/**
 * @author :JenkinsZhou
 * @description :下拉刷新接口
 * @company :途酷科技
 * @date 2020年10月28日9:46
 * @Email: 971613168@qq.com
 */
public interface IRefreshView extends OnRefreshListener,IMultiStatusViewControl {
    /**
     * 需要下拉刷新的布局 --可以是Activity根布局, Fragment 不要传根布局(除非根布局为SmartRefreshLayout)
     *
     * @return
     */
    default View getContentView() {
        return null;
    }

    /**
     * 是否支持下拉刷新
     *
     * @return
     */
    default boolean isRefreshEnable() {
        return true;
    }

    /**
     * 回调设置的SmartRefreshLayout
     *
     * @param refreshLayout
     */
    default void setSmartRefreshLayout(SmartRefreshLayout refreshLayout) {

    }

}
