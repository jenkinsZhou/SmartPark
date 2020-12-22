package com.tourcoo.smartpark.core;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;

import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.tourcoo.SmartParkApplication;
import com.tourcoo.smartpark.core.control.ActivityDispatchEventControl;
import com.tourcoo.smartpark.core.control.ActivityFragmentControl;
import com.tourcoo.smartpark.core.control.HttpPageRequestControl;
import com.tourcoo.smartpark.core.control.HttpRequestControl;
import com.tourcoo.smartpark.core.control.LoadMoreFoot;
import com.tourcoo.smartpark.core.control.LoadingDialog;
import com.tourcoo.smartpark.core.control.MultiStatusView;
import com.tourcoo.smartpark.core.control.ObserverControl;
import com.tourcoo.smartpark.core.control.QuitAppControl;
import com.tourcoo.smartpark.core.control.RecyclerViewControl;
import com.tourcoo.smartpark.core.control.TitleBarViewControl;
import com.tourcoo.smartpark.core.control.ToastControl;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.tourcoo.smartpark.core.widget.dialog.loading.LoadingDialogWrapper;

import static com.tourcoo.smartpark.core.CommonConstant.EXCEPTION_NOT_INIT_FAST_MANAGER;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日16:57
 * @Email: 971613168@qq.com
 */
public class UiManager {
    public static final String TAG = "UiManager";
    //原本在Provider中默认进行初始化,如果app出现多进程使用该模式可避免调用异常出现
    static {
        Application application = SmartParkApplication.getContext();
        if (application != null) {
            Log.i(TAG, "initSuccess");
            init(application);
        }
    }

    private static volatile UiManager sInstance;

    private UiManager() {
    }

    public static UiManager getInstance() {
        if (sInstance == null) {
            throw new NullPointerException(EXCEPTION_NOT_INIT_FAST_MANAGER);
        }
        return sInstance;
    }

    private static Application mApplication;
    /**
     * Adapter加载更多View
     */
    private LoadMoreFoot mLoadMoreFoot;
    /**
     * 全局设置列表
     */
    private RecyclerViewControl mRecyclerViewControl;
    /**
     * SmartRefreshLayout默认刷新头
     */
    private DefaultRefreshHeaderCreator mDefaultRefreshHeader;
    /**
     * 多状态布局--加载中/空数据/错误/无网络
     */
    private MultiStatusView mMultiStatusView;
    /**
     * 配置全局通用加载等待Loading提示框
     */
    private LoadingDialog mLoadingDialog;
    /**
     * 配置TitleBarView相关属性
     */
    private TitleBarViewControl mTitleBarViewControl;
   /* *//**
     * 配置Activity滑动返回相关属性
     *//*
    private SwipeBackControl mSwipeBackControl;*/
    /**
     * 配置Activity/Fragment(背景+Activity强制横竖屏+Activity 生命周期回调+Fragment生命周期回调)
     */
    private ActivityFragmentControl mActivityFragmentControl;

  /*  *//**
     * 配置BasisActivity 子类前台时监听按键相关
     *//*
    private ActivityKeyEventControl mActivityKeyEventControl;*/

    /**
     * 配置BasisActivity 子类事件派发相关
     */
    private ActivityDispatchEventControl mActivityDispatchEventControl;
    /**
     * 配置网络请求
     */
    private HttpPageRequestControl mHttpPageRequestControl;

    private HttpRequestControl mHttpRequestControl;

    /**
     * 配置{@link com.tourcoo.smartpark.core.retrofit.BaseObserver#onError(Throwable)} ()} 全局处理
     */
    private ObserverControl mObserverControl;
    /**
     * Activity 主页点击返回键控制
     */
    private QuitAppControl mQuitAppControl;
    /**
     * ToastUtil相关配置
     */
    private ToastControl mToastControl;

    public Application getApplication() {
        return mApplication;
    }

    /**
     * 不允许外部调用
     *
     * @param application Application 对象
     * @return
     */
    static UiManager init(Application application) {
        Log.i("FastManager","init_mApplication:" + mApplication + ";application;" + application);
        //保证只执行一次初始化属性
        if (mApplication == null && application != null) {
            mApplication = application;
            sInstance = new UiManager();
            //预设置FastLoadDialog属性
            sInstance.setLoadingDialog(new LoadingDialog() {
                @Nullable
                @Override
                public LoadingDialogWrapper createLoadingDialog(@Nullable Activity activity) {
                    return new LoadingDialogWrapper(activity, new IosLoadingDialog(activity));
                }
            });
            //初始化Toast工具
            ToastUtil.init(mApplication);
            //注册activity生命周期
            mApplication.registerActivityLifecycleCallbacks(new ApplicationLifecycleCallbacks());
        }
        return getInstance();
    }


