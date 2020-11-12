package com.tourcoo.smartpark.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.tourcoo.smartpark.R;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月12日11:41
 * @Email: 971613168@qq.com
 */
public class AppUpdateDialog {
    private Context mContext;
    private Dialog dialog;
    private int width = 0;
    private TextView tvPositive;
    private TextView tvTitle;
    private TextView tvContent;

    public AppUpdateDialog(Context context) {
        this.mContext = context;
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
    }




    public AppUpdateDialog create() {
        // 获取Dialog布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_app_update, null);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvContent = view.findViewById(R.id.tvContent);
        tvPositive = view.findViewById(R.id.tvPositive);
      /*  tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/
        // 设置Dialog最小宽度为屏幕宽度
        view.setMinimumWidth(width);
        // 定义Dialog布局和参数
        dialog = new Dialog(mContext, R.style.AlertDialogStyle);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager m = window.getWindowManager();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            //宽高可设置具体大小
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            // 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
            // 注释：dialog.onWindowAttributesChanged(lp);
            window.setAttributes(lp);
            // 获取屏幕宽、高用
            Display d = m.getDefaultDisplay();
            // 获取对话框当前的参数值
            WindowManager.LayoutParams p = window.getAttributes();
            Display display = m.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            // 宽度设置为屏幕的0.58
            p.width = (int) (width * 0.58);
            window.setAttributes(p);
        }
        return this;
    }

    public AppUpdateDialog setPositiveButtonClick(CharSequence text, View.OnClickListener onClickListener) {
        if (TextUtils.isEmpty(text)) {
            text = "";
        }
        if (tvPositive != null) {
            tvPositive.setText(text);
            tvPositive.setOnClickListener(onClickListener);
        }
        return this;
    }


  /*  public AppUpdateDialog setNegativeButtonClick(CharSequence text, View.OnClickListener onClickListener) {
        if (TextUtils.isEmpty(text)) {
            text = "";
        }
        if (tvNegative != null) {
            tvNegative.setText(text);
            tvNegative.setOnClickListener(onClickListener);
        }
        return this;
    }*/


    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public AppUpdateDialog setContent(CharSequence charSequence) {
        if (charSequence != null && tvContent != null) {
            tvContent.setText(charSequence);
        }
        return this;
    }


    public AppUpdateDialog setPositiveButton(CharSequence charSequence, View.OnClickListener onClickListener) {
        if (charSequence != null && tvPositive != null) {
            tvPositive.setText(charSequence);
        }
        if (tvPositive != null && onClickListener != null) {
            tvPositive.setOnClickListener(onClickListener);
        }
        return this;
    }

    public AppUpdateDialog setPositiveButton(View.OnClickListener onClickListener) {
        if (tvPositive != null && onClickListener != null) {
            tvPositive.setOnClickListener(onClickListener);
        }
        return this;
    }

}
