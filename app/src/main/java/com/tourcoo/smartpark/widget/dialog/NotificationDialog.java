package com.tourcoo.smartpark.widget.dialog;

import android.app.Dialog;
import android.content.Context;
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

/**
 * @author :JenkinsZhou
 * @description : 消息弹窗
 * @company :途酷科技
 * @date 2020年12月31日17:26
 * @Email: 971613168@qq.com
 */
public class NotificationDialog {
    private Context context;
    private Dialog dialog;
    private int width = 0;
    private RelativeLayout container;
    private Display display;
    private TextView tvMessage;

    public NotificationDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public NotificationDialog init() {
        // 获取Dialog布局
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_notification, null);

        // 获取自定义Dialog布局中的控件
        container = view.findViewById(R.id.container);

        tvMessage = container.findViewById(R.id.tvMessage);
        // 定义Dialog布局和参数
        dialog = new Dialog(context, R.style.AlertDialogStyle);
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
                        (int) (display.getWidth() * 0.85),
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        return this;
    }

    public NotificationDialog setTitle(String title) {

        return this;
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

}
