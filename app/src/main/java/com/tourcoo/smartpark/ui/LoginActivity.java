package com.tourcoo.smartpark.ui;

import android.os.Bundle;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日17:08
 * @Email: 971613168@qq.com
 */
public class LoginActivity extends BaseTitleActivity {
    @Override
    public int getContentLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    public void setTitleBar(TitleBarView titleBar) {
        titleBar.setVisibility(View.GONE);
    }


}
