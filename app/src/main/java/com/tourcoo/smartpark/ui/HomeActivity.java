package com.tourcoo.smartpark.ui;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

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
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.retrofit.UploadProgressBody;
import com.tourcoo.smartpark.core.retrofit.UploadRequestListener;
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.bean.ParkInfo;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.ui.account.EditPassActivity;
import com.tourcoo.smartpark.ui.account.LoginActivity;
import com.tourcoo.smartpark.ui.pay.PayResultActivity;
import com.tourcoo.smartpark.ui.record.RecordCarInfoConfirmActivity;
import com.tourcoo.smartpark.ui.report.FeeDailyReportActivity;
import com.tourcoo.smartpark.util.GridDividerItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        initView();
        initTestData();
        findViewById(R.id.tvCarRecord).setOnClickListener(this);
        findViewById(R.id.tvPayExit).setOnClickListener(this);
        findViewById(R.id.tvHomeReportFee).setOnClickListener(this);
        setImmersionBar(true);
    }

    private void initView() {
        homeToolBar = findViewById(R.id.homeToolBar);
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        parkingRecyclerView = findViewById(R.id.parkingRecyclerView);
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

private void test(){
        File file = new File("");
    RequestBody requestBody =  RequestBody.create(MediaType.parse("image/jpg"), file);
    MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
   /* ApiRepository.getInstance().getApiService().uploadFiles(body).enqueue(new Callback<BaseResult<List<String>>>() {
        @Override
        public void onResponse(@NotNull Call<BaseResult<List<String>>> call, Response<BaseResult<List<String>>> response) {

        }

        @Override
        public void onFailure(@NotNull Call<BaseResult<List<String>>> call, Throwable t) {

        }
    });*/
}

}
