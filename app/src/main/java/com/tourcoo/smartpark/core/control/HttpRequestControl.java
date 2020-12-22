package com.tourcoo.smartpark.core.control;

import com.tourcoo.smartpark.bean.BaseResult;

import java.util.List;

import io.reactivex.annotations.NonNull;

/**
 * @author :JenkinsZhou
 * @description : HttpRequestControl
 * @company :途酷科技
 * @date 2020年12月22日10:50
 * @Email: 971613168@qq.com
 */
public interface HttpRequestControl  {

    /**
     *
     * @param httpRequestControl
     */
    void httpRequestSuccess(IHttpRequestControl httpRequestControl,BaseResult<?> data);


    /**
     * 请求失败后回调
     * @param httpRequestControl 调用页面相关参数
     * @param e 抛出的错误
     */
    void httpRequestError(IHttpRequestControl httpRequestControl, @NonNull Throwable e);
}
