package com.tourcoo.smartpark.core.delegate;

import android.app.Activity;
import android.view.View;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.ITitleView;
import com.tourcoo.smartpark.core.control.TitleBarViewControl;
import com.tourcoo.smartpark.core.utils.FindViewUtil;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日14:28
 * @Email: 971613168@qq.com
 */
public class TitleDelegate {
    public TitleBarView mTitleBar;
    private TitleBarViewControl mTitleBarViewControl;
    public TitleDelegate(View rootView, ITitleView iTitleBarView, final Class<?> cls) {
        mTitleBar = rootView.findViewById(R.id.commonTitleBar);
        if (mTitleBar == null) {
            mTitleBar = FindViewUtil.getTargetView(rootView, TitleBarView.class);
        }
        if (mTitleBar == null) {
            return;
        }
        LogUtils.i("class:" + cls.getSimpleName());
        //默认的MD风格返回箭头icon如使用该风格可以不用设置
        final Activity activity = StackUtil.getInstance().getActivity(cls);
        //设置TitleBarView 所有TextView颜色
        mTitleBar.setLeftTextDrawable(activity != null ? R.drawable.ic_back : 0)
                //.setLeftTextDrawableTintResource(R.color.colorTitleText)
                .setOnLeftTextClickListener(activity == null ? null : new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Activity activity = StackUtil.getInstance().getActivity(cls);
                        //增加判断避免快速点击返回键造成崩溃
                        if (activity == null) {
                            return;
                        }
                        activity.onBackPressed();
                    }
                })
//                .setTextColorResource(R.color.colorTitleText)
                //.setRightTextDrawableTintResource(R.color.colorTitleText)
                //.setActionTintResource(R.color.colorTitleText)
                .setTitleMainText(getTitle(activity));
        mTitleBarViewControl = UiManager.getInstance().getTitleBarViewControl();
        if (mTitleBarViewControl != null) {
            mTitleBarViewControl.createTitleBarViewControl(mTitleBar, cls);
        }
        iTitleBarView.beforeSetTitleBar(mTitleBar);
        iTitleBarView.setTitleBar(mTitleBar);
    }

    /**
     * 获取Activity 标题({@link Activity#getTitle()}获取不和应用名称一致才进行设置-因Manifest未设置Activity的label属性获取的是应用名称)
     *
     * @param activity
     * @return
     */
    private CharSequence getTitle(Activity activity) {
        if (activity != null) {
            CharSequence appName = CommonUtil.getAppName(activity);
            CharSequence label = activity.getTitle();
            if (label != null && !label.equals(appName)) {
                return label;
            }
        }
        return "";
    }

    public void onDestroy() {
        mTitleBar = null;
        mTitleBarViewControl = null;
        LogUtils.i("FastTitleDelegate", "onDestroy");
    }
}
