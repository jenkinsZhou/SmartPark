package com.tourcoo.smartpark.socket;

/**
 * @author :JenkinsZhou
 * @description : SocketData
 * @company :途酷科技
 * @date 2020年12月30日11:31
 * @Email: 971613168@qq.com
 */
public class SocketData {
    private int msgNum;
    private String carNumber;
    private String number;

    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
