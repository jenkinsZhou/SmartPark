package com.tourcoo.smartpark.core.base.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.kingja.loadsir.core.LoadService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.control.IHttpRequestControl;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.delegate.RefreshLoadDelegate;
import com.tourcoo.smartpark.core.delegate.TitleDelegate;
import com.tourcoo.smartpark.core.multi_status.LoadingStatusCallback;
import com.tourcoo.smartpark.core.utils.ToastUtil;

import static com.tourcoo.smartpark.core.control.RequestConfig.FIRST_PAGE;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日15:04
 * @Email: 971613168@qq.com
 */
public abstract class BaseRefreshLoadActivity<T> extends BaseTitleActivity implements IRefreshLoadView<T> {

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
        mDefaultPage = FIRST_PAGE;
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

    @Override
    public void onReload(View v) {
        TextView tvRefresh = v.findViewById(R.id.tvRefresh);
        if (tvRefresh != null) {
            tvRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doRefresh();
                }
            });
        } else {
            doRefresh();
        }

    }


    private void doRefresh() {
        if (mStatusManager != null) {
            mStatusManager.showCallback(LoadingStatusCallback.class);
        }
        loadPageData(mDefaultPage);
    }

}
