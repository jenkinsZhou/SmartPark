package com.tourcoo.smartpark.bean.fee;

/**
 * @author :JenkinsZhou
 * @description : 收费记录
 * @company :途酷科技
 * @date 2020年12月09日11:19
 * @Email: 971613168@qq.com
 */
public class DailyFeeRecord {


    /**
     * number : A020998
     * parking : 测试停车场1
     * carNumber : 渝S·833P9
     * payType : alipay_scan
     * duration : 0时29分4秒
     * type : 1
     * parkingNumber : A02
     */

    private String number;
    private String parking;
    private String carNumber;
    private String payType;
    private String duration;
    private int type;
    private String parkingNumber;
    private double fee;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getParkingNumber() {
        return parkingNumber;
    }

    public void setParkingNumber(String parkingNumber) {
        this.parkingNumber = parkingNumber;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }
}
