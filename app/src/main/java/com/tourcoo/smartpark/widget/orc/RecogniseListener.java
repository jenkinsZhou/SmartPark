package com.tourcoo.smartpark.widget.orc;

import com.baidu.vis.ocrplatenumber.Response;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月18日16:27
 * @Email: 971613168@qq.com
 */
public interface RecogniseListener {

    void recogniseSuccess(Response result);

    void recogniseFailed();
}
