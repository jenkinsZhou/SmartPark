package com.tourcoo.smartpark.bean.account;

/**
 * @author :JenkinsZhou
 * @description : 用戶实体
 * @company :途酷科技
 * @date 2020年11月24日16:05
 * @Email: 971613168@qq.com
 */
public class UserInfo {

    /**
     * name : 收费员1
     * number : 800001
     * avatar : http://192.168.0.201:8000/uploads/images/HrRDdUpkt5XrYbgtQUATLD5anNHuegHUXwzA1h8d.png
     * date : 2020年11月24日
     * parking : 测试停车场
     * carNum : 0
     * actualIncome : 1111
     * theoreticalIncome : 0
     */

    private String name;
    private String number;
    private String avatar;
    private String date;
    private String parking;
    private int carNum;
    private int actualIncome;
    private int theoreticalIncome;
    private boolean needResetPass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public int getCarNum() {
        return carNum;
    }

    public void setCarNum(int carNum) {
        this.carNum = carNum;
    }

    public int getActualIncome() {
        return actualIncome;
    }

    public void setActualIncome(int actualIncome) {
        this.actualIncome = actualIncome;
    }

    public int getTheoreticalIncome() {
        return theoreticalIncome;
    }

    public void setTheoreticalIncome(int theoreticalIncome) {
        this.theoreticalIncome = theoreticalIncome;
    }

    public boolean isNeedResetPass() {
        return needResetPass;
    }

    public void setNeedResetPass(boolean needResetPass) {
        this.needResetPass = needResetPass;
    }
}
