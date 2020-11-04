package com.tourcoo.smartpark.core.control;

import com.kingja.loadsir.core.LoadService;

/**
 * @author :JenkinsZhou
 * @description :用于全局设置多状态布局
 * @company :途酷科技
 * @date 2020年10月28日15:38
 * @Email: 971613168@qq.com
 */
public interface MultiStatusView {

    void setMultiStatusView(LoadService loadService, IRefreshView iRefreshView);
}
