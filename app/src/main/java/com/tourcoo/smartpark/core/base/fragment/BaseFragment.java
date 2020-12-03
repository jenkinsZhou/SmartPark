package com.tourcoo.smartpark.core.base.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentManager;

import com.apkfuns.logutils.LogUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.tourcoo.smartpark.core.CommonConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IBasicView;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.manager.RxJavaManager;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.trello.rxlifecycle3.android.FragmentEvent;
import com.trello.rxlifecycle3.components.support.RxFragment;

/**
 * @author :JenkinsZhou
 * @description :基类BaseFragment
 * @company :途酷科技
 * @date 2020年10月29日16:17
 * @Email: 971613168@qq.com
 */
public abstract class BaseFragment extends RxFragment implements IBasicView {
    protected String TAG = getClass().getSimpleName();
    protected Activity mContext;
    protected View mContentView;
    protected boolean mIsFirstShow;
    protected boolean mIsViewLoaded;
    protected boolean mIsVisibleChanged = false;
    private boolean mIsInViewPager;
    protected Bundle mSavedInstanceState;

    /**
     * 检查Fragment或FragmentActivity承载的Fragment是否只有一个
     *
     * @return
     */
    protected boolean isSingleFragment() {
        int size = 0;
        FragmentManager manager = getFragmentManager();
        if (manager != null && manager.getFragments() != null) {
            size = manager.getFragments().size();
        }
        LogUtils.i(TAG, TAG + ";FragmentManager承载Fragment数量:" + size);
        return size <= 1;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = (Activity) context;
        mIsFirstShow = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mSavedInstanceState = savedInstanceState;
        beforeSetContentView();
        mContentView = inflater.inflate(getContentLayout(), container, false);
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
        mIsViewLoaded = true;
        beforeInitView(savedInstanceState);
        initView(savedInstanceState);

        if (isSingleFragment() && !mIsVisibleChanged) {
            if (getUserVisibleHint() || isVisible() || !isHidden()) {
                onVisibleChanged(true);
            }
        }
        LogUtils.i(TAG, TAG + ";mIsVisibleChanged:" + mIsVisibleChanged
                + ";getUserVisibleHint:" + getUserVisibleHint()
                + ";isHidden:" + isHidden() + ";isVisible:" + isVisible());
        return mContentView;
    }

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        if (UiManager.getInstance().getActivityFragmentControl() != null) {
            UiManager.getInstance().getActivityFragmentControl().setContentViewBackground(mContentView, this.getClass());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
        mContext = null;
        mSavedInstanceState = null;
        TAG = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.i(TAG, "onResume-isAdded:" + isAdded() + ";getUserVisibleHint:" + getUserVisibleHint()
                + ";isHidden:" + isHidden() + ";isVisible:" + isVisible() + ";isResume:" + isResumed() + ";isVisibleToUser:" + isVisibleToUser(this) + ";host:");
        if (isAdded() && isVisibleToUser(this)) {
            onVisibleChanged(true);
        }
    }

    /**
     * @param fragment
     * @return
     */
    private boolean isVisibleToUser(BaseFragment fragment) {
        if (fragment == null) {
            return false;
        }
        if (fragment.getParentFragment() != null) {
            return isVisibleToUser((BaseFragment) fragment.getParentFragment()) && (fragment.isInViewPager() ? fragment.getUserVisibleHint() : fragment.isVisible());
        }
        return fragment.isInViewPager() ? fragment.getUserVisibleHint() : fragment.isVisible();
    }

    /**
     * 不在viewpager中Fragment懒加载
     */
    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!mIsViewLoaded) {
            RxJavaManager.getInstance().setTimer(10)
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(new BaseObserver<Long>() {
                        @Override
                        public void onRequestSuccess(Long entity) {
                            onHiddenChanged(hidden);
                        }
                    });
        } else {
            onVisibleChanged(!hidden);
        }

    }

    /**
     * 在viewpager中的Fragment懒加载
     */
    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsInViewPager = true;
        if (!mIsViewLoaded) {
            RxJavaManager.getInstance().setTimer(10)
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(new BaseObserver<Long>() {
                        @Override
                        public void onRequestSuccess(Long entity) {
                            setUserVisibleHint(isVisibleToUser);
                        }
                    });
        } else {
            onVisibleChanged(isVisibleToUser);
        }
    }

    /**
     * 是否在ViewPager
     *
     * @return
     */
    public boolean isInViewPager() {
        return mIsInViewPager;
    }

    /**
     * 用户可见变化回调
     *
     * @param isVisibleToUser
     */
    protected void onVisibleChanged(final boolean isVisibleToUser) {
        LogUtils.i(TAG, "onVisibleChanged-isVisibleToUser:" + isVisibleToUser);
        mIsVisibleChanged = true;
        if (isVisibleToUser) {
            //避免因视图未加载子类刷新UI抛出异常
            if (!mIsViewLoaded) {
                RxJavaManager.getInstance().setTimer(10)
                        .compose(bindUntilEvent(FragmentEvent.DESTROY))
                        .subscribe(new BaseObserver<Long>() {
                            @Override
                            public void onRequestSuccess(Long entity) {
                                onVisibleChanged(true);
                            }
                        });
            } else {
                lazyLoadData();
            }
        }
    }

    private void lazyLoadData() {
        if (mIsFirstShow && mIsViewLoaded) {
            mIsFirstShow = false;
            onVisibleLoadData();
        }
    }
}
