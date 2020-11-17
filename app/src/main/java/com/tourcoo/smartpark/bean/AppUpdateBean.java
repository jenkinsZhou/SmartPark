package com.tourcoo.smartpark.bean;

/**
 * @author :JenkinsZhou
 * @description : 版本更新
 * @company :途酷科技
 * @date 2020年11月13日16:23
 * @Email: 971613168@qq.com
 */
public class AppUpdateBean {

    /**
     * version : v1.0.0
     * apkPath : http://192.168.0.201:8000/uploads/20201113/app-release.apk
     * description : 更新111
     * 更新2222
     * 更新433333
     * force : true
     */

    private String version;
    private String apkPath;
    private String description;
    private boolean force;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
