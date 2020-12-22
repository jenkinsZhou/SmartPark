package com.tourcoo.smartpark.core.retrofit;

import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IHttpPageRequestControl;
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
    protected IHttpPageRequestControl mHttpPageRequestControl;
    protected IHttpRequestControl requestControl;

    public BaseObserver(IHttpPageRequestControl httpRequestControl) {
        this.mHttpPageRequestControl = httpRequestControl;
    }

    public BaseObserver(IHttpRequestControl requestControl) {
        this.requestControl = requestControl;
    }

    public BaseObserver() {
        this(null, null);
    }

    public BaseObserver(IHttpPageRequestControl mHttpPageRequestControl, IHttpRequestControl requestControl) {
        this.mHttpPageRequestControl = mHttpPageRequestControl;
        this.requestControl = requestControl;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onNext(T t) {
        onRequestSuccess(t);
    }

    /**
     * 获取成功后数据展示
     *
     * @param entity 可能为null
     */
    public abstract void onRequestSuccess(T entity);

    public void onRequestError(Throwable throwable) {

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
        if (UiManager.getInstance().getHttpRequestControl() != null) {
            UiManager.getInstance().getHttpRequestControl().httpRequestError(mHttpPageRequestControl, e);
        }
        if (UiManager.getInstance().getRequestControl() != null) {
            UiManager.getInstance().getRequestControl().httpRequestError(requestControl, e);
        }
        onRequestError(e);
    }
}
