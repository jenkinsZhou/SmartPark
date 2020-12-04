package com.tourcoo.smartpark.bean.settle;

/**
 * @author :停车费结算详情实体
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月03日15:34
 * @Email: 971613168@qq.com
 */
public class SettleDetail {


    /**
     * arrears : 1212.38
     * carNumber : 皖A·J1A85
     * number : A010086
     * id : 15
     * type : 1
     * fee : 0
     * createdAt : 2020-12-03 15:26:09
     * leaveAt : 2020-12-03 15:29:11
     * count : 1212.38
     */

    private double arrears;
    private String carNumber;
    private String number;
    private int id;
    private int type;
    private double fee;
    private String createdAt;
    private String leaveAt;
    private double count;
    private long carId;

    public double getArrears() {
        return arrears;
    }

    public void setArrears(double arrears) {
        this.arrears = arrears;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLeaveAt() {
        return leaveAt;
    }

    public void setLeaveAt(String leaveAt) {
        this.leaveAt = leaveAt;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }
}