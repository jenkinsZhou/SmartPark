package com.tourcoo.smartpark.core.delegate;

import android.content.Context;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.utils.FindViewUtil;
import com.tourcoo.smartpark.core.widget.CommonLoadMoreView;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日15:05
 * @Email: 971613168@qq.com
 */
public class RefreshLoadDelegate<T> {

    public SmartRefreshLayout mRefreshLayout;
    public RecyclerView mRecyclerView;
    public BaseQuickAdapter<T, BaseViewHolder> mAdapter;
    public LoadService mStatusManager;
    private IRefreshLoadView<T> mRefreshLoadView;
    private RefreshDelegate mRefreshDelegate;
    private Context mContext;
    private UiManager mManager;
    public View mRootView;
    private Class<?> mTargetClass;

    public RefreshLoadDelegate(View rootView, IRefreshLoadView<T> iRefreshLoadView, Class<?> cls) {
        this.mRootView = rootView;
        this.mRefreshLoadView = iRefreshLoadView;
        this.mTargetClass = cls;
        this.mContext = rootView.getContext().getApplicationContext();
        this.mManager = UiManager.getInstance();
        if (mRefreshLoadView == null) {
            return;
        }
        mRefreshDelegate = new RefreshDelegate(rootView, iRefreshLoadView);
        mRefreshLayout = mRefreshDelegate.mRefreshLayout;
        getRecyclerView(rootView);
        initRecyclerView();
        setStatusManager();
    }

    /**
     * 初始化RecyclerView配置
     */
    protected void initRecyclerView() {
        if (mRecyclerView == null) {
            return;
        }
        if (UiManager.getInstance().getRecyclerViewControl() != null) {
            UiManager.getInstance().getRecyclerViewControl().setRecyclerView(mRecyclerView, mTargetClass);
        }
        mAdapter = mRefreshLoadView.getAdapter();
        mRecyclerView.setLayoutManager(mRefreshLoadView.getLayoutManager() == null ? new LinearLayoutManager(mContext) : mRefreshLoadView.getLayoutManager());
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRecyclerView.setAdapter(mAdapter);
        if (mAdapter != null) {
            setLoadMore(mRefreshLoadView.isLoadMoreEnable());
            //先判断是否Activity/Fragment设置过;再判断是否有全局设置;最后设置默认
            mAdapter.setLoadMoreView(mRefreshLoadView.getLoadMoreView() != null
                    ? mRefreshLoadView.getLoadMoreView() :
                    mManager.getLoadMoreFoot() != null ?
                            mManager.getLoadMoreFoot().createDefaultLoadMoreView(mAdapter) :
                            new CommonLoadMoreView(mContext).getBuilder().build());
            if (mRefreshLoadView.isItemClickEnable()) {
                mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        mRefreshLoadView.onItemClicked(adapter, view, position);
                    }

                });
            }
        }
    }

    public void setLoadMore(boolean enable) {
        if (mAdapter != null) {
            mAdapter.setOnLoadMoreListener(enable ? mRefreshLoadView : null, mRecyclerView);
        }
    }

    private void setStatusManager() {
        //优先使用当前配置
        View contentView = mRefreshLoadView.getMultiStatusContentView();
        if (contentView == null) {
            contentView = mRefreshLayout;
        }
        if (contentView == null) {
            contentView = mRecyclerView;
        }
        if (contentView == null) {
            contentView = mRootView;
        }
        if (contentView == null) {
            return;
        }
        //这里实例化多状态管理类
        mStatusManager  = LoadSir.getDefault().register(contentView,mRefreshLoadView );
        if (mManager != null && mManager.getMultiStatusView() != null) {
            mManager.getMultiStatusView().setMultiStatusView(mStatusManager, mRefreshLoadView);
        }
    }

    /**
     * 获取布局RecyclerView
     *
     * @param rootView
     */
    private void getRecyclerView(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.commonRecyclerView);
        if (mRecyclerView == null) {
            mRecyclerView = FindViewUtil.getTargetView(rootView, RecyclerView.class);
        }
    }

    /**
     * 与Activity 及Fragment onDestroy 及时解绑释放避免内存泄露
     */
    public void onDestroy() {
        if (mRefreshDelegate != null) {
            mRefreshDelegate.onDestroy();
            mRefreshDelegate = null;
        }
        mRefreshLayout = null;
        mRecyclerView = null;
        mAdapter = null;
        mStatusManager = null;
        mRefreshLoadView = null;
        mContext = null;
        mManager = null;
        mRootView = null;
        mTargetClass = null;
        LogUtils.i("RefreshLoadDelegate", "onDestroy");
    }
}
