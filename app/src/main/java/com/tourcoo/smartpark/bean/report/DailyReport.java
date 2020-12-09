package com.tourcoo.smartpark.bean.report;

/**
 * @author :JenkinsZhou
 * @description : 用户日报
 * @company :途酷科技
 * @date 2020年12月09日9:36
 * @Email: 971613168@qq.com
 */
public class DailyReport {


    /**
     * number : consectetur in enim dolore ut
     * name : eu id
     * date : ad Duis
     * parking : do
     * actualIncome : -9.379884517235051E7
     * carNum : -45722632
     * theoreticalIncome : 1.1160488135405362E7
     * onlineIncome : -3.1436571763261557E7
     * offlineIncome : 2.005158067954786E7
     */

    private String number;
    private String name;
    private String date;
    private String parking;
    private double actualIncome;
    private String carNum;
    private double theoreticalIncome;
    private double onlineIncome;
    private double offlineIncome;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getActualIncome() {
        return actualIncome;
    }

    public void setActualIncome(double actualIncome) {
        this.actualIncome = actualIncome;
    }

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
    }

    public double getTheoreticalIncome() {
        return theoreticalIncome;
    }

    public void setTheoreticalIncome(double theoreticalIncome) {
        this.theoreticalIncome = theoreticalIncome;
    }

    public double getOnlineIncome() {
        return onlineIncome;
    }

    public void setOnlineIncome(double onlineIncome) {
        this.onlineIncome = onlineIncome;
    }

    public double getOfflineIncome() {
        return offlineIncome;
    }

    public void setOfflineIncome(double offlineIncome) {
        this.offlineIncome = offlineIncome;
    }
}
