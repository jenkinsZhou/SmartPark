package com.tourcoo;

import android.app.Application;
import android.content.Context;


import androidx.multidex.MultiDex;

import com.apkfuns.log2file.LogFileEngineFactory;
import com.apkfuns.logutils.LogUtils;
import com.kingja.loadsir.core.LoadSir;
import com.simple.spiderman.SpiderMan;
import com.tourcoo.smartpark.core.ActivityControlImpl;
import com.tourcoo.smartpark.core.AppImpl;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.HttpPageRequestControlImpl;
import com.tourcoo.smartpark.core.control.HttpRequestControlImpl;
import com.tourcoo.smartpark.core.control.RequestConfig;
import com.tourcoo.smartpark.core.multi_status.MultiEmptyStatusCallback;
import com.tourcoo.smartpark.core.multi_status.MultiStatusErrorCallback;
import com.tourcoo.smartpark.core.multi_status.MultiStatusLoadingCallback;
import com.tourcoo.smartpark.core.multi_status.MultiStatusNetErrorCallback;
import com.tourcoo.smartpark.core.retrofit.RetrofitHelper;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.event.EventConstant;
import com.tourcoo.smartpark.event.OrcInitEvent;
import com.tourcoo.smartpark.threadpool.ThreadPoolManager;
import com.tourcoo.smartpark.widget.orc.OrcPlantInitListener;
import com.tourcoo.smartpark.widget.orc.PredictorWrapper;

import org.greenrobot.eventbus.EventBus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

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
        initLoadSir();
        // 以下用来捕获程序崩溃异常
        // 程序崩溃时触发线程
//        Thread.setDefaultUncaughtExceptionHandler(restartHandler);
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
                .setHttpPageRequestControl(new HttpPageRequestControlImpl())
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
                .setBaseUrl(RequestConfig.BASE_URL)
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
                .setTimeout(20);

    }

    private void intiPlantOrcSdk() {
        ThreadPoolManager.getThreadPoolProxy().execute(() -> {
            PredictorWrapper.setListener(new OrcPlantInitListener() {
                @Override
                public void initSuccess() {
                    EventBus.getDefault().post(new OrcInitEvent(EventConstant.EVENT_INIT_ORC_SUCCESS));
                    ToastUtil.showSuccessDebug("初始化成功:");
                }

                @Override
                public void initFailed(Throwable e) {
                    EventBus.getDefault().post(new OrcInitEvent(EventConstant.EVENT_INIT_ORC_FAILED));
                    ToastUtil.showFailedDebug("车牌识别sdk初始化失败:" + e.toString());
                }


            });
            //授权初始化
            try {
                PredictorWrapper.initLicense(this);
                // 初始化模型
                PredictorWrapper.initModel(this);
            }catch (Exception e){
                e.printStackTrace();
            }

        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public Thread.UncaughtExceptionHandler restartHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            //下面为调试用的代码，发布时可注释
            Writer info = new StringWriter();
            PrintWriter printWriter = new PrintWriter(info);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            LogUtils.e("SmartParkApplication", "SmartParkApplication崩溃原因:" + ex.toString());
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.flush();
            printWriter.close();
            String result = info.toString();
            StackUtil.getInstance().exit();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    };

    private void initLoadSir() {
        LoadSir.beginBuilder()
                .addCallback(new MultiStatusErrorCallback())//添加各种状态页
                .addCallback(new MultiEmptyStatusCallback())
                .addCallback(new MultiStatusLoadingCallback())
                .addCallback(new MultiStatusNetErrorCallback())
                .commit();
    }
}
