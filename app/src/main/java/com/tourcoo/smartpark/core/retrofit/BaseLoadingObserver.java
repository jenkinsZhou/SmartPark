package com.tourcoo.smartpark.core.retrofit;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IHttpPageRequestControl;
import com.tourcoo.smartpark.core.control.IHttpRequestControl;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.widget.dialog.loading.LoadingDialogWrapper;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年11月13日16:44
 * @Email: 971613168@qq.com
 */
public abstract class BaseLoadingObserver<T> extends BaseObserver<T> {
    /**
     * Dialog
     */
    private LoadingDialogWrapper mDialog;

    /**
     * 用于全局配置
     *
     * @param activity
     */
    public BaseLoadingObserver(@Nullable Activity activity, IHttpPageRequestControl httpRequestControl, @StringRes int resId) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity).setMessage(resId), httpRequestControl);
    }

    public BaseLoadingObserver(IHttpPageRequestControl httpRequestControl, @StringRes int resId) {
        this(StackUtil.getInstance().getCurrent(), httpRequestControl, resId);
    }

    public BaseLoadingObserver(@Nullable Activity activity, IHttpPageRequestControl httpRequestControl, CharSequence msg) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity).setMessage(msg), httpRequestControl);
    }

    public BaseLoadingObserver(IHttpPageRequestControl httpRequestControl, CharSequence msg) {
        this(StackUtil.getInstance().getCurrent(), httpRequestControl, msg);
    }

    public BaseLoadingObserver(@Nullable Activity activity, @StringRes int resId) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity).setMessage(resId));
    }

    public BaseLoadingObserver(@StringRes int resId) {
        this(StackUtil.getInstance().getCurrent(), resId);
    }

    public BaseLoadingObserver(@Nullable Activity activity, CharSequence msg) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity).setMessage(msg));
    }

    public BaseLoadingObserver(CharSequence msg) {
        this(StackUtil.getInstance().getCurrent(), msg);
    }

    public BaseLoadingObserver(@Nullable Activity activity, IHttpPageRequestControl httpRequestControl) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity), httpRequestControl);
    }

    public BaseLoadingObserver(@Nullable Activity activity, IHttpRequestControl httpRequestControl) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity), httpRequestControl);
    }
    public BaseLoadingObserver(IHttpPageRequestControl httpPageRequestControl) {
        this(StackUtil.getInstance().getCurrent(), httpPageRequestControl);
    }



    public BaseLoadingObserver(IHttpRequestControl httpRequestControl) {
        this(StackUtil.getInstance().getCurrent(), httpRequestControl);
    }

    public BaseLoadingObserver(@Nullable Activity activity) {
        this(UiManager.getInstance().getLoadingDialog().createLoadingDialog(activity));
    }

    public BaseLoadingObserver() {
        this(StackUtil.getInstance().getCurrent());
    }

    public BaseLoadingObserver(LoadingDialogWrapper dialog) {
        this(dialog, null,null);
    }



    public BaseLoadingObserver(LoadingDialogWrapper dialog, IHttpPageRequestControl httpRequestControl) {
        super(httpRequestControl);
        this.mDialog = dialog;
    }

    public BaseLoadingObserver(LoadingDialogWrapper dialog, IHttpPageRequestControl httpPageRequestControl,IHttpRequestControl httpRequestControl) {
        super(httpPageRequestControl,httpRequestControl);
        this.mDialog = dialog;
    }
    public BaseLoadingObserver(LoadingDialogWrapper dialog, IHttpRequestControl requestControl) {
        super(requestControl);
        this.mDialog = dialog;
    }



    @Override
    public void onNext(T entity) {
        dismissProgressDialog();
        super.onNext(entity);
    }

    @Override
    public void onError(Throwable e) {
        dismissProgressDialog();
        super.onError(e);
    }

    public void showProgressDialog() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgressDialog();
    }
}
