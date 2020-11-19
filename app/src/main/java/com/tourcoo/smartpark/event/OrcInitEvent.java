package com.tourcoo.smartpark.event;

/**
 * @author :JenkinsZhou
 * @description : 百度车牌识别实例化事件
 * @company :途酷科技
 * @date 2020年11月19日11:25
 * @Email: 971613168@qq.com
 */
public class OrcInitEvent {
    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public OrcInitEvent(int status) {
        this.status = status;
    }

    public OrcInitEvent() {
    }
}
