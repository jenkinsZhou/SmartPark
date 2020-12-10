package com.tourcoo.smartpark.core.multi_status;

import com.kingja.loadsir.callback.Callback;
import com.tourcoo.smartpark.R;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月10日15:04
 * @Email: 971613168@qq.com
 */
public class LoadingStatusCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.multi_status_layout_loading;
    }
}
