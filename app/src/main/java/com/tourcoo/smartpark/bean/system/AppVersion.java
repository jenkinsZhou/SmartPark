package com.tourcoo.smartpark.bean.system;

/**
 * @author :JenkinsZhou
 * @description : 版本更新实体
 * @company :途酷科技
 * @date 2020年12月09日16:06
 * @Email: 971613168@qq.com
 */
public class AppVersion {


    /**
     * apkPath : magna dolore esse elit cillum
     * version : sit et culpa
     * force : true
     * description : non
     */

    private String apkPath;
    private String version;
    private int versionCode;
    private boolean force;
    private String description;

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
}
