package com.tourcoo.smartpark.ui;

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

import com.gyf.immersionbar.ImmersionBar;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.adapter.home.HomeGridParkAdapter;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.control.RequestConfig;
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver;
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.tourcoo.smartpark.ui.account.AccountHelper;
import com.tourcoo.smartpark.ui.account.EditPassActivity;
import com.tourcoo.smartpark.ui.account.login.LoginActivity;
import com.tourcoo.smartpark.ui.record.RecordCarInfoConfirmActivity;
import com.tourcoo.smartpark.ui.report.FeeDailyReportActivity;
import com.tourcoo.smartpark.util.GridDividerItemDecoration;
import com.tourcoo.smartpark.util.StringUtil;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;


import java.util.List;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月30日11:22
 * @Email: 971613168@qq.com
 */
public class HomeActivity extends RxAppCompatActivity implements View.OnClickListener {
    private Toolbar homeToolBar;
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;
    private boolean drawerOpenStatus = false;
    private RecyclerView parkingRecyclerView;
    private HomeGridParkAdapter homeGridParkAdapter;
    private Handler mHandler = new Handler();
    private TextView tvCarRecord;
    private IosLoadingDialog loadingDialog;
    private TextView tvUserName, tvUserLocation, tvUserWorkTime, tvTotalCarCount, tvActualIncome, tvTheoreticalIncome;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        setImmersionBar(true);
        loadingDialog = new IosLoadingDialog(HomeActivity.this, "加载中...");
        initView();
        initSpaceRecyclerView();
        requestParkSpaceList();
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
                Intent intent1 = new Intent();
                intent1.setClass(HomeActivity.this, EditPassActivity.class);
                startActivity(intent1);
                break;
            case R.id.tvPayExit:
                Intent intent2 = new Intent();
                intent2.setClass(HomeActivity.this, RecordCarInfoConfirmActivity.class);
                startActivity(intent2);
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
        homeGridParkAdapter = new HomeGridParkAdapter();
        homeGridParkAdapter.bindToRecyclerView(parkingRecyclerView);
       /* List<ParkSimpleInfo> parkInfoList = new ArrayList<>();
        ParkSimpleInfo parkInfo;
        int size = 10;
        for (int i = 0; i < size; i++) {
            parkInfo = new ParkSimpleInfo();
            parkInfo.setParkingNum("A02568" + i);
            parkInfo.setPlantNum("皖A·761M" + i);
            if (i % 2 != 0) {
                parkInfo.setStatus(1);
            } else {
                parkInfo.setStatus(0);
            }
            parkInfoList.add(parkInfo);

        }
        homeGridParkAdapter.setNewData(parkInfoList);
        homeGridParkAdapter.bindToRecyclerView(parkingRecyclerView);*/
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
    }


    private String getNotNullStr(String value) {
        return StringUtil.getNotNullValueLine(value);
    }


    private void requestParkSpaceList() {
        ApiRepository.getInstance().requestParkSpaceList().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(new BaseLoadingObserver<BaseResult<List<ParkSpaceInfo>>>("正在获取车位列表...") {
            @Override
            public void onRequestSuccess(BaseResult<List<ParkSpaceInfo>> entity) {
                if (entity == null) {
                    return;
                }
                if (entity.getCode() == RequestConfig.REQUEST_SUCCESS_CODE) {
                    loadSpaceData(entity.getData());
                } else {
                    ToastUtil.showFailed(entity.getErrMsg());
                }
            }
        });
    }

    private void loadSpaceData(List<ParkSpaceInfo> parkSpaceInfoList) {
        if (parkSpaceInfoList == null) {
            return;
        }
        homeGridParkAdapter.setNewData(parkSpaceInfoList);
    }

}
