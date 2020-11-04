package com.tourcoo.smartpark.core.control;

/**
 * @author :JenkinsZhou
 * @description :http请求数据接口
 * @company :途酷科技
 * @date 2020年10月28日10:27
 * @Email: 971613168@qq.com
 */
public interface OnHttpDataListener {

    /**
     * 无数据回调
     */
    void empty();

    /**
     * 加载数据回调
     */
    void onNext();

    /**
     * 无更多数据回调
     */
    void onNoMore();

}
