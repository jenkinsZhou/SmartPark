package com.tourcoo.smartpark.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.gyf.immersionbar.ImmersionBar;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.adapter.home.GridParkAdapter;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.bean.system.AppVersion;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.QuitAppControl;
import com.tourcoo.smartpark.core.control.RequestConfig;
import com.tourcoo.smartpark.core.manager.RxJavaManager;
import com.tourcoo.smartpark.core.multi_status.MultiEmptyStatusCallback;
import com.tourcoo.smartpark.core.multi_status.MultiStatusErrorCallback;
import com.tourcoo.smartpark.core.multi_status.MultiStatusNetErrorCallback;
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.retrofit.DownloadObserver;
import com.tourcoo.smartpark.core.retrofit.RetrofitHelper;
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository;
import com.tourcoo.smartpark.core.utils.FileUtil;
import com.tourcoo.smartpark.core.utils.NetworkUtil;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.tourcoo.smartpark.socket.WebSocketConfig;
import com.tourcoo.smartpark.socket.WebSocketManager;
import com.tourcoo.smartpark.threadpool.ThreadPoolManager;
import com.tourcoo.smartpark.ui.account.AccountHelper;
import com.tourcoo.smartpark.ui.account.EditPassActivity;
import com.tourcoo.smartpark.ui.account.login.LoginActivity;
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity;
import com.tourcoo.smartpark.ui.fee.ExitPayFeeEnterActivity;
import com.tourcoo.smartpark.ui.record.ArrearsRecordListActivity;
import com.tourcoo.smartpark.ui.record.WaitSettleListActivity;
import com.tourcoo.smartpark.ui.report.DailyFeeReportActivity;
import com.tourcoo.smartpark.ui.record.RecordCarInfoConfirmActivity;
import com.tourcoo.smartpark.util.GridDividerItemDecoration;
import com.tourcoo.smartpark.util.StringUtil;
import com.tourcoo.smartpark.widget.dialog.AppUpdateDialog;
import com.tourcoo.smartpark.widget.dialog.BottomSheetDialog;
import com.tourcoo.smartpark.widget.dialog.CommonInputDialog;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.tourcoo.smartpark.constant.ParkConstant.PARK_STATUS_USED;
import static com.tourcoo.smartpark.socket.WebSocketConfig.SOCKET_URL;
import static com.tourcoo.smartpark.socket.WebSocketConfig.TIME_OUT;
import static com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity.EXTRA_SETTLE_RECORD_ID;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月30日11:22
 * @Email: 971613168@qq.com
 */
