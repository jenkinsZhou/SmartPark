package com.tourcoo.smartpark;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tourcoo.smartpark.core.base.activity.BaseActivity;
import com.tourcoo.smartpark.core.control.QuitAppControl;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.ui.LoginActivity;

public class MainActivity extends BaseActivity implements QuitAppControl {
private TextView tvHello;
private TextView tvHello1;

    @Override
    public int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tvHello = findViewById(R.id.tvHello);
        tvHello1 = findViewById(R.id.tvHello1);
        tvHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showNormal("正常的吐司");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        tvHello1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showSuccess("成功的吐司");
            }
        });
    }

    @Override
    public long quipApp(boolean isFirst, Activity activity) {
        return 2000;
    }

    @Override
    public void onBackPressed() {
        quitApp();
    }
}