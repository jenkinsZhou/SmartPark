package com.tourcoo.smartpark.core.control;

import com.kingja.loadsir.core.LoadService;

/**
 * @author :JenkinsZhou
 * @description :多状态布局管理接口
 * @company :途酷科技
 * @date 2020年10月29日15:21
 * @Email: 971613168@qq.com
 */
public interface StatusLayoutControl {
    /**
     * 获取多布局状态管理
     *
     * @return
     */
    LoadService getStatusLayoutManager();
}
