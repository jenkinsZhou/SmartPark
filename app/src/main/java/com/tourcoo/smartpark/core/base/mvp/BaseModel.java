package com.tourcoo.smartpark.core.base.mvp;

import androidx.lifecycle.LifecycleOwner;

import com.trello.rxlifecycle3.LifecycleTransformer;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月24日14:54
 * @Email: 971613168@qq.com
 */
public class BaseModel {

    private LifecycleTransformer lifecycleTransformer;

    public BaseModel(LifecycleTransformer lifecycleTransformer) {
        this.lifecycleTransformer = lifecycleTransformer;
    }

    /**
     * 返回生命周期所有者
     *
     * @return LifecycleOwner
     */
    protected LifecycleTransformer getLifecycleTransformer() {
        return lifecycleTransformer;
    }
}
