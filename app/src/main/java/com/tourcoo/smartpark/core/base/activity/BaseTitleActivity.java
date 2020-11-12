package com.tourcoo.smartpark.core.base.activity;

import android.os.Bundle;

import com.gyf.immersionbar.ImmersionBar;
import com.tourcoo.smartpark.core.control.ITitleView;
import com.tourcoo.smartpark.core.utils.FindViewUtil;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日14:09
 * @Email: 971613168@qq.com
 */
public abstract class BaseTitleActivity extends BaseActivity implements ITitleView {

    protected TitleBarView mTitleBar;

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        super.beforeInitView(savedInstanceState);
        mTitleBar = FindViewUtil.getTargetView(mContentView, TitleBarView.class);
        ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .navigationBarDarkIcon(true)
                .init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTitleBar = null;
    }
}
