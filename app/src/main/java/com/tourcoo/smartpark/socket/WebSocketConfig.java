package com.tourcoo.smartpark.socket;

import com.tourcoo.smartpark.core.control.RequestConfig;

import static com.tourcoo.smartpark.core.control.RequestConfig.SERVICE_PORT;

/**
 * @author :JenkinsZhou
 * @description : socket相关配置
 * @company :途酷科技
 * @date 2020年12月18日11:47
 * @Email: 971613168@qq.com
 */
public class WebSocketConfig {
    public static final int TIME_OUT = 10000;

    public static final String SOCKET_URL = "ws://"+ RequestConfig.SERVICE_IP+SERVICE_PORT+"/push";
}
