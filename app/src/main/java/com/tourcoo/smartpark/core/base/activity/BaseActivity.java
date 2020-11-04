package com.tourcoo.smartpark.core.base.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.tourcoo.smartpark.core.CommonConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IBaseView;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.control.QuitAppControl;
import com.tourcoo.smartpark.core.manager.RxJavaManager;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月26日16:21
 * @Email: 971613168@qq.com
 */
public abstract class BaseActivity extends RxAppCompatActivity implements IBaseView {
    protected String TAG = getClass().getSimpleName();
    protected Activity mContext;
    protected View mContentView;
    protected Bundle mSavedInstanceState;
    protected boolean mIsViewLoaded = false;
    protected boolean mIsFirstShow = true;
    protected boolean mIsFirstBack = true;
    protected long mDelayBack = 2000;
    private QuitAppControl mQuitAppControl;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSavedInstanceState = savedInstanceState;
        mContext = this;
        beforeSetContentView();
        mContentView = View.inflate(mContext, getContentLayout(), null);
        //解决StatusLayoutManager与SmartRefreshLayout冲突
        if (this instanceof IRefreshLoadView) {
            if (CommonUtil.isClassExist(CommonConstant.SMART_REFRESH_LAYOUT_CLASS)) {
                if (mContentView.getClass() == SmartRefreshLayout.class) {
                    FrameLayout frameLayout = new FrameLayout(mContext);
                    if (mContentView.getLayoutParams() != null) {
                        frameLayout.setLayoutParams(mContentView.getLayoutParams());
                    }
                    frameLayout.addView(mContentView);
                    mContentView = frameLayout;
                }
            }
        }
        setContentView(mContentView);
        mIsViewLoaded = true;
        beforeInitView(savedInstanceState);
        initView(savedInstanceState);
    }


    private void beforeLazyLoadData(){
        //确保视图加载及视图绑定完成避免刷新UI抛出异常
        if(mIsViewLoaded){
            RxJavaManager.getInstance().setTimer(10)
                    .compose(this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new BaseObserver<Long>() {
                        @Override
                        public void onRequestSuccess(Long entity) {
                            beforeLazyLoadData();
                        }
                    });
        }else {
            lazyLoadData();
        }
    }

    protected void lazyLoadData(){
        if (mIsFirstShow) {
            mIsFirstShow = false;
            loadData();
        }
    }

    @Override
    protected void onResume() {
        beforeLazyLoadData();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContentView = null;
        mContext = null;
        mSavedInstanceState = null;
        mQuitAppControl = null;
        TAG = null;
    }


    /**
     * 退出程序
     */
    protected void quitApp() {
        mQuitAppControl = UiManager.getInstance().getQuitAppControl();
        mDelayBack = mQuitAppControl != null ? mQuitAppControl.quipApp(mIsFirstBack, this) : mDelayBack;
        //时延太小或已是第二次提示直接通知执行最终操作
        if (mDelayBack <= 0 || !mIsFirstBack) {
            if (mQuitAppControl != null) {
                mQuitAppControl.quipApp(false, this);
            } else {
                StackUtil.getInstance().exit();
            }
            return;
        }
        //编写逻辑
        if (mIsFirstBack) {
            mIsFirstBack = false;
            RxJavaManager.getInstance().setTimer(mDelayBack)
                    .compose(this.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(new BaseObserver<Long>() {
                        @Override
                        public void onRequestSuccess(Long entity) {
                            mIsFirstBack = true;
                        }
                    });
        }
    }

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        if (UiManager.getInstance().getActivityFragmentControl() != null) {
            UiManager.getInstance().getActivityFragmentControl().setContentViewBackground(mContentView, this.getClass());
        }
    }
}
