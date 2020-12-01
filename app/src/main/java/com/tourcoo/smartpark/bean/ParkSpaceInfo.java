package com.tourcoo.smartpark.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author :JenkinsZhou
 * @description : 停车位信息
 * @company :途酷科技
 * @date 2020年11月24日17:40
 * @Email: 971613168@qq.com
 */
public class ParkSpaceInfo implements Parcelable {

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.number);
        dest.writeInt(this.used);
        dest.writeString(this.parkingNumber);
        dest.writeString(this.carNumber);
        dest.writeInt(this.type);
        dest.writeString(this.spaceNumber);
    }

    public ParkSpaceInfo() {
    }

    protected ParkSpaceInfo(Parcel in) {
        this.id = in.readInt();
        this.number = in.readString();
        this.used = in.readInt();
        this.parkingNumber = in.readString();
        this.carNumber = in.readString();
        this.type = in.readInt();
        this.spaceNumber = in.readString();
    }

    public static final Parcelable.Creator<ParkSpaceInfo> CREATOR = new Parcelable.Creator<ParkSpaceInfo>() {
        @Override
        public ParkSpaceInfo createFromParcel(Parcel source) {
            return new ParkSpaceInfo(source);
        }

        @Override
        public ParkSpaceInfo[] newArray(int size) {
            return new ParkSpaceInfo[size];
        }
    };
}
