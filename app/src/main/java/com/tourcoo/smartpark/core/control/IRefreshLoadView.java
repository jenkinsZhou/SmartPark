package com.tourcoo.smartpark.core.control;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.loadmore.LoadMoreView;

/**
 * @author :JenkinsZhou
 * @description :下拉加载及上拉刷新
 * @company :途酷科技
 * @date 2020年10月28日15:43
 * @Email: 971613168@qq.com
 */
public interface IRefreshLoadView<T> extends IRefreshView, BaseQuickAdapter.RequestLoadMoreListener {
    /**
     * 使用BaseRecyclerViewAdapterHelper作为上拉加载的实现方式
     * 如果使用ListView或GridView等需要自己去实现上拉加载更多的逻辑
     *
     * @return BaseRecyclerViewAdapterHelper的实现类
     */
    BaseQuickAdapter<T, BaseViewHolder> getAdapter();

   default RecyclerView.LayoutManager getLayoutManager(){
        return null;
    }

    /**
     * 获取加载更多布局
     *
     * @return
     */
    default LoadMoreView getLoadMoreView() {
        return null;
    }


    /**
     * 触发下拉或上拉刷新操作
     *
     * @param page
     */
    void loadPageData(int page);


    /**
     * 是否支持加载更多功能
     *
     * @return
     */
    default boolean isLoadMoreEnable() {
        return true;
    }


    /**
     * item是否有点击事件
     *
     * @return
     */
    default boolean isItemClickEnable() {
        return true;
    }


    /**
     * item点击回调
     *
     * @param adapter
     * @param view
     * @param position
     */
    default void onItemClicked(BaseQuickAdapter<T, BaseViewHolder> adapter, View view, int position) {

    }

    /**
     * 设置全局监听接口
     *
     * @return
     */
    IHttpRequestControl getIHttpRequestControl();
}
