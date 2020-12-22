package com.tourcoo.smartpark.core.base.activity;

import android.os.Bundle;
import android.view.View;

import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.core.control.IHttpRequestControl;
import com.tourcoo.smartpark.core.control.IMultiStatusViewControl;
import com.tourcoo.smartpark.core.control.NetDataSuccessCallback;

/**
 * @author :JenkinsZhou
 * @description : 多状态布局
 * @company :途酷科技
 * @date 2020年12月22日11:44
 * @Email: 971613168@qq.com
 */
public abstract class BaseTitleMultiStatusActivity extends BaseTitleActivity implements IMultiStatusViewControl {
    protected LoadService mStatusManager;

    @Override
    public void beforeInitView(Bundle savedInstanceState) {
        super.beforeInitView(savedInstanceState);
        initStatusManager();
    }

    private void initStatusManager() {
        //优先使用当前配置
        View contentView = getMultiStatusView();
        if (contentView == null) {
            return;
        }
        //这里实例化多状态管理类
        mStatusManager = LoadSir.getDefault().register(contentView, this);
    }

    @Override
    public IHttpRequestControl getHttpRequestControl() {
        return new IHttpRequestControl() {
            @Override
            public View getContentView() {
                return getMultiStatusView();
            }

            @Override
            public void handleSuccessData(BaseResult<?> netData) {
                handleNetSuccessCallback(netData);
            }

            @Override
            public LoadService getStatusLayoutManager() {
                return mStatusManager;
            }
        };
    }

    protected abstract View getMultiStatusView();

    protected abstract void handleNetSuccessCallback(BaseResult<?> data);
}