    public LoadMoreFoot getLoadMoreFoot() {
        return mLoadMoreFoot;
    }

    /**
     * 设置Adapter统一加载更多相关脚布局
     *
     * @param mLoadMoreFoot
     * @return
     */
    public UiManager setLoadMoreFoot(LoadMoreFoot mLoadMoreFoot) {
        this.mLoadMoreFoot = mLoadMoreFoot;
        return this;
    }

    public RecyclerViewControl getRecyclerViewControl() {
        return mRecyclerViewControl;
    }

    /**
     * 全局设置列表
     *
     * @param control
     * @return
     */
    public UiManager setRecyclerViewControl(RecyclerViewControl control) {
        this.mRecyclerViewControl = control;
        return this;
    }

    public DefaultRefreshHeaderCreator getDefaultRefreshHeader() {
        return mDefaultRefreshHeader;
    }

    /**
     * 设置SmartRefreshLayout 下拉刷新头
     *
     * @param control
     * @return
     */
    public UiManager setDefaultRefreshHeader(DefaultRefreshHeaderCreator control) {
        this.mDefaultRefreshHeader = control;
        return sInstance;
    }

    public MultiStatusView getMultiStatusView() {
        return mMultiStatusView;
    }

    /**
     * 设置多状态布局--加载中/空数据/错误/无网络
     *
     * @param control
     * @return
     */
    public UiManager setMultiStatusView(MultiStatusView control) {
        this.mMultiStatusView = control;
        return this;
    }

    public LoadingDialog getLoadingDialog() {
        return mLoadingDialog;
    }

    /**
     * 设置全局网络请求等待Loading提示框如登录等待loading
     *
     * @param control
     * @return
     */
    public UiManager setLoadingDialog(LoadingDialog control) {
        if (control != null) {
            this.mLoadingDialog = control;
        }
        return this;
    }

    public TitleBarViewControl getTitleBarViewControl() {
        return mTitleBarViewControl;
    }

    public UiManager setTitleBarViewControl(TitleBarViewControl control) {
        mTitleBarViewControl = control;
        return this;
    }




    public ActivityFragmentControl getActivityFragmentControl() {
        return mActivityFragmentControl;
    }

    /**
     * 配置Activity/Fragment(背景+Activity强制横竖屏+Activity 生命周期回调+Fragment生命周期回调)
     *
     * @param control
     * @return
     */
    public UiManager setActivityFragmentControl(ActivityFragmentControl control) {
        mActivityFragmentControl = control;
        return this;
    }




    public ActivityDispatchEventControl getActivityDispatchEventControl() {
        return mActivityDispatchEventControl;
    }

    /**
     * 配置BasisActivity 子类事件派发相关
     *
     * @param control
     * @return
     */
    public UiManager setActivityDispatchEventControl(ActivityDispatchEventControl control) {
        mActivityDispatchEventControl = control;
        return this;
    }

    public HttpPageRequestControl getHttpRequestControl() {
        return mHttpPageRequestControl;
    }

    public HttpRequestControl getRequestControl() {
        return mHttpRequestControl;
    }
    /**
     * 配置Http请求成功及失败相关回调-方便全局处理
     *
     * @param control
     * @return
     */
    public UiManager setHttpPageRequestControl(HttpPageRequestControl control) {
        mHttpPageRequestControl = control;
        return this;
    }

    public UiManager setHttpRequestControl(HttpRequestControl control) {
        mHttpRequestControl = control;
        return this;
    }
    public ObserverControl getObserverControl() {
        return mObserverControl;
    }

    /**
     * 配置{@link com.tourcoo.smartpark.core.retrofit.BaseObserver#onError(Throwable)}全局处理
     *
     * @param control ObserverControl对象
     * @return
     */
    public UiManager setObserverControl(ObserverControl control) {
        mObserverControl = control;
        return this;
    }

    public QuitAppControl getQuitAppControl() {
        return mQuitAppControl;
    }

    /**
     * 配置Http请求成功及失败相关回调-方便全局处理
     *
     * @param control
     * @return
     */
    public UiManager setQuitAppControl(QuitAppControl control) {
        mQuitAppControl = control;
        return this;
    }

    public ToastControl getToastControl() {
        return mToastControl;
    }

    /**
     * 配置ToastUtil
     *
     * @param control
     * @return
     */
    public UiManager setToastControl(ToastControl control) {
        mToastControl = control;
        return this;
    }



}
