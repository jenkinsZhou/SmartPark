package com.tourcoo.smartpark.core.base.fragment;

import android.os.Bundle;

import com.tourcoo.smartpark.core.control.ITitleView;
import com.tourcoo.smartpark.core.utils.FindViewUtil;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :设置有TitleBar的基类Fragment
 * @company :途酷科技
 * @date 2020年10月29日16:22
 * @Email: 971613168@qq.com
 */
public abstract class BaseTitleFragment extends BaseFragment implements ITitleView {

    protected TitleBarView mTitleBar;

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        super.beforeInitView(savedInstanceState);
        mTitleBar = FindViewUtil.getTargetView(mContentView, TitleBarView.class);
    }
}
