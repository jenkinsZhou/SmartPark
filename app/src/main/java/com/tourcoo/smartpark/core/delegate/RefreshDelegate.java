package com.tourcoo.smartpark.core.delegate;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.apkfuns.logutils.LogUtils;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.core.control.IRefreshView;
import com.tourcoo.smartpark.core.utils.FindViewUtil;

/**
 * @author :JenkinsZhou
 * @description :刷新代理类
 * @company :途酷科技
 * @date 2020年10月29日14:36
 * @Email: 971613168@qq.com
 */
public class RefreshDelegate {

    public SmartRefreshLayout mRefreshLayout;
    public View mRootView;
    private UiManager mManager;
    private IRefreshView mRefreshView;
    private Context mContext;
    private LoadService loadService;


    public RefreshDelegate(View rootView, IRefreshView refreshView) {
        this.mRootView = rootView;
        this.mRefreshView = refreshView;
        this.mContext = rootView.getContext().getApplicationContext();
        this.mManager = UiManager.getInstance();
        if (mRefreshView == null) {
            return;
        }
        if (mRootView == null) {
            mRootView = mRefreshView.getContentView();
        }
        if (mRootView == null) {
            return;
        }
        getRefreshLayout(rootView);
        initRefreshHeader();
        if (mRefreshLayout != null) {
            mRefreshView.setSmartRefreshLayout(mRefreshLayout);
        }
        setStatusManager();
    }

    /**
     * 初始化刷新头配置
     */
    protected void initRefreshHeader() {
        if (mRefreshLayout == null) {
            return;
        }
        if (mRefreshLayout.getRefreshHeader() != null) {
            return;
        }
        mRefreshLayout.setRefreshHeader(
                mManager.getDefaultRefreshHeader() != null ?
                        mManager.getDefaultRefreshHeader().createRefreshHeader(mContext, mRefreshLayout) :
                        new ClassicsHeader(mContext).setSpinnerStyle(SpinnerStyle.Translate));
        mRefreshLayout.setOnRefreshListener(mRefreshView);
        mRefreshLayout.setEnableRefresh(mRefreshView.isRefreshEnable());
    }

    /**
     * 获取布局里的刷新Layout
     *
     * @param rootView
     * @return
     */
    private void getRefreshLayout(View rootView) {
        mRefreshLayout = rootView.findViewById(R.id.commonRefreshLayout);
        if (mRefreshLayout == null) {
            mRefreshLayout = FindViewUtil.getTargetView(rootView, SmartRefreshLayout.class);
        }
        //原布局无SmartRefreshLayout 将rootView 从父布局移除并添加进SmartRefreshLayout 将SmartRefreshLayout作为新的
        if (mRefreshLayout == null && mRefreshView.isRefreshEnable()) {
            ViewGroup parentLayout;
            ViewGroup.LayoutParams params = mRootView.getLayoutParams();

            if (mRootView.getParent() != null) {
                parentLayout = (ViewGroup) mRootView.getParent();
            } else {
                parentLayout = mRootView.getRootView().findViewById(android.R.id.content);
            }
            //如果此时parentLayout为null 可能mRootView为Fragment 根布局
            if (parentLayout == null) {
                return;
            }
            int index = parentLayout.indexOfChild(mRootView);
            //先移除rootView
            parentLayout.removeView(mRootView);
            //新建SmartRefreshLayout
            mRefreshLayout = new SmartRefreshLayout(mRootView.getContext());
            //将rootView添加进SmartRefreshLayout
            mRefreshLayout.addView(mRootView);
            //将SmartRefreshLayout添加进parentLayout
            parentLayout.addView(mRefreshLayout, index, params);
        }
    }


    /**
     * 与Activity 及Fragment onDestroy 及时解绑释放避免内存泄露
     */
    public void onDestroy() {
        mRefreshLayout = null;
        mContext = null;
        mManager = null;
        mRootView = null;
        LogUtils.d("RefreshDelegate", "onDestroy");
    }


    private void setStatusManager() {
        //优先使用当前配置
        View contentView = mRefreshView.getMultiStatusContentView();
        if (contentView == null) {
            contentView = mRefreshLayout;
        }
        if (contentView == null) {
            contentView = mRootView;
        }
        if (contentView == null) {
            return;
        }
        //这里实例化多状态管理类
        loadService  = LoadSir.getDefault().register(contentView,mRefreshView);
        if (mManager != null && mManager.getMultiStatusView() != null) {
            mManager.getMultiStatusView().setMultiStatusView(loadService, mRefreshView);
        }
    }
}
