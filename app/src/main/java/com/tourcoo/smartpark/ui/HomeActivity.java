package com.tourcoo.smartpark.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.adapter.home.GridParkAdapter;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.control.RequestConfig;
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.tourcoo.smartpark.ui.account.AccountHelper;
import com.tourcoo.smartpark.ui.account.EditPassActivity;
import com.tourcoo.smartpark.ui.account.login.LoginActivity;
import com.tourcoo.smartpark.ui.fee.ExitPayFeeEnterActivity;
import com.tourcoo.smartpark.ui.record.RecordCarInfoConfirmActivity;
import com.tourcoo.smartpark.ui.report.FeeDailyReportActivity;
import com.tourcoo.smartpark.util.GridDividerItemDecoration;
import com.tourcoo.smartpark.util.StringUtil;
import com.tourcoo.smartpark.widget.dialog.CommonInputDialog;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;


import java.util.ArrayList;
import java.util.List;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月30日11:22
 * @Email: 971613168@qq.com
 */
public class HomeActivity extends RxAppCompatActivity implements View.OnClickListener, Application.ActivityLifecycleCallbacks, OnRefreshListener {
    private Toolbar homeToolBar;
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        mContext = this;
        loadingDialog = new IosLoadingDialog(HomeActivity.this);
        initView();
        initSpaceRecyclerView();
        initAdapterClick();
        requestUserInfo();
        requestParkSpaceList("正在获取车位信息");
        setImmersionBar(true);
    }

    private void initView() {
        homeToolBar = findViewById(R.id.homeToolBar);
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
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
        tvCarRecord.setOnClickListener(this);
        findViewById(R.id.tvPayExit).setOnClickListener(this);
        findViewById(R.id.tvHomeReportFee).setOnClickListener(this);
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
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.tvCarRecord:
                skipSignSpace(null);
                break;
            case R.id.tvPayExit:
                skipExitPayEnter();
                break;
            case R.id.tvHomeReportFee:
                Intent intent3 = new Intent();
                intent3.setClass(HomeActivity.this, FeeDailyReportActivity.class);
                startActivity(intent3);
                break;
            case R.id.tvHomeEditPass:
                CommonUtil.startActivity(HomeActivity.this, EditPassActivity.class);
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
        super.onBackPressed();
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
    }


    private void showUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        String info = StringUtil.getNotNullValueLine(userInfo.getName()) + "/" + StringUtil.getNotNullValueLine(userInfo.getNumber());
        tvUserName.setText(info);
        tvUserLocation.setText(getNotNullStr(userInfo.getParking()));
        tvUserWorkTime.setText(getNotNullStr(userInfo.getDate()));
        tvTotalCarCount.setText(getNotNullStr(userInfo.getCarNum() + ""));
        tvTheoreticalIncome.setText(getNotNullStr(userInfo.getTheoreticalIncome() + ""));
        tvActualIncome.setText(getNotNullStr(userInfo.getActualIncome() + ""));
        AccountHelper.getInstance().setUserInfo(userInfo);
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
                homeRefreshLayout.finishRefresh(false);
            }
        });
    }

    private void loadSpaceData(List<ParkSpaceInfo> parkSpaceInfoList) {
        if (parkSpaceInfoList == null) {
            return;
        }
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
        super.onDestroy();
    }

    private void requestUserInfo() {
        ApiRepository.getInstance().requestUserInfo().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseObserver<BaseResult<UserInfo>>() {
            @Override
            public void onRequestSuccess(BaseResult<UserInfo> entity) {
                closeLoading();
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS && entity.getData() != null) {
                    showUserInfo(entity.getData());
                }
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
        Intent intent = new Intent();
        intent.setClass(mContext, RecordCarInfoConfirmActivity.class);
        intent.putExtra(EXTRA_SPACE_INFO, parkSpaceInfo);
        startActivityForResult(intent, REQUEST_CODE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN:
                if (resultCode == RESULT_OK) {
                    //刷新列表
                    requestParkSpaceList("正在刷新");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        requestParkSpaceList("");
    }

    /**
     * 离场收费入口
     */
    private void skipExitPayEnter(){
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, ExitPayFeeEnterActivity.class);
        startActivity(intent);
    }
}
