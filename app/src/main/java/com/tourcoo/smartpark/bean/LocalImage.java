package com.tourcoo.smartpark.bean;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月19日10:39
 * @Email: 971613168@qq.com
 */
public class LocalImage {
    private String imagePath;
    //传给后台的图片url
    private String serviceImageUrl ;
    /**
     * 是否是拍照识别的图片
     */
    private boolean recognize = false;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isRecognize() {
        return recognize;
    }

    public void setRecognize(boolean recognize) {
        this.recognize = recognize;
    }

    public LocalImage(String imagePath, boolean recognize) {
        this.imagePath = imagePath;
        this.recognize = recognize;
    }

    public LocalImage() {
    }

    public String getServiceImageUrl() {
        return serviceImageUrl;
    }

    public void setServiceImageUrl(String serviceImageUrl) {
        this.serviceImageUrl = serviceImageUrl;
    }
}
