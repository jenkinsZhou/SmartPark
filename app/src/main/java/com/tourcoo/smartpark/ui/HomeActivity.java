package com.tourcoo.smartpark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.gyf.immersionbar.ImmersionBar;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.utils.ToastUtil;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity);
        initView();
    }

    private void initView() {
        homeToolBar = findViewById(R.id.homeToolBar);
        ImmersionBar.with(this)
                .statusBarColor(R.color.colorWhite).statusBarDarkFont(true, 0.5f).init();
        ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.tvLogout).setOnClickListener(this);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                drawerOpenStatus = true;
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                drawerOpenStatus = false;
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
            case  R.id.tvLogout:
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
