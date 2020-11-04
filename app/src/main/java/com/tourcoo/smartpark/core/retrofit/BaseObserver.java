package com.tourcoo.smartpark.core.retrofit;

import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IHttpRequestControl;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月27日17:59
 * @Email: 971613168@qq.com
 */
public abstract class BaseObserver<T> extends DefaultObserver<T> {
    public IHttpRequestControl mHttpRequestControl;

    public BaseObserver(IHttpRequestControl httpRequestControl) {
        this.mHttpRequestControl = httpRequestControl;
    }

    public BaseObserver() {
        this(null);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onNext( T t) {
        onRequestSuccess(t);
    }

    /**
     * 获取成功后数据展示
     *
     * @param entity 可能为null
     */
    public abstract void onRequestSuccess(T entity);

    public  void onRequestError(Throwable throwable){

    }


    @Override
    public void onError(@NonNull Throwable e) {
        //错误全局拦截控制
        boolean isIntercept = UiManager.getInstance().getObserverControl() != null && UiManager.getInstance().getObserverControl().onError(this, e);
        if (isIntercept) {
            return;
        }
        if (e instanceof DataNullException) {
            onNext(null);
            return;
        }
        if(UiManager.getInstance().getHttpRequestControl() != null){
            UiManager.getInstance().getHttpRequestControl().httpRequestError(mHttpRequestControl,e);
        }
        onRequestError(e);
    }
}
