package com.tourcoo.smartpark.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.utils.StackUtil;

/**
 * @author :JenkinsZhou
 * @description : 消息弹窗
 * @company :途酷科技
 * @date 2020年12月31日17:26
 * @Email: 971613168@qq.com
 */
public class NotificationDialog {
    private Activity context;
    private Dialog dialog;
    private Display display;
    private TextView tvMessage;
    private TextView tvTitle, tvConfirm;

    public NotificationDialog() {
        context = StackUtil.getInstance().getCurrent();
        if (context == null) {
            return;
        }
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public NotificationDialog init() {
        if (context == null) {
            return this;
        }
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_notification, null);
        // 获取自定义Dialog布局中的控件
        RelativeLayout container = view.findViewById(R.id.container);
        tvMessage = container.findViewById(R.id.tvMessage);
        tvTitle = container.findViewById(R.id.tvTitle);
        tvConfirm = container.findViewById(R.id.tvConfirm);
        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogStyle);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(view);
        view.findViewById(R.id.ivClosed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // 调整dialog背景大小
        container.setLayoutParams(
                new FrameLayout.LayoutParams(
                        (int) (display.getWidth() * 0.62),
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    public NotificationDialog setTitle(CharSequence title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        return this;
    }

    public void show() {
        if (dialog == null) {
            return;
        }
       dialog.show();
    }


    public NotificationDialog setMessage(CharSequence message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
        }
        return this;
    }

    public NotificationDialog setPositiveClickListener(View.OnClickListener onClickListener) {
        if (onClickListener == null) {
            return this;
        }
        if (tvConfirm != null) {
            tvConfirm.setOnClickListener(onClickListener);
        }
        return this;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
        if (dialog != null && dismissListener != null) {
            dialog.setOnDismissListener(dismissListener);
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public Activity getActivity() {
        if (dialog != null) {
            return context;
        }
        return null;
    }
    public void releaseContext(){
        context = null;
    }
}
