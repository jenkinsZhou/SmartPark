package com.tourcoo.smartpark.bean.fee;

/**
 * @author :JenkinsZhou
 * @description : 欠费历史记录实体
 * @company :途酷科技
 * @date 2020年12月07日10:32
 * @Email: 971613168@qq.com
 */
public class ArrearsHistoryRecord {

    /**
     * createdAt : mollit
     * fee : -7.382316137723835E7
     * id : -51871492
     * parking : nulla Ut laborum laboris culpa
     * leaveAt : Lorem
     * duration : et ullamco irure nulla sint
     */

    private String createdAt;
    private double fee;
    private long id;
    private String parking;
    private String leaveAt;
    private String duration;
    private boolean select;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParking() {
        return parking;
    }

    public void setParking(String parking) {
        this.parking = parking;
    }

    public String getLeaveAt() {
        return leaveAt;
    }

    public void setLeaveAt(String leaveAt) {
        this.leaveAt = leaveAt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
