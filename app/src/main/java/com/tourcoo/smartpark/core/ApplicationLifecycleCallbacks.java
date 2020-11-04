package com.tourcoo.smartpark.core;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.core.base.activity.BaseRefreshLoadActivity;
import com.tourcoo.smartpark.core.base.fragment.BaseRefreshLoadFragment;
import com.tourcoo.smartpark.core.control.ActivityFragmentControl;
import com.tourcoo.smartpark.core.control.INavigationBar;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.control.IRefreshView;
import com.tourcoo.smartpark.core.control.IStatusBar;
import com.tourcoo.smartpark.core.control.ITitleView;
import com.tourcoo.smartpark.core.delegate.DelegateManager;
import com.tourcoo.smartpark.core.delegate.RefreshDelegate;
import com.tourcoo.smartpark.core.delegate.TitleDelegate;
import com.tourcoo.smartpark.core.helper.navigation.KeyboardHelper;
import com.tourcoo.smartpark.core.helper.navigation.NavigationViewHelper;
import com.tourcoo.smartpark.core.manager.RxJavaManager;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.utils.FindViewUtil;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.utils.StatusViewHelper;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日9:59
 * @Email: 971613168@qq.com
 */
public class ApplicationLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private String TAG = getClass().getSimpleName();
    private ActivityFragmentControl mActivityFragmentControl;
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;
    private FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks;
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        LogUtils.i(TAG, "onActivityCreated:" + activity.getClass().getSimpleName() + ";contentView:" + CommonUtil.getRootView(activity));
        getControl();
        //统一Activity堆栈管理
        StackUtil.getInstance().push(activity);
        //统一Fragment生命周期处理
        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(this, true);
            if (mFragmentLifecycleCallbacks != null) {
                fragmentManager.registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, true);
            }
        }
        //回调给其他监听者实现自己逻辑
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityCreated(activity, savedInstanceState);
        }
    }
    /**
     * 回调设置Activity/Fragment背景
     *
     * @param v
     * @param cls
     */
    private void setContentViewBackground(View v, Class<?> cls) {
        if (mActivityFragmentControl != null && v != null) {
            mActivityFragmentControl.setContentViewBackground(v, cls);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        View contentView = CommonUtil.getRootView(activity);
        boolean isSet = activity.getIntent().getBooleanExtra(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND, false);
        if (!isSet) {
            setContentViewBackground(CommonUtil.getRootView(activity), activity.getClass());
        }
        //设置状态栏
        setStatusBar(activity);
        //设置虚拟导航栏功能
        setNavigationBar(activity);
        //设置TitleBarView-先设置TitleBarView避免多状态将布局替换
        if (activity instanceof ITitleView
                && !(activity instanceof IRefreshLoadView)
                && !activity.getIntent().getBooleanExtra(CommonConstant.IS_SET_TITLE_BAR_VIEW, false)
                && contentView != null) {
            DelegateManager.getInstance().putTitleDelegate(activity.getClass(),
                    new TitleDelegate(contentView, (ITitleView) activity, activity.getClass()));
            activity.getIntent().putExtra(CommonConstant.IS_SET_TITLE_BAR_VIEW, true);
        }
        //配置下拉刷新
        if (activity instanceof IRefreshView
                && !(BaseRefreshLoadActivity.class.isAssignableFrom(activity.getClass()))
                && !activity.getIntent().getBooleanExtra(CommonConstant.IS_SET_REFRESH_VIEW, false)) {
            IRefreshView refreshView = (IRefreshView) activity;
            if (contentView != null
                    || refreshView.getContentView() != null) {
                DelegateManager.getInstance().putRefreshDelegate(activity.getClass(),
                        new RefreshDelegate(
                                refreshView.getContentView() != null ? refreshView.getContentView() : contentView,
                                (IRefreshView) activity));
                activity.getIntent().putExtra(CommonConstant.IS_SET_REFRESH_VIEW, true);
            }
        }
        //回调给开发者实现自己应用逻辑
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityStarted(activity);
        }
    }

    /**
     * 设置状态栏
     *
     * @param activity 目标Activity
     */
    private void setStatusBar(Activity activity) {
        boolean isSet = activity.getIntent().getBooleanExtra(CommonConstant.IS_SET_STATUS_VIEW_HELPER, false);
        if (isSet) {
            return;
        }
        TitleBarView titleBarView = FindViewUtil.getTargetView(activity.getWindow().getDecorView(), TitleBarView.class);
        //不包含TitleBarView处理
        if (titleBarView == null && !(activity instanceof BaseMainActivity)) {
            View topView = getTopView(CommonUtil.getRootView(activity));
            LogUtils.i(TAG, "其它三方库设置状态栏沉浸");
            StatusViewHelper statusViewHelper = StatusViewHelper.with(activity)
                    .setControlEnable(true)
                    .setPlusStatusViewEnable(false)
                    .setTransEnable(true)
                    .setTopView(topView);
            if (topView != null && topView.getBackground() != null) {
                Drawable drawable = topView.getBackground().mutate();
                statusViewHelper.setStatusLayoutDrawable(drawable);
            }
            boolean isInit = mActivityFragmentControl == null || mActivityFragmentControl.setStatusBar(activity, statusViewHelper, topView);
            if (activity instanceof IStatusBar) {
                isInit = ((IStatusBar) activity).setStatusBar(activity, statusViewHelper, topView);
            }
            if (isInit) {
                //状态栏黑色文字图标flag被覆盖问题--临时解决
                RxJavaManager.getInstance().setTimer(10)
                        .subscribe(new BaseObserver<Long>() {
                            @Override
                            public void onRequestSuccess(Long entity) {
                                if (activity.isFinishing()) {
                                    return;
                                }
                                statusViewHelper.init();
                                activity.getIntent().putExtra(CommonConstant.IS_SET_STATUS_VIEW_HELPER, true);
                            }
                        });
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        LogUtils.i(TAG, "onActivityResumed:" + activity.getClass().getSimpleName());
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        LogUtils.i(TAG, "onActivityPaused:" + activity.getClass().getSimpleName() + ";isFinishing:" + activity.isFinishing());
        //Activity销毁前的时机需要关闭软键盘-在onActivityStopped及onActivityDestroyed生命周期内已无法关闭
        if (activity.isFinishing()) {
            KeyboardHelper.closeKeyboard(activity);
        }
        //回调给开发者实现自己应用逻辑
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        LogUtils.i(TAG, "onActivityStopped:" + activity.getClass().getSimpleName() + ";isFinishing:" + activity.isFinishing());
        //回调给开发者实现自己应用逻辑
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        LogUtils.i(TAG, "onActivitySaveInstanceState:" + activity.getClass().getSimpleName());
        //回调给开发者实现自己应用逻辑
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivitySaveInstanceState(activity, outState);
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //横竖屏会重绘将状态重置
        if (activity.getIntent() != null) {
            activity.getIntent().removeExtra(CommonConstant.IS_SET_STATUS_VIEW_HELPER);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_NAVIGATION_VIEW_HELPER);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_REFRESH_VIEW);
            activity.getIntent().removeExtra(CommonConstant.IS_SET_TITLE_BAR_VIEW);
        }
        LogUtils.i(TAG, "onActivityDestroyed:" + activity.getClass().getSimpleName() + ";isFinishing:" + activity.isFinishing());
        StackUtil.getInstance().pop(activity, false);

        //清除下拉刷新代理FastRefreshDelegate
        DelegateManager.getInstance().removeRefreshDelegate(activity.getClass());
        //清除标题栏代理类FastTitleDelegate
        DelegateManager.getInstance().removeTitleDelegate(activity.getClass());
        /*//清除BasisHelper
        DelegateManager.getInstance().removeBasisHelper(activity);*/
        //回调给开发者实现自己应用逻辑
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityDestroyed(activity);
        }
    }

    @Override
    public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentViewDestroyed(fm, f);
        if (f.getArguments() != null) {
            f.getArguments().putBoolean(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND, false);
        }
        DelegateManager.getInstance().removeRefreshDelegate(f.getClass());
        DelegateManager.getInstance().removeTitleDelegate(f.getClass());
    }

    /**
     * 实时获取回调
     */

    private void getControl() {
        mActivityFragmentControl = UiManager.getInstance().getActivityFragmentControl();
        if (mActivityFragmentControl == null) {
            return;
        }
        mActivityLifecycleCallbacks = mActivityFragmentControl.getActivityLifecycleCallbacks();
        mFragmentLifecycleCallbacks = mActivityFragmentControl.getFragmentLifecycleCallbacks();
    }


    /**
     * 获取Activity 顶部View(用于延伸至状态栏下边)
     *
     * @param target
     * @return
     */
    private View getTopView(View target) {
        if (target != null && target instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) target;
            if (group.getChildCount() > 0) {
                target = ((ViewGroup) target).getChildAt(0);
            }
        }
        return target;
    }


    /**
     * 设置全局虚拟导航栏功能
     *
     * @param activity 目标Activity
     */
    private void setNavigationBar(Activity activity) {
        boolean isSet = activity.getIntent().getBooleanExtra(CommonConstant.IS_SET_NAVIGATION_VIEW_HELPER, false);
        if (isSet) {
            return;
        }
        LogUtils.i(TAG, "setNavigationBars:设置虚拟导航栏");
        View bottomView = CommonUtil.getRootView(activity);
       /* //继承FastMainActivity底部View处理
        if (BaseMainActivity.class.isAssignableFrom(activity.getClass())) {
            CommonTabLayout tabLayout = FindViewUtil.getTargetView(bottomView, CommonTabLayout.class);
            if (tabLayout != null) {
                bottomView = tabLayout;
            }
        }*/
        //设置虚拟导航栏控制
        NavigationViewHelper navigationViewHelper = NavigationViewHelper.with(activity)
                .setWhiteStyle();
        if (activity instanceof KeyboardHelper.OnKeyboardVisibilityChangedListener) {
            navigationViewHelper.setOnKeyboardVisibilityChangedListener((KeyboardHelper.OnKeyboardVisibilityChangedListener) activity);
        }
        boolean isInit = mActivityFragmentControl == null || mActivityFragmentControl.setNavigationBar(activity, navigationViewHelper, bottomView);
        if (activity instanceof INavigationBar) {
            isInit = ((INavigationBar) activity).setNavigationBar(activity, navigationViewHelper, bottomView);
        }
        if (isInit) {
            activity.getIntent().putExtra(CommonConstant.IS_SET_NAVIGATION_VIEW_HELPER, true);
            navigationViewHelper.init();
        }
    }

    @Override
    public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState);
        boolean isSet = f.getArguments() != null && f.getArguments().getBoolean(CommonConstant.IS_SET_CONTENT_VIEW_BACKGROUND, false);
        if (!isSet) {
            setContentViewBackground(v, f.getClass());
        }
        //设置TitleBarView-先设置TitleBarView避免多状态将布局替换
        if (f instanceof ITitleView && !(f instanceof IRefreshLoadView)) {
            DelegateManager.getInstance().putTitleDelegate(f.getClass(),
                    new TitleDelegate(v, (ITitleView) f, f.getClass()));
        }
        //刷新功能处理
        if (f instanceof IRefreshView
                && !(BaseRefreshLoadFragment.class.isAssignableFrom(f.getClass()))) {
            IRefreshView refreshView = (IRefreshView) f;
            DelegateManager.getInstance().putRefreshDelegate(f.getClass(),
                    new RefreshDelegate(
                            refreshView.getContentView() != null ? refreshView.getContentView() : f.getView(),
                            refreshView));
        }
    }


}
