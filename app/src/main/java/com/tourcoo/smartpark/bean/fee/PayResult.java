package com.tourcoo.smartpark.bean.fee;

import java.io.Serializable;

/**
 * @author :JenkinsZhou
 * @description : 支付结果
 * @company :途酷科技
 * @date 2020年12月09日15:00
 * @Email: 971613168@qq.com
 */
public class PayResult implements Serializable {


    private double trueFee;


    public double getTrueFee() {
        return trueFee;
    }

    public void setTrueFee(double trueFee) {
        this.trueFee = trueFee;
    }
}
