package com.tourcoo.smartpark.bean;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年11月04日16:04
 * @Email: 971613168@qq.com
 */
public class ParkInfo {
    private String plantNum;
    private String parkingNum;
    private int carType;
    private int status;

    public String getPlantNum() {
        return plantNum;
    }

    public void setPlantNum(String plantNum) {
        this.plantNum = plantNum;
    }

    public String getParkingNum() {
        return parkingNum;
    }

    public void setParkingNum(String parkingNum) {
        this.parkingNum = parkingNum;
    }

    public int getCarType() {
        return carType;
    }

    public void setCarType(int carType) {
        this.carType = carType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
