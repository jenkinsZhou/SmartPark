package com.tourcoo.smartpark.core.control;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.kingja.loadsir.core.LoadService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

/**
 * @author :JenkinsZhou
 * @description :下拉刷新、列表、多状态布局的全局回调接口
 * @company :途酷科技
 * @date 2020年10月28日9:55
 * @Email: 971613168@qq.com
 */
public interface IHttpRequestControl extends StatusLayoutControl{

    /**
     * 获取刷新布局
     *
     * @return
     */
    SmartRefreshLayout getRefreshLayout();


    /**
     * 获取RecyclerView Adapter
     *
     * @return
     */
    BaseQuickAdapter getRecyclerAdapter();



    /**
     * 获取当前页码
     * @return
     */
    int getCurrentPage();

    /**
     * 获取每页数据数量
     * @return
     */
    int getPageSize();

    /**
     * 获取调用类
     * @return
     */
    Class<?> getRequestClass();
}
