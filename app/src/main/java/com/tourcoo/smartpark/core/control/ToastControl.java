package com.tourcoo.smartpark.core.control;

import android.widget.Toast;

import com.tourcoo.smartpark.core.widget.view.radius.RadiusTextView;


/**
 * @author :JenkinsZhou
 * @description :吐司控制
 * @company :途酷科技
 * @date 2020年10月28日17:11
 * @Email: 971613168@qq.com
 */
public interface ToastControl {

    /**
     * 处理其它异常情况
     *
     * @return
     */
    Toast getToast();

    /**
     * 设置Toast
     *
     * @param toast    ToastUtil 中的Toast
     * @param textView ToastUtil 中的Toast设置的View
     */
    void setToast(Toast toast, RadiusTextView textView);
}
