package com.tourcoo.smartpark.core.control;

/**
 * @author :JenkinsZhou
 * @description :请求相关常量
 * @company :途酷科技
 * @date 2020年10月28日11:22
 * @Email: 971613168@qq.com
 */
public class RequestConfig {
    /**
     * 第一页（有的为0有的为1）
     */
    public static final int FIRST_PAGE = 1;

    public static final int RESPONSE_CODE_SUCCESS = 200;
    public static final int REQUEST_CODE_SUCCESS = 1;
    public static final int REQUEST_CODE_TOKEN_INVALID = 401;

//    public static final String SERVICE_IP = "192.168.0.238";
public static final String SERVICE_IP = "park.tklvyou.cn";
//    public static final String SERVICE_IP = "https://park.tklvyou.cn";
    public static final String SERVICE_PORT = ":8001";
//public static final String SERVICE_PORT = ":8002";
//    public static final String SOCKET_URL = "ws://192.168.0.238:8007/push";
public static final String SOCKET_URL = "wss://park.tklvyou.cn:8001/push";
    public static final String BASE_URL = "https://" + SERVICE_IP + SERVICE_PORT + "/";


//192.168.0.200:8007

}
