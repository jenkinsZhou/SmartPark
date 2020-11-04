package com.tourcoo.smartpark.core.base.fragment;

import android.os.Bundle;

import com.tourcoo.smartpark.core.control.ITitleView;
import com.tourcoo.smartpark.core.delegate.TitleDelegate;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日16:27
 * @Email: 971613168@qq.com
 */
public abstract class BaseTitleRefreshLoadFragment<T> extends BaseRefreshLoadFragment<T> implements ITitleView {
    protected TitleBarView mTitleBar;

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        mTitleBar = new TitleDelegate(mContentView, this, getClass()).mTitleBar;
        super.beforeInitView(savedInstanceState);
    }
}
