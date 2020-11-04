package com.tourcoo.smartpark.core.control;

import com.tourcoo.smartpark.core.retrofit.BaseObserver;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日9:34
 * @Email: 971613168@qq.com
 */
public interface ObserverControl {

    boolean onError(BaseObserver o, Throwable e);
}
