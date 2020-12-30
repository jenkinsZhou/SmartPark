package com.tourcoo.smartpark.bean.message;

/**
 * @author :JenkinsZhou
 * @description : 消息实体
 * @company :途酷科技
 * @date 2020年12月30日15:14
 * @Email: 971613168@qq.com
 */
public class MessageInfo {

    /**
     * id : 51936797
     * number : aute
     * fee : -9.969237944024827E7
     * carNumber : id mollit culpa
     * leaveAt : exercitation consectetur incididunt amet
     * createdAt : dolore
     * type : -90166623
     */

    private long id;
    private String number;
    private double fee;
    private double realFee;
    private String carNumber;
    private String leaveAt;
    private String createdAt;
    private int type;
    private boolean select;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getRealFee() {
        return realFee;
    }

    public void setRealFee(double realFee) {
        this.realFee = realFee;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
