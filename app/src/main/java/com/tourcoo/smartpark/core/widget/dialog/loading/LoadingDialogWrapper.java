package com.tourcoo.smartpark.core.widget.dialog.loading;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.tourcoo.smartpark.core.utils.FindViewUtil;
import com.tourcoo.smartpark.core.utils.StackUtil;

import java.lang.ref.WeakReference;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2019年12月26日16:50
 * @Email: 971613168@qq.com
 */
public class LoadingDialogWrapper {
    private Dialog mDialog;

    private Activity mActivity;
    private final WeakReference<Activity> mReference;

    public LoadingDialogWrapper() {
        this(StackUtil.getInstance().getCurrent());
    }

    public LoadingDialogWrapper(Activity activity) {
        this(activity, new ProgressDialog.Builder(activity)
                .create());
    }

    public LoadingDialogWrapper(Activity activity, Dialog dialog) {
        this.mReference = new WeakReference<>(activity);
        this.mDialog = dialog;
    }

    /**
     * 设置是否可点击返回键关闭dialog
     *
     * @param enable
     * @return
     */
    public LoadingDialogWrapper setCancelable(boolean enable) {
        if (mDialog != null) {
            mDialog.setCancelable(enable);
        }
        return this;
    }

    /**
     * 设置是否可点击dialog以外关闭
     *
     * @param enable
     * @return
     */
    public LoadingDialogWrapper setCanceledOnTouchOutside(boolean enable) {
        if (mDialog != null) {
            mDialog.setCanceledOnTouchOutside(enable);
        }
        return this;
    }

    /**
     * @param msg
     * @return
     */
    public LoadingDialogWrapper setMessage(CharSequence msg) {
        if (mDialog == null) {
            return this;
        }
        if (mDialog instanceof ProgressDialog) {
            ((ProgressDialog) mDialog).setMessage(msg);
        } else if (mDialog instanceof IosLoadingDialog) {
           if(!TextUtils.isEmpty(msg)){
               ((IosLoadingDialog) mDialog).setLoadingText(msg);
           }
        } else {
            if (mDialog.getWindow() != null) {
                TextView textView = FindViewUtil.getTargetView(mDialog.getWindow().getDecorView(), TextView.class);
                if (textView != null) {
                    textView.setText(msg);
                } else {
                    Log.e("LoadingDialogWrapper", "textView为null");
                }
            }

        }
        return this;
    }

    /**
     * @param msg
     * @return
     */
    public LoadingDialogWrapper setMessage(int msg) {
        mActivity = mReference.get();
        if (mActivity != null) {
            return setMessage(mActivity.getText(msg));
        }
        return this;
    }

    /**
     * @param enable 设置全透明
     * @return
     */
    public LoadingDialogWrapper setFullTrans(boolean enable) {
        if (mDialog != null) {
            WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
            // 黑暗度
            lp.dimAmount = enable ? 0f : 0.5f;
            mDialog.getWindow().setAttributes(lp);
        }
        return this;
    }

    public Dialog getDialog() {
        return mDialog;
    }

    public void show() {
        mActivity = mReference.get();
        if (mActivity != null && mDialog != null && !mActivity.isFinishing()) {
            mDialog.show();
        }
    }

    public void dismiss() {
        mActivity = mReference.get();
        if (mActivity != null && mDialog != null && !mActivity.isFinishing()) {
            mDialog.dismiss();
        }
    }
}
