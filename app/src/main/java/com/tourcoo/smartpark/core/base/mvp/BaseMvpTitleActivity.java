package com.tourcoo.smartpark.core.base.mvp;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.trello.rxlifecycle3.LifecycleTransformer;
import com.trello.rxlifecycle3.android.ActivityEvent;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年08月07日13:37
 * @Email: 971613168@qq.com
 */
@SuppressWarnings("unchecked")
public abstract class BaseMvpTitleActivity<P extends BasePresenter> extends BaseTitleActivity implements IBaseView {


    protected P presenter;

    protected IosLoadingDialog loadingDialog;


    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = createPresenter();
        if (presenter != null) {
            presenter.attachView(this);
        }
        loadingDialog = new IosLoadingDialog(mContext, "正在加载...");
        loadingDialog.setCancelable(loadingCanCancel());
        loadPresenter();
    }


    protected abstract void loadPresenter();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
            presenter = null;
        }

    }


    //***************************************IBaseView方法实现*************************************


    @Override
    public void onEmpty(Object tag) {

    }

    @Override
    public void onError(Object tag, String errorMsg) {

    }

    @Override
    public Context getContext() {
        return mContext;
    }


    //***************************************IBaseView方法实现*************************************

    /**
     * 创建Presenter
     *
     * @return p
     */
    protected abstract P createPresenter();

    @Override
    public <T> LifecycleTransformer<T> bindUntilEvent() {
        return bindUntilEvent(ActivityEvent.DESTROY);
    }

    @Override
    public void showLoadingDialog() {

    }

    @Override
    public void showLoadingDialog(String message) {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.setLoadingText(message);
            loadingDialog.show();
        }
    }

    @Override
    public void closeLoadingDialog() {
        LogUtils.d("执行了---closeLoadingDialog");
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


    /**
     * 加载框是否可以取消
     *
     * @return boolean
     */
    protected boolean loadingCanCancel() {
        return false;
    }
}
