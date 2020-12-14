package com.tourcoo.smartpark.print;

/**
 * @author :JenkinsZhou
 * @description : 设备连接监听
 * @company :途酷科技
 * @date 2020年12月14日14:14
 * @Email: 971613168@qq.com
 */
public interface DeviceConnectListener {

    void deviceConnectSuccess();

    /**
     * 断开连接
     */
    void deviceDisConnected();

    void deviceConnecting();

    /**
     * 设备未连接
     */
    void deviceNoConnect();
}
