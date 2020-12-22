package com.tourcoo.smartpark.core.control;

import com.tourcoo.smartpark.bean.BaseResult;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年12月22日11:42
 * @Email: 971613168@qq.com
 */
public interface NetDataSuccessCallback {

    void  handleSuccessData(BaseResult<?> netData);
}
