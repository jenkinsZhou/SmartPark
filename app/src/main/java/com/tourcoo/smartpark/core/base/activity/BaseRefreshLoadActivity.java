package com.tourcoo.smartpark.core.base.activity;

import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.kingja.loadsir.core.LoadService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tourcoo.smartpark.core.control.IHttpRequestControl;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.delegate.RefreshLoadDelegate;
import com.tourcoo.smartpark.core.delegate.TitleDelegate;

import static com.tourcoo.smartpark.core.control.RequestConfig.FIRST_PAGE;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日15:04
 * @Email: 971613168@qq.com
 */
public abstract class BaseRefreshLoadActivity<T> extends BaseTitleActivity  implements IRefreshLoadView<T> {

    protected SmartRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LoadService mStatusManager;
    private BaseQuickAdapter mQuickAdapter;
    protected int mDefaultPage = FIRST_PAGE;
    protected int mDefaultPageSize = 10;

    protected RefreshLoadDelegate<T> mFastRefreshLoadDelegate;
    private Class<?> mClass;

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        super.beforeInitView(savedInstanceState);
        mClass = getClass();
        new TitleDelegate(mContentView, this, getClass());
        mFastRefreshLoadDelegate = new RefreshLoadDelegate<>(mContentView, this, getClass());
        mRecyclerView = mFastRefreshLoadDelegate.mRecyclerView;
        mRefreshLayout = mFastRefreshLoadDelegate.mRefreshLayout;
        mStatusManager = mFastRefreshLoadDelegate.mStatusManager;
        mQuickAdapter = mFastRefreshLoadDelegate.mAdapter;
    }

    @Override
    public IHttpRequestControl getIHttpRequestControl() {
        IHttpRequestControl requestControl = new IHttpRequestControl() {
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
        return requestControl;
    }


    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        mDefaultPage =FIRST_PAGE;
        mFastRefreshLoadDelegate.setLoadMore(isLoadMoreEnable());
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
    protected void onDestroy() {
        if (mFastRefreshLoadDelegate != null) {
            mFastRefreshLoadDelegate.onDestroy();
        }
        super.onDestroy();
    }
}
