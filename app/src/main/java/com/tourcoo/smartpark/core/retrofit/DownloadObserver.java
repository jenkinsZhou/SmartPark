package com.tourcoo.smartpark.core.retrofit;

import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.core.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:25
 * @Email: 971613168@qq.com
 */
public abstract class DownloadObserver extends BaseObserver<ResponseBody> {

    /**
     * 下载暂停标识字符-以异常形式抛出
     */
    public final static String DOWNLOAD_PAUSE = "FAST_DOWNLOAD_PAUSE";
    /**
     * 提示Dialog
     */
    private Dialog mDialog;
    /**
     * 主线程Handler用于通知进度更新
     */
    private Handler mHandler;
    /**
     * 目标文件存储的文件夹路径
     */
    private String mDestFileDir;
    /**
     * 目标文件存储的文件名
     */
    private String mDestFileName;
    /**
     * 是否断点续传
     */
    private boolean mIsRangeEnable = false;

    public DownloadObserver(String destFileName) {
        this(destFileName, false);
    }

    public DownloadObserver(String destFileName, boolean isRangeEnable) {
        this(FileUtil.getCacheDir(), destFileName, isRangeEnable);
    }

    public DownloadObserver(String destFileDir, String destFileName) {
        this(destFileDir, destFileName, false);
    }

    public DownloadObserver(String destFileDir, String destFileName, boolean isRangeEnable) {
        this(destFileDir, destFileName, null, isRangeEnable);
    }

    public DownloadObserver(String destFileName, Dialog dialog) {
        this(destFileName, dialog, false);
    }

    public DownloadObserver(String destFileName, Dialog dialog, boolean isRangeEnable) {
        this(FileUtil.getCacheDir(), destFileName, dialog, isRangeEnable);
    }

    public DownloadObserver(String destFileDir, String destFileName, Dialog dialog) {
        this(destFileDir, destFileName, dialog, false);
    }

    public DownloadObserver(String destFileDir, String destFileName, Dialog dialog, boolean isRangeEnable) {
        super();
        this.mDestFileDir = TextUtils.isEmpty(destFileDir) ? FileUtil.getCacheDir() : destFileDir;
        this.mDestFileName = destFileName;
        this.mDialog = dialog;
        this.mIsRangeEnable = isRangeEnable;
        LogUtils.i("FastDownloadObserver", "mDestFileDir:" + mDestFileDir);
    }

    /**
     * 主线程
     *
     * @return
     */
    private Handler getMainLooperHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(final Throwable e) {
        getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                onFail(e);
            }
        });
    }



    @Override
    public void onNext(ResponseBody entity) {
        final File file;
        try {
            file = saveFile(entity);
            getMainLooperHandler().post(new Runnable() {
                @Override
                public void run() {
                    dismissProgressDialog();
                    onSuccess(file);
                }
            });
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void onRequestSuccess(ResponseBody entity) {

    }

    @Override
    public void onRequestError(Throwable throwable) {

    }

    protected void showProgressDialog() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    protected void dismissProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgressDialog();
    }

    private boolean mPause;

    public void pause() {
        getMainLooperHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                mPause = true;
            }
        });
    }

    /**
     * 保存文件
     *
     * @param response
     * @return 返回保存文件
     * @throws IOException 写入文件IO异常
     */
    public File saveFile(ResponseBody response) throws Exception {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        File dir = new File(mDestFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, mDestFileName);
        try {
            is = response.byteStream();
            long sum = mIsRangeEnable ? file.length() : 0;
            final long total = response.contentLength() + sum;
            final long finalSum1 = sum;
            getMainLooperHandler().post(new Runnable() {
                @Override
                public void run() {
                    onProgress(finalSum1 * 1.0f / total, finalSum1, total);
                }
            });
            fos = new FileOutputStream(file, mIsRangeEnable);
            while ((len = is.read(buf)) != -1) {
                if (mPause) {
                    fos.close();
                    throw new Exception(DOWNLOAD_PAUSE);
                } else {
                    sum += len;
                    fos.write(buf, 0, len);
                    final long finalSum = sum;
                    getMainLooperHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            onProgress(finalSum * 1.0f / total, finalSum, total);
                        }
                    });
                }
            }
            fos.flush();
            return file;
        } finally {
            try {
                response.close();
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                onError(e);
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                onError(e);
            }
        }
    }

    /**
     * 下载完成-返回文件
     *
     * @param file 文件对象
     */
    public abstract void onSuccess(File file);

    /**
     * 下载失败返回错误对象
     *
     * @param e 错误对象
     */
    public abstract void onFail(Throwable e);

    /**
     * 下载进度
     *
     * @param progress 进度如 0.01
     * @param current  当前已下载字节数
     * @param total    总文件字节数
     */
    public abstract void onProgress(float progress, long current, long total);
}
