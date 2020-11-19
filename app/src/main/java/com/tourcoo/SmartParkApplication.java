package com.tourcoo;

import android.app.Application;


import com.apkfuns.log2file.LogFileEngineFactory;
import com.apkfuns.logutils.LogUtils;
import com.simple.spiderman.SpiderMan;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.ActivityControlImpl;
import com.tourcoo.smartpark.core.AppImpl;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.HttpRequestControlImpl;
import com.tourcoo.smartpark.core.control.RequestConstant;
import com.tourcoo.smartpark.core.manager.GlideManager;
import com.tourcoo.smartpark.core.retrofit.RetrofitHelper;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.threadpool.ThreadPoolManager;
import com.tourcoo.smartpark.ui.HomeActivity;
import com.tourcoo.smartpark.widget.orc.OrcPlantInitListener;
import com.tourcoo.smartpark.widget.orc.PredictorWrapper;

import leakcanary.LeakCanary;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日14:42
 * @Email: 971613168@qq.com
 */
public class SmartParkApplication extends Application {
    private static Application context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        SpiderMan.init(context);
        initLog();
        initConfig();
        intiPlantOrcSdk();

    }

    private void initLog() {
        // 设置日志写文件引擎
        LogUtils.getLog2FileConfig().configLogFileEngine(new LogFileEngineFactory(context));
        //不写入文件
        LogUtils.getLog2FileConfig().configLog2FileEnable(false);
        LogUtils.getLogConfig().configShowBorders(false);
    }

    public static Application getContext() {
        return context;
    }

    private void initConfig() {
        //以下为更丰富自定义方法-可不设置即使用默认配置
        //全局UI配置参数-按需求设置
        AppImpl impl = new AppImpl(context);
        ActivityControlImpl activityControl = new ActivityControlImpl();
        UiManager.getInstance()
                //设置Adapter加载更多视图--默认设置了FastLoadMoreView
                .setLoadMoreFoot(impl)
                //全局设置RecyclerView
                .setRecyclerViewControl(impl)
                //设置RecyclerView加载过程多布局属性
                .setMultiStatusView(impl)
                //设置全局网络请求等待Loading提示框如登录等待loading--观察者必须为FastLoadingObserver及其子类
                .setLoadingDialog(impl)
                //设置SmartRefreshLayout刷新头-自定加载使用BaseRecyclerViewAdapterHelper
                .setDefaultRefreshHeader(impl)
                //设置全局TitleBarView相关配置
                .setTitleBarViewControl(impl)
                //设置Activity滑动返回控制-默认开启滑动返回功能不需要设置透明主题
//                .setSwipeBackControl(new SwipeBackControlImpl())
                //设置Activity/Fragment相关配置(横竖屏+背景+虚拟导航栏+状态栏+生命周期)
                .setActivityFragmentControl(activityControl)
                //配置BasisActivity 子类事件派发相关
                .setActivityDispatchEventControl(activityControl)
                //设置http请求结果全局控制
                .setHttpRequestControl(new HttpRequestControlImpl())
                //配置{@link FastObserver#onError(Throwable)}全局处理
                .setObserverControl(impl)
                //设置主页返回键控制-默认效果为2000 毫秒时延退出程序
                .setQuitAppControl(impl)
                //设置ToastUtil全局控制
                .setToastControl(impl);
        //初始化Retrofit配置
        RetrofitHelper.getInstance()
                //配置全局网络请求BaseUrl
                .setBaseUrl(RequestConstant.BASE_URL)
                //信任所有证书--也可设置setCertificates(单/双向验证)
                .setCertificates()
                //设置统一请求头
//                .addHeader(header)
//                .addHeader(key,value)
                //设置请求全局log-可设置tag及Level类型
                .setLogEnable(true)
//                .setLogEnable(BuildConfig.DEBUG, TAG, HttpLoggingInterceptor.Level.BODY)
                //设置统一超时--也可单独调用read/write/connect超时(可以设置时间单位TimeUnit)
                //默认20 s
                .setTimeout(30);

    }

    private void intiPlantOrcSdk() {
        ThreadPoolManager.getThreadPoolProxy().execute(() -> {
            PredictorWrapper.setListener(new OrcPlantInitListener() {
                @Override
                public void initSuccess() {
                    ToastUtil.showSuccessDebug("初始化成功:");
                }

                @Override
                public void initFailed(Throwable e) {
                    ToastUtil.showFailedDebug("车牌识别sdk初始化失败:" + e.toString());
                }


            });
            //授权初始化
            if (!PredictorWrapper.initLicense(this)) {
                return;
            }
            // 初始化模型
            PredictorWrapper.initModel(this);
        });
    }
}
