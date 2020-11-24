package com.tourcoo.smartpark.core.base.mvp;

import android.content.Context;

import com.trello.rxlifecycle3.LifecycleTransformer;

/**
 * @author :JenkinsZhou
 * @description :所有View基类
 * @company :途酷科技
 * @date 2019年07月01日10:16
 * @Email: 971613168@qq.com
 */
public interface IBaseView {
    /**
     * 显示加载框
     */
    void showLoadingDialog();

    void showLoadingDialog(String message);
    /**
     * 隐藏加载框
     */
    void closeLoadingDialog();

    /**
     * 空数据
     *
     * @param tag TAG
     */
    void onEmpty(Object tag);

    /**
     * 错误数据
     *
     * @param tag      TAG
     * @param errorMsg 错误信息
     */
    void onError(Object tag, String errorMsg);

    /**
     * 上下文
     *
     * @return context
     */
    Context getContext();


    <T> LifecycleTransformer<T> bindUntilEvent();



}
