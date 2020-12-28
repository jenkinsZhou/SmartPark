package com.tourcoo.smartpark.bean.fee;

/**
 * @author :JenkinsZhou
 * @description : 费用详情实体（收费详情和欠费详情公用）
 * @company :途酷科技
 * @date 2020年12月21日14:54
 * @Email: 971613168@qq.com
 */
public class FeeDetail {

/**
 *  {
 *         "number": "ad nostrud elit commodo",
 *         "parking": "aliquip Lorem pariatur est",
 *         "carNumber": "ex sed",
 *         "duration": "ullamco Lorem dolore mollit",
 *         "type": -67746782,
 *         "parkingNumber": "deserunt minim",
 *         "fee": -79255536.38820028,
 *         "createdAt": "anim",
 *         "leaveAt": "voluptate est"
 *     }
 */
    /**
     * number : proident ea
     * parking : ea do occaecat in enim
     * carNumber : aliqua incididunt quis ad
     * duration : nostrud
     * type : 19071022
     * parkingNumber : dolore
     * fee : -4.243938018361453E7
     * createdAt : aute nostrud ex enim
     * leaveAt : consequat nostrud enim in
     * totalFee : -7.807756837896305E7
     * arrears : 8.93052366784513E7
     */

    private String number;
    private String parking;
    private String carNumber;
    private String duration;
    private int type;
    private String parkingNumber;
    private double fee;
    private String createdAt;
    private String leaveAt;
    private double totalFee;
    private double arrears;
    private double theoreticalFee;

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

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getArrears() {
        return arrears;
    }

    public void setArrears(double arrears) {
        this.arrears = arrears;
    }

    public double getTheoreticalFee() {
        return theoreticalFee;
    }

    public void setTheoreticalFee(double theoreticalFee) {
        this.theoreticalFee = theoreticalFee;
    }
}