public class HomeActivity extends RxAppCompatActivity implements View.OnClickListener, Application.ActivityLifecycleCallbacks, OnRefreshListener, Callback.OnReloadListener {
    private Toolbar homeToolBar;
    private String mFilePath;
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;
    private boolean drawerOpenStatus = false;
    private RecyclerView parkingRecyclerView;
    private GridParkAdapter gridParkAdapter;
    private Handler mHandler = new Handler();
    private TextView tvCarRecord;
    private IosLoadingDialog loadingDialog;
    private Context mContext;
    private TextView tvUserName, tvUserLocation, tvUserWorkTime, tvTotalCarCount, tvActualIncome, tvTheoreticalIncome;
    public static final String EXTRA_SPACE_INFO = "EXTRA_SPACE_INFO";
    public static final int REQUEST_CODE_SIGN = 1002;
    private SmartRefreshLayout homeRefreshLayout;
    private boolean firstLoad = true;
    protected boolean mIsFirstBack = true;
    protected long mDelayBack = 1000L;
    private AppUpdateDialog appUpdateDialog;
    private boolean isDownloading = false;
    private LoadService loadService;
    private boolean isInstalling = false;
    private WebSocketManager socketManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        mContext = this;
        loadingDialog = new IosLoadingDialog(HomeActivity.this);
        initView();
        initStatusManager();
        initSpaceRecyclerView();
        initAdapterClick();
        setImmersionBar(true);
    }

    private void initView() {
        homeToolBar = findViewById(R.id.homeToolBar);
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.tvPay).setOnClickListener(this);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserWorkTime = findViewById(R.id.tvUserWorkTime);
        parkingRecyclerView = findViewById(R.id.parkingRecyclerView);
        tvCarRecord = findViewById(R.id.tvCarRecord);
        tvUserLocation = findViewById(R.id.tvUserLocation);
        tvTotalCarCount = findViewById(R.id.tvTotalCarCount);
        tvActualIncome = findViewById(R.id.tvActualIncome);
        tvTheoreticalIncome = findViewById(R.id.tvTheoreticalIncome);
        homeRefreshLayout = findViewById(R.id.homeRefreshLayout);
        homeRefreshLayout.setRefreshHeader(new ClassicsHeader(mContext));
        homeRefreshLayout.setOnRefreshListener(this);
        findViewById(R.id.tvSignIn).setOnClickListener(this);
        findViewById(R.id.tvSignOut).setOnClickListener(this);

        tvCarRecord.setOnClickListener(this);
        findViewById(R.id.tvPayExit).setOnClickListener(this);
        findViewById(R.id.tvHomeReportFee).setOnClickListener(this);
        findViewById(R.id.tvHomeArrears).setOnClickListener(this);

        findViewById(R.id.tvLogout).setOnClickListener(this);
        findViewById(R.id.tvHomeEditPass).setOnClickListener(this);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerOpenStatus = true;
                setImmersionBar(false);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerOpenStatus = false;
                setImmersionBar(true);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchDrawerLayout();
            }
        });
    }

    private void switchDrawerLayout() {
        if (drawerOpenStatus) {
            drawerLayout.close();
        } else {
            drawerLayout.open();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvLogout:
                showLogoutDialog();
                break;
            case R.id.tvCarRecord:
                skipSignSpace(null);
                break;
            case R.id.tvPayExit:
                skipExitPayEnter();
                break;
            case R.id.tvHomeReportFee:
                Intent intent3 = new Intent();
                intent3.setClass(HomeActivity.this, DailyFeeReportActivity.class);
                startActivity(intent3);
                closeDrawerLayout();
                break;
            case R.id.tvHomeArrears:
                CommonUtil.startActivity(HomeActivity.this, ArrearsRecordListActivity.class);
                break;
            case R.id.tvHomeEditPass:
                CommonUtil.startActivity(HomeActivity.this, EditPassActivity.class);
                closeDrawerLayout();
                break;
            case R.id.tvSignIn:
                requestSign(1);
                closeDrawerLayout();
                break;
            case R.id.tvSignOut:
                requestSign(0);
                closeDrawerLayout();
                break;
            case R.id.tvPay:
                CommonUtil.startActivity(HomeActivity.this, WaitSettleListActivity.class);
                closeDrawerLayout();
                break;
            default:
                break;
        }
    }


    private void initSpaceRecyclerView() {
        parkingRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        parkingRecyclerView.addItemDecoration(new GridDividerItemDecoration(SizeUtil.dp2px(7f), ContextCompat.getColor(this, R.color.whiteF5F5F5), false));
        gridParkAdapter = new GridParkAdapter();
        gridParkAdapter.bindToRecyclerView(parkingRecyclerView);
    }

    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected void setImmersionBar(boolean darkFont) {
        ImmersionBar.with(HomeActivity.this).titleBar(homeToolBar)
                .navigationBarColor(R.color.shape1).titleBarMarginTop(homeToolBar).statusBarDarkFont(darkFont).navigationBarDarkIcon(darkFont)
                .init();
    }

    @Override
    public void onBackPressed() {
        if (drawerOpenStatus) {
            drawerLayout.close();
            return;
        }
        quitApp();
    }


    protected void showLoading(String msg) {
        if (loadingDialog != null) {
            if (!TextUtils.isEmpty(msg)) {
                loadingDialog.setLoadingText(msg);
            }
            loadingDialog.show();
        }
    }


    protected void closeLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showUserInfo(AccountHelper.getInstance().getUserInfo());
        isInstalling = false;
    }


    private void showUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        AccountHelper.getInstance().setUserInfo(userInfo);
        String info = StringUtil.getNotNullValueLine(userInfo.getName()) + "/" + StringUtil.getNotNullValueLine(userInfo.getNumber());
        tvUserName.setText(info);
        tvUserLocation.setText(getNotNullStr(userInfo.getParking()));
        tvUserWorkTime.setText(getNotNullStr(userInfo.getDate()));
        tvTotalCarCount.setText(getNotNullStr(userInfo.getCarNum() + ""));
        tvTheoreticalIncome.setText(getNotNullStr(userInfo.getTheoreticalIncome() + ""));
        tvActualIncome.setText(getNotNullStr(userInfo.getActualIncome() + ""));
        showResetPassByCondition();
    }


    private String getNotNullStr(String value) {
        return StringUtil.getNotNullValueLine(value);
    }


    private void requestParkSpaceList(String message) {
        ApiRepository.getInstance().requestParkSpaceList().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseLoadingObserver<BaseResult<List<ParkSpaceInfo>>>(message) {
            @Override
            public void onRequestSuccess(BaseResult<List<ParkSpaceInfo>> entity) {
                if (entity == null) {
                    homeRefreshLayout.finishRefresh(false);
                    return;
                }
                homeRefreshLayout.finishRefresh(true);
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS) {
                    loadSpaceData(entity.getData());
                } else {
                    ToastUtil.showFailed(entity.getErrMsg());
                }
            }

            @Override
            public void onRequestError(Throwable throwable) {
                super.onRequestError(throwable);
                loadService.showCallback(MultiStatusErrorCallback.class);
                homeRefreshLayout.finishRefresh(false);
            }
        });
    }

    private void loadSpaceData(List<ParkSpaceInfo> parkSpaceInfoList) {
        if (parkSpaceInfoList == null) {
            loadService.showCallback(MultiEmptyStatusCallback.class);
            return;
        }
        loadService.showSuccess();
        gridParkAdapter.setNewData(parkSpaceInfoList);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        LogUtils.i("HomeActivity已被创建");
        StackUtil.getInstance().push(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        LogUtils.d("HomeActivity已被销毁");
        StackUtil.getInstance().pop(activity, false);
    }

    @Override
    protected void onDestroy() {
        closeLoading();
        releaseService();
        super.onDestroy();
    }

    private void releaseService() {
        if (socketManager != null) {
            socketManager.release();
            LogUtils.w("socket资源已释放");
        }
    }

    private void requestUserInfoAndParkList() {
        if (!NetworkUtil.isConnected(mContext)) {
            loadService.showCallback(MultiStatusNetErrorCallback.class);
            return;
        }
        ApiRepository.getInstance().requestUserInfo().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseLoadingObserver<BaseResult<UserInfo>>() {
            @Override
            public void onRequestSuccess(BaseResult<UserInfo> entity) {
                closeLoading();
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS && entity.getData() != null) {
                    showUserInfo(entity.getData());
                    if (firstLoad) {
                        requestParkSpaceList("正在获取车位信息");
                        firstLoad = false;
                    } else {
                        requestParkSpaceList("刷新中...");
                    }

                }
            }

            @Override
            public void onRequestError(Throwable throwable) {
                super.onRequestError(throwable);
                loadService.showCallback(MultiStatusErrorCallback.class);
            }
        });
    }

    private void showResetPassDialog() {
        CommonInputDialog dialog = new CommonInputDialog(mContext);
        dialog.create();
        dialog.setPositiveButtonClick("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(dialog.getInputString())) {
                    ToastUtil.showNormal("请先输入密码");
                    return;
                }
                requestResetNewPass(dialog.getInputString());
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void requestResetNewPass(String newPass) {
        ApiRepository.getInstance().requestUpdatePass(newPass).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseObserver<BaseResult<Object>>() {
            @Override
            public void onRequestSuccess(BaseResult<Object> entity) {
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS) {
                    ToastUtil.showSuccess(entity.getErrMsg());
                } else {
                    ToastUtil.showNormal(entity.getErrMsg());
                    showResetPassDialog();
                }
            }
        });
    }

    private void showResetPassByCondition() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AccountHelper.getInstance().isNeedResetPass()) {
                    showResetPassDialog();
                }
            }
        }, 300);
    }

    private void initAdapterClick() {
        gridParkAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (gridParkAdapter.getData().size() < position) {
                    return;
                }
                skipSignSpace(gridParkAdapter.getData().get(position));
            }
        });
    }

    private void skipSignSpace(ParkSpaceInfo parkSpaceInfo) {
        closeDrawerLayout();
        Intent intent = new Intent();
        if (parkSpaceInfo != null && parkSpaceInfo.getUsed() == PARK_STATUS_USED) {
            LogUtils.i("recordId=" + parkSpaceInfo.getRecordId() + ",parkId=" + parkSpaceInfo.getId());
            intent.putExtra(EXTRA_SETTLE_RECORD_ID, parkSpaceInfo.getRecordId());
            intent.putExtra(SettleFeeDetailActivity.EXTRA_PARK_ID, parkSpaceInfo.getId());
            intent.setClass(mContext, SettleFeeDetailActivity.class);
        } else {
            intent.setClass(mContext, RecordCarInfoConfirmActivity.class);
            intent.putExtra(EXTRA_SPACE_INFO, parkSpaceInfo);
        }
        startActivityForResult(intent, REQUEST_CODE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN:
                if (resultCode == RESULT_OK) {
                    //刷新列表
                    requestUserInfoAndParkList();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        requestUserInfoAndParkList();
    }

    /**
     * 离场收费入口
     */
    private void skipExitPayEnter() {
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, ExitPayFeeEnterActivity.class);
        startActivity(intent);
        closeDrawerLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSocket();
        requestUserInfoAndParkList();
        requestAppVersion();
    }


    private void showLogoutDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(mContext);
        BottomSheetDialog.SheetItemTextStyle style = new BottomSheetDialog.SheetItemTextStyle();
        style.setTextColor(ContextCompat.getColor(mContext, R.color.redFF4A5C));
        style.setTypeface(Typeface.DEFAULT);
        BottomSheetDialog.SheetItem item = new BottomSheetDialog.SheetItem("退出登录", style, new BottomSheetDialog.OnSheetItemClickListener() {

            @Override
            public void onClick(int which) {
                AccountHelper.getInstance().logout();
            }
        });


        dialog.addSheetItem(item);
        dialog.create().setTitle("退出登录后 会返回到登录页").show();
    }

    private void requestSign(int signIn) {
        ApiRepository.getInstance().requestSign(signIn).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseLoadingObserver<BaseResult<Object>>() {
            @Override
            public void onRequestSuccess(BaseResult<Object> entity) {
                if (entity == null) {
                    return;
                }
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS) {
                    ToastUtil.showSuccess(entity.getErrMsg());
                } else {
                    ToastUtil.showWarning(entity.getErrMsg());
                }
            }
        });
    }

    protected void quitApp() {
        QuitAppControl mQuitAppControl = UiManager.getInstance().getQuitAppControl();
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


    private void requestAppVersion() {
        if (appUpdateDialog != null && appUpdateDialog.isShowing()) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ApiRepository.getInstance().requestAppVersion(CommonUtil.getVersionName(mContext)).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseObserver<BaseResult<AppVersion>>() {
                    @Override
                    public void onRequestSuccess(BaseResult<AppVersion> entity) {
                        if (entity == null) {
                            return;
                        }
                        if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS && entity.getData() != null) {
                            handleUpdateCallback(entity.getData());
                        }
                    }
                });
            }
        }, 300);

    }


    private void handleUpdateCallback(AppVersion appVersion) {
        if (appVersion == null || !appVersion.isForce() || isInstalling) {
            return;
        }
        if (appUpdateDialog != null && appUpdateDialog.isShowing()) {
            return;
        }
        appUpdateDialog = new AppUpdateDialog(mContext).create(true);
        appUpdateDialog.setTitle("发现新版本").
                setDesc(StringUtil.getNotNullValueLine(appVersion.getDescription())).
                setContent("v" + appVersion.getVersion()).
                setPositiveButtonClick("立即更新", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isDownloading) {
                            ToastUtil.showWarning("当前正在下载中");
                            return;
                        }
                        downApk(appVersion.getApkPath());
                    }
                });
        appUpdateDialog.show();
    }


    private void downApk(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mFilePath = FileUtil.getCacheDir();
        } else {
            mFilePath = FileUtil.getExternalStorageDirectory();
        }
        String fileName = "/" + System.currentTimeMillis() + "_" + CommonUtil.getRandom(100000) + ".apk";
        isDownloading = true;
        appUpdateDialog.setPositiveText("正在下载");
        RetrofitHelper.getInstance().downloadFile(url).compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new DownloadObserver(mFilePath, fileName) {
                    @Override
                    public void onSuccess(File file) {
                        appUpdateDialog.dismiss();
                        isDownloading = false;
                        isInstalling = true;
                        LogUtils.i("文件路径:" + file.getPath());
                        RetrofitHelper.getInstance().setLogEnable(true);
                        doInstallApk(file);
                    }

                    @Override
                    public void onFail(Throwable e) {
                        ToastUtil.showFailed("下载失败" + e.getMessage());
                        isDownloading = false;
                        isInstalling = false;
                        appUpdateDialog.dismiss();
                        RetrofitHelper.getInstance().setLogEnable(true);
                    }

                    @Override
                    public void onProgress(float progress, long current, long total) {
                        updateDialogProgress(progress * 100);
                    }
                });
    }

    private void updateDialogProgress(float progress) {
        if (appUpdateDialog != null) {
            appUpdateDialog.setProgress(progress);
        }
    }

    private void initStatusManager() {
        //这里实例化多状态管理类
        loadService = LoadSir.getDefault().register(homeRefreshLayout, this);
    }

    @Override
    public void onReload(View v) {
        requestUserInfoAndParkList();
    }

    private void closeDrawerLayout() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.close();
            }
        }, 300);

    }


    private void doInstallApk(File file) {
        appUpdateDialog.setPositiveText("下载完成");
        showLoading("准备安装");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtil.installApk(file);
                    closeLoading();
                } catch (Exception e) {
                    closeLoading();
                    e.printStackTrace();
                    ToastUtil.showFailed("安装失败：" + e.toString());
                }
            }
        }, 1000);

    }

  /*  private void initWebServiceConnect() {
        if (!AccountHelper.getInstance().isLogin()) {
            return;
        }
        if (factory == null || ws == null) {
            factory = new WebSocketFactory().setConnectionTimeout(5000);
            try {
                ws = factory.createSocket("ws://192.168.0.238:8007/push", TIME_OUT);
                //设置心跳频率为20秒
                ws.setPingInterval(20 * 1000);
                ws.addHeader("Authorization", AccountHelper.getInstance().getAccessToken());
                ws.addListener(new WebSocketListener() {
                    @Override
                    public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                        LogUtils.d("----》onStateChanged:"+newState);
                        if(newState ==WebSocketState.CLOSED){
                            LogUtils.d("----》CLOSED:"+newState);
                            reConnect();
                        }
                    }

                    @Override
                    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                        LogUtils.i("----》onConnected");
                    }

                    @Override
                    public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
                        LogUtils.e("----》onConnectError");
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                        //这里设置重连
                        LogUtils.e("----》onDisconnected,"+"是否在主线程:" + (Looper.myLooper() == Looper.getMainLooper()));
                        reConnect();
                    }

                    @Override
                    public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onFrame");
                    }

                    @Override
                    public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onContinuationFrame");
                    }

                    @Override
                    public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onTextFrame");
                    }

                    @Override
                    public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onBinaryFrame");
                    }

                    @Override
                    public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onCloseFrame");
                    }

                    @Override
                    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onPingFrame");
                    }

                    @Override
                    public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onPongFrame");
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, String text) throws Exception {
                        LogUtils.i("----》onTextMessage="+text);
                    }

                    @Override
                    public void onTextMessage(WebSocket websocket, byte[] data) throws Exception {
                        LogUtils.i("----》onTextMessage字节数据长度:"+data.length);
                    }

                    @Override
                    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                        LogUtils.i("----》onBinaryMessage");
                    }

                    @Override
                    public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onSendingFrame");
                    }

                    @Override
                    public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onFrameSent");
                    }

                    @Override
                    public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                        LogUtils.i("----》onFrameUnsent");
                    }

                    @Override
                    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                        LogUtils.i("----》onThreadCreated");
                    }

                    @Override
                    public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                        LogUtils.i("----》onThreadStarted");
                    }

                    @Override
                    public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                        LogUtils.w("----》onThreadStopping");
                    }

                    @Override
                    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                        LogUtils.e("----》onError：" + "---" + cause.toString());
                    }

                    @Override
                    public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                        LogUtils.e("----》onFrameError：" + "---" + cause.toString());
                    }

                    @Override
                    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
                        LogUtils.e("----》onMessageError：" + "--->" + cause);
                    }

                    @Override
                    public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
                        LogUtils.e("----》onMessageDecompressionError：" + "---" + cause.toString());
                    }

                    @Override
                    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
                        LogUtils.e("----》onTextMessageError：" + "---" + cause.toString());
                    }

                    @Override
                    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                        LogUtils.e("----》onSendError：" + "---" + cause.toString());
                    }

                    @Override
                    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                        LogUtils.e("----》onUnexpectedError：" + "---" + cause.toString());
                    }

                    @Override
                    public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                        LogUtils.e("----》handleCallbackError：" + "---" + cause.toString());
                    }

                    @Override
                    public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
                        LogUtils.d("----》onSendingHandshake：" + "正在握手");
                    }
                });
                ws.connect();
            } catch (IOException | WebSocketException e) {
                e.printStackTrace();
                LogUtils.e("----》onConnectError：" + e.toString());
            }
        }
    }*/


    /*private void reConnect(){
        if (ws != null) {
            LogUtils.i("----》正在重连...是否在主线程:" + (Looper.myLooper() == Looper.getMainLooper()));
            try {
                if(ws.getState() ==){

                }
                ws.recreate(TIME_OUT).connect();
            } catch (WebSocketException | IOException e) {
                e.printStackTrace();
                LogUtils.e("e----》ws:" + (Looper.myLooper() == Looper.getMainLooper()));
            }

        }
    }*/

    private void initSocket() {
        if (!AccountHelper.getInstance().isLogin()) {
            ToastUtil.showFailed("登录已过期");
            CommonUtil.startActivity(mContext, LoginActivity.class);
            finish();
        }
        if (socketManager == null) {
            socketManager = new WebSocketManager(SOCKET_URL, AccountHelper.getInstance().getAccessToken());
            socketManager.connect();
            socketManager.setWebSocketListener(new WebSocketManager.WebSocketListener() {
                @Override
                public void onConnected(Map<String, List<String>> headers) {
                    LogUtils.d("---->OS. WebSocket onConnected:" + headers.size());
                }

                @Override
                public void onTextMessage(String text) {
                    LogUtils.d("---->OS. WebSocket onTextMessage:" + text);
                }
            });
        }

    }


}
