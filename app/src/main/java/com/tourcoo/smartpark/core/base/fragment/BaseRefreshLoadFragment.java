package com.tourcoo.smartpark.core.base.fragment;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.kingja.loadsir.core.LoadService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tourcoo.smartpark.core.control.IHttpPageRequestControl;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.delegate.RefreshLoadDelegate;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日16:24
 * @Email: 971613168@qq.com
 */
public abstract class BaseRefreshLoadFragment<T> extends BaseFragment implements IRefreshLoadView<T> {

    protected SmartRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LoadService mStatusManager;
    protected int mDefaultPage = 0;
    protected int mDefaultPageSize = 10;
    private BaseQuickAdapter mQuickAdapter;
    private Class<?> mClass;

    protected RefreshLoadDelegate<T> mFastRefreshLoadDelegate;

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        super.beforeInitView(savedInstanceState);
        mClass = this.getClass();
        mFastRefreshLoadDelegate = new RefreshLoadDelegate<>(mContentView, this, mClass);
        mRecyclerView = mFastRefreshLoadDelegate.mRecyclerView;
        mRefreshLayout = mFastRefreshLoadDelegate.mRefreshLayout;
        mStatusManager = mFastRefreshLoadDelegate.mStatusManager;
        mQuickAdapter = mFastRefreshLoadDelegate.mAdapter;
        mFastRefreshLoadDelegate.setLoadMore(isLoadMoreEnable());
    }

    @Override
    public IHttpPageRequestControl getIHttpRequestControl() {
        return new IHttpPageRequestControl() {
            @Override
            public SmartRefreshLayout getRefreshLayout() {
                return mRefreshLayout;
            }

            @Override
            public BaseQuickAdapter getRecyclerAdapter() {
                return mQuickAdapter;
            }

            @Override
            public LoadService getStatusLayoutManager() {
                return mStatusManager;
            }

            @Override
            public int getCurrentPage() {
                return mDefaultPage;
            }

            @Override
            public int getPageSize() {
                return mDefaultPageSize;
            }

            @Override
            public Class<?> getRequestClass() {
                return mClass;
            }
        };
    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        mDefaultPage = 0;
        loadPageData(mDefaultPage);
    }

    @Override
    public void onLoadMoreRequested() {
        loadPageData(++mDefaultPage);
    }

    @Override
    public void loadData() {
        loadPageData(mDefaultPage);
    }

    @Override
    public void onDestroy() {
        if (mFastRefreshLoadDelegate != null) {
            mFastRefreshLoadDelegate.onDestroy();
        }
        super.onDestroy();
    }
}
