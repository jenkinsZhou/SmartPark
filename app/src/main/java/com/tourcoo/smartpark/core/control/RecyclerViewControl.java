package com.tourcoo.smartpark.core.control;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author :JenkinsZhou
 * @description : 列表布局全局控制RecyclerView
 * @company :途酷科技
 * @date 2020年10月28日17:08
 * @Email: 971613168@qq.com
 */
public interface RecyclerViewControl {
    /**
     * 全局设置
     *
     * @param recyclerView
     * @param cls
     */
    void setRecyclerView(RecyclerView recyclerView, Class<?> cls);
}
