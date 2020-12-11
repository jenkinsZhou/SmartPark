package com.tourcoo.smartpark.core.multi_status;

import com.kingja.loadsir.callback.Callback;
import com.tourcoo.smartpark.R;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日11:29
 * @Email: 971613168@qq.com
 */
public class MultiEmptyStatusCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.multi_status_layout_empty;
    }
}
