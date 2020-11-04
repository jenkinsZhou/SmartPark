package com.tourcoo.smartpark.core.control;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.tourcoo.smartpark.core.widget.dialog.loading.LoadingDialogWrapper;

/**
 * @author :JenkinsZhou
 * @description :用于全局配置网络请求登录Loading提示框
 * @company :途酷科技
 * @date 2020年10月28日16:24
 * @Email: 971613168@qq.com
 */
public interface LoadingDialog {

    /**
     * 设置快速Loading Dialog
     *
     * @param activity
     * @return
     */
    @Nullable
    LoadingDialogWrapper createLoadingDialog(@Nullable Activity activity);
}
