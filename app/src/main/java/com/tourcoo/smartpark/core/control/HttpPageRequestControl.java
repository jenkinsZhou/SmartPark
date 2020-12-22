package com.tourcoo.smartpark.core.control;

import java.util.List;

import io.reactivex.annotations.NonNull;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日10:26
 * @Email: 971613168@qq.com
 */
public interface HttpPageRequestControl  {

    /**
     * @param httpRequestControl 调用页面相关参数
     * @param dataList           数据列表
     * @param httpDataListener   设置完成回调--用于特殊需求页面做后续操作
     */
    void httpRequestSuccess(IHttpPageRequestControl httpRequestControl, List<?> dataList, OnHttpDataListener httpDataListener);

    /**
     * 网络成功回调
     *
     * @param httpRequestControl 调用页面相关参数
     * @param dataList           数据列表
     */
    default void httpRequestSuccess(IHttpPageRequestControl httpRequestControl, List<?> dataList) {
        httpRequestSuccess(httpRequestControl, dataList, null);
    }

    /**
     * 请求失败后回调
     * @param httpRequestControl 调用页面相关参数
     * @param e 抛出的错误
     */
    void httpRequestError(IHttpPageRequestControl httpRequestControl, @NonNull Throwable e);
}
