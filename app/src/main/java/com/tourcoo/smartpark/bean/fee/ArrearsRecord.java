package com.tourcoo.smartpark.bean.fee;

/**
 * @author :JenkinsZhou
 * @description : 欠费记录
 * @company :途酷科技
 * @date 2020年12月10日11:46
 * @Email: 971613168@qq.com
 */
public class ArrearsRecord {


    /**
     * number : anim ut
     * parking : nisi elit commodo
     * carNumber : non consequat velit consectetur pariatur
     * duration : ex minim enim est
     * type : 41822620
     * parkingNumber : aute dolor cupidatat esse tempor
     * id : -23283279
     * fee : 4.241495018589005E7
     * leaveAt : laborum dolore esse consectetur id
     * createdAt : qui proident est Excepteur mollit
     */

    private String number;
    private String parking;
    private String carNumber;
    private String duration;
    private int type;
    private String parkingNumber;
    private long id;
    private double fee;
    private String leaveAt;
    private String createdAt;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getLeaveAt() {
        return leaveAt;
    }

    public void setLeaveAt(String leaveAt) {
        this.leaveAt = leaveAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
