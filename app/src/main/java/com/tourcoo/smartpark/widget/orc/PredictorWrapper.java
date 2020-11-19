package com.tourcoo.smartpark.widget.orc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.baidu.vis.ocrplatenumber.Predictor;
import com.baidu.vis.ocrplatenumber.Response;
import com.baidu.vis.ocrplatenumber.SDKExceptions;
import com.baidu.vis.unified.license.AndroidLicenser;
import com.baidu.vis.unified.license.BDLicenseLocalInfo;
import com.tourcoo.smartpark.threadpool.ThreadPoolManager;

import java.util.concurrent.locks.ReentrantLock;

public class PredictorWrapper {
    private static final String TAG = "PredictorWrapper";
    private static ReentrantLock lock = new ReentrantLock();
    private static OrcPlantInitListener listener;
    private static RecogniseListener recogniseListener;
    private static final String FAILED_STR = " fail 预测失败 response == null || responses.length < 1";
    public static final int PLANT_TYPE_BLUE = 0;
    public static final int PLANT_TYPE_GREEN = 2;
    public static final int PLANT_TYPE_YELLOW = 1;
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private static boolean initSuccess = false;

    public static boolean initLicense(Context context) {
        // 获取鉴权相关本地设备及应用相关信息
        BDLicenseLocalInfo bdLicenseLocalInfo = AndroidLicenser.getInstance().authGetLocalInfo(context, Predictor.getAlgorithmId());
        Log.d(TAG, "BDLicenseLocalInfo :" + bdLicenseLocalInfo.toString());

        // 使用申请的license-key 及 收钱文件进行本地授权
      /*  AndroidLicenser.ErrorCode ret = AndroidLicenser.getInstance().authFromFile(activity, "ocrplatenumberdemo_test_license",
                "idl-license.ocrplatenumberdemo_test_license", true, Predictor.getAlgorithmId());*/
        AndroidLicenser.ErrorCode ret = AndroidLicenser.getInstance().authFromFile(context, "yixing_platenumber_7_B2ECF",
                "yixing_platenumber_7_B2ECF", true, Predictor.getAlgorithmId());
        if (ret != AndroidLicenser.ErrorCode.SUCCESS) {
            Log.e(TAG, "ErrorMsg :" + AndroidLicenser.getInstance().getErrorMsg(Predictor.getAlgorithmId()));
//            setTextViewOnUiThread(activity, textView, "鉴权失败");
            return false;
        }
//        setTextViewOnUiThread(activity, textView, "鉴权成功");
        return true;
    }

    public static boolean initModel(Context context) {
        try {
            // 进行模型初始化
            int ret = Predictor.getInstance().initModelFromAssets(context, "ocrplatenumber_models", 2);
            if (ret != 0) {
                Log.d(TAG, "initModel error : " + ret);
                mHandler.post(() -> {
                    initSuccess = false;
                    if (listener != null) {
                        listener.initFailed(new Throwable("initModel error : " + ret));
                    }
                });

//                setTextViewOnUiThread(activity, textView, "模型初始化失败");
                return false;
            } else {
                initSuccess = true;
                mHandler.post(() -> {
                    if (listener != null) {
                        listener.initSuccess();
                    }
                });
//                setTextViewOnUiThread(activity, textView, "模型初始化成功");
                return true;
            }

        } catch (Exception e) {
//            setTextViewOnUiThread(activity, textView, "模型初始化失败");
            LogUtils.tag(TAG).e(e.toString());
            initSuccess = false;
            mHandler.post(() -> {
                if (listener != null) {
                    listener.initFailed(e);
                }
            });
            e.printStackTrace();
            return false;
        }
    }

    public static Response testOneImage(Bitmap bitmap) {
        try {
            Response[] responses = Predictor.getInstance().predict(bitmap);
            if (responses == null || responses.length < 1) {
                return null;
            } else {
                //result = " success " + responses[0].probability + " " + responses[0].plate_number;
            /*    for (Response response : responses) {
                    result = result + response.x1 + " " + response.y1 + " " +
                            response.x2 + " " + response.y2 + " " +
                            response.x3 + " " + response.y3 + " " +
                            response.x4 + " " + response.y4 + " " +
                            response.x1 + " " + response.y1 + " " +
                            response.plate_number + " 0 NO\n";
                }*/
                return responses[0];
            }

        } catch (SDKExceptions.IlleagleLicense | SDKExceptions.NotInit e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
            return null;
        }
    }

    public static void asyncTestOneImage(final Activity activity, final Bitmap bitmap) {
        if (activity == null || bitmap == null) {
            LogUtils.tag(TAG).d("asyncTestOneImage拦截");
            return;
        }
//        setImageViewOnUiThread(activity, bitmap, imageView);
        ThreadPoolManager.getThreadPoolProxy().execute(() -> {
            // 可以做多性能测试
            int count = 1;
            while (count != 0) {
                Response result = null;
                if (lock.tryLock()) {
                    long startTime = System.currentTimeMillis();
                    result = testOneImage(bitmap);
                    Log.w(TAG, "testOneImage: " + (System.currentTimeMillis() - startTime) + "ms");
                    lock.unlock();
                }
                if (recogniseListener != null) {
                    Response finalResult = result;
                    activity.runOnUiThread(() -> {
                        if (finalResult == null) {
                            recogniseListener.recogniseFailed();
                        } else {
                            recogniseListener.recogniseSuccess(finalResult);
                        }
                    });
                }
                count--;
            }
        });
    }

    // will block UI thread if called from UI thread
    public static Response syncTestOneImage(Activity activity, Bitmap bitmap) {
        if (activity == null || bitmap == null) {
            Log.e(TAG, "activity == null || bitmap == null || imageView == null || textView == null");
            return null;
        }
        Response result = testOneImage(bitmap);
//        setImageViewOnUiThread(activity, bitmap, imageView);
        if (recogniseListener != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result == null) {
                        recogniseListener.recogniseFailed();
                    } else {
                        recogniseListener.recogniseSuccess(result);
                    }
                }
            });
        }
        return result;
    }


    private static void setImageViewOnUiThread(final Activity activity, final Bitmap bitmap,
                                               final ImageView imageView) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }


    public static void setListener(OrcPlantInitListener listener) {
        PredictorWrapper.listener = listener;
    }


    public static void setRecogniseListener(RecogniseListener recogniseListener) {
        PredictorWrapper.recogniseListener = recogniseListener;
    }

    public static boolean isInitSuccess() {
        return initSuccess;
    }

    public static void setInitSuccess(boolean initSuccess) {
        PredictorWrapper.initSuccess = initSuccess;
    }
}
