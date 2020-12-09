package com.tourcoo.smartpark.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;


import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity;
import com.tourcoo.smartpark.core.manager.RxJavaManager;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.utils.StatusBarUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;
import com.tourcoo.smartpark.ui.account.AccountHelper;
import com.tourcoo.smartpark.ui.account.login.LoginActivity;
import com.trello.rxlifecycle3.android.ActivityEvent;


/**
 * @Author: JenkinsZhou on 2018/11/19 14:25
 * @E-Mail: 971613168@qq.com
 * @Function: 欢迎页
 * @Description:
 */
public class SplashActivity extends BaseTitleActivity {


    @Override
    public void beforeSetContentView() {
//        LoggerManager.i(TAG, "isTaskRoot:" + isTaskRoot() + ";getCurrent:" + StackUtil.getInstance().getCurrent());
        //防止应用后台后点击桌面图标造成重启的假象---MIUI及Flyme上发现过(原生未发现)
        if (!isTaskRoot()) {
            finish();
            return;
        }
        super.beforeSetContentView();
    }


    @Override
    public void setTitleBar(TitleBarView titleBar) {
        titleBar.setStatusBarLightMode(false)
                .setVisibility(View.GONE);
    }

    @Override
    public int getContentLayout() {
        return R.layout.activity_splash;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        if (!isTaskRoot()) {
            return;
        }
        if (!StatusBarUtil.isSupportStatusBarFontChange()) {
            //隐藏状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
      /*  tvApp.setCompoundDrawablesWithIntrinsicBounds(null,
                DrawableUtil.setTintDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_launcher).mutate(), Color.WHITE)
                , null, null);*/
//        mContentView.setBackgroundResource(R.drawable.img_bg_login);
      /*  tvVersion.setText("V" + FrameUtil.getVersionName(mContext));
        tvVersion.setTextColor(Color.WHITE);
        tvCopyRight.setTextColor(Color.WHITE);*/
        RxJavaManager.getInstance().setTimer(500)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new BaseObserver<Long>() {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        skipByCondition();
                        finish();
                    }

                    @Override
                    public void onRequestSuccess(Long entity) {

                    }


                });
    }

    private void skipByCondition() {
        Intent intent = new Intent();
        if (AccountHelper.getInstance().isLogin()) {
            intent.setClass(mContext, HomeActivity.class);
        } else {
            intent.setClass(mContext, LoginActivity.class);
        }
        startActivity(intent);
    }

}
