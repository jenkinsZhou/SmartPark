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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gyf.immersionbar.ImmersionBar;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.adapter.home.HomeGridParkAdapter;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.bean.ParkInfo;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.tourcoo.smartpark.threadpool.ThreadPoolManager;
import com.tourcoo.smartpark.ui.account.EditPassActivity;
import com.tourcoo.smartpark.ui.account.LoginActivity;
import com.tourcoo.smartpark.ui.record.RecordCarInfoConfirmActivity;
import com.tourcoo.smartpark.ui.report.FeeDailyReportActivity;
import com.tourcoo.smartpark.util.GridDividerItemDecoration;
import com.tourcoo.smartpark.widget.orc.OrcPlantInitListener;
import com.tourcoo.smartpark.widget.orc.PredictorWrapper;


import java.util.ArrayList;
import java.util.List;


/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月30日11:22
 * @Email: 971613168@qq.com
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar homeToolBar;
    private ImageView ivMenu;
    private DrawerLayout drawerLayout;
    private boolean drawerOpenStatus = false;
    private RecyclerView parkingRecyclerView;
    private HomeGridParkAdapter homeGridParkAdapter;
    private Handler mHandler = new Handler();
    private TextView tvCarRecord;
    private IosLoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        loadingDialog = new IosLoadingDialog(HomeActivity.this, "加载中...");
        initView();
        intiPlantOrcSdk();
        initTestData();
        setImmersionBar(true);
    }

    private void initView() {
        homeToolBar = findViewById(R.id.homeToolBar);
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        parkingRecyclerView = findViewById(R.id.parkingRecyclerView);
        tvCarRecord = findViewById(R.id.tvCarRecord);
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


    private void initTestData() {
        parkingRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        parkingRecyclerView.addItemDecoration(new GridDividerItemDecoration(SizeUtil.dp2px(7f), ContextCompat.getColor(this, R.color.whiteF5F5F5), false));
        homeGridParkAdapter = new HomeGridParkAdapter();
        List<ParkInfo> parkInfoList = new ArrayList<>();
        ParkInfo parkInfo;
        int size = 10;
        for (int i = 0; i < size; i++) {
            parkInfo = new ParkInfo();
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
        homeGridParkAdapter.bindToRecyclerView(parkingRecyclerView);
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

    private void intiPlantOrcSdk() {
        showLoading("正在初始化组件...");
        ThreadPoolManager.getThreadPoolProxy().execute(() -> {
            PredictorWrapper.setListener(new OrcPlantInitListener() {
                @Override
                public void initSuccess() {
                    closeLoading();
                }

                @Override
                public void initFailed() {
                    closeLoading();
                    ToastUtil.showFailed("车牌识别sdk初始化失败");
                }
            });
            //授权初始化
            if (!PredictorWrapper.initLicense(HomeActivity.this)) {
                return;
            }
            // 初始化模型
            PredictorWrapper.initModel(HomeActivity.this);
        });
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
}
