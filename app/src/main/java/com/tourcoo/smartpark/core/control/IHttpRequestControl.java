package com.tourcoo.smartpark.core.control;

import android.view.View;

import com.tourcoo.smartpark.bean.BaseResult;


/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月22日10:51
 * @Email: 971613168@qq.com
 */
public interface IHttpRequestControl extends StatusLayoutControl {
    /**
     * 获取内容布局
     *
     * @return
     */
    View getContentView();

    void  handleSuccessData(BaseResult<?> netData);
}
