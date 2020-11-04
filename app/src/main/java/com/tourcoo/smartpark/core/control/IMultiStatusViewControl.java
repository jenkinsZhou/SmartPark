package com.tourcoo.smartpark.core.control;

import android.view.View;

import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;

/**
 * @author :JenkinsZhou
 * @description :LoadSir 属性控制
 * @company :途酷科技
 * @date 2020年10月28日15:33
 * @Email: 971613168@qq.com
 */
public interface IMultiStatusViewControl extends Callback.OnReloadListener {

    /**
     * 设置StatusLayoutManager 的目标View
     *
     * @return
     */
    default View getMultiStatusContentView() {
        return null;
    }


    /**
     * 设置StatusLayoutManager
     *
     * @param manager
     */
    default void setMultiStatusViewManager(LoadService manager) {

    }

    /**
     * 获取空布局里点击View回调
     *
     * @return
     */
    default View.OnClickListener getEmptyClickListener() {
        return null;
    }

    /**
     * 获取错误布局里点击View回调
     *
     * @return
     */
    default View.OnClickListener getErrorClickListener() {
        return null;
    }

    /**
     * 获取自定义布局里点击View回调
     *
     * @return
     */
    default View.OnClickListener getCustomerClickListener() {
        return null;
    }
}
