package com.tourcoo.smartpark.core.retrofit;

/**
 * @author :JenkinsZhou
 * @description :上传文件监听
 * @company :途酷科技
 * @date 2020年10月29日11:33
 * @Email: 971613168@qq.com
 */
public interface UploadRequestListener {

    /**
     * 上传进度回调
     *
     * @param progress 进度
     * @param current  已上传字节数
     * @param total    总上传字节数
     */
    void onProgress(float progress, long current, long total);

    /**
     * 上传失败回调
     *
     * @param e 错误
     */
    void onFail(Throwable e);
}
