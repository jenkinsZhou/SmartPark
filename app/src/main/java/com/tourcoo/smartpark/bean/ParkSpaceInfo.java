package com.tourcoo.smartpark.bean;

/**
 * @author :JenkinsZhou
 * @description : 停车位信息
 * @company :途酷科技
 * @date 2020年11月24日17:40
 * @Email: 971613168@qq.com
 */
public class ParkSpaceInfo {

    /**
     * id : 2010
     * number : A010100
     * used : 0
     * parkingNumber : A01
     * carNumber :
     * type : 0
     * spaceNumber : 0100
     */

    private int id;
    private String number;
    private int used;
    private String parkingNumber;
    private String carNumber;
    private int type;
    private String spaceNumber;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public String getParkingNumber() {
        return parkingNumber;
    }

    public void setParkingNumber(String parkingNumber) {
        this.parkingNumber = parkingNumber;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSpaceNumber() {
        return spaceNumber;
    }

    public void setSpaceNumber(String spaceNumber) {
        this.spaceNumber = spaceNumber;
    }
}
