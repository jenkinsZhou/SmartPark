package com.tourcoo.smartpark.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.tourcoo.smartpark.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月12日15:15
 * @Email: 971613168@qq.com
 */
public class CommonInputDialog {

    private Context mContext;
    private Dialog dialog;
    private int width = 0;
    private TextView tvPositive;
    private EditText etInput;
   /*
    private TextView tvTitle;
    private TextView tvContent;*/

    public CommonInputDialog(Context context) {
        this.mContext = context;
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
    }


    public CommonInputDialog create() {
        // 获取Dialog布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_common_input, null);
        tvPositive = view.findViewById(R.id.tvPositive);
        etInput = view.findViewById(R.id.etInput);
//        limitInput();
       /* tvTitle = view.findViewById(R.id.tvTitle);
        tvContent = view.findViewById(R.id.tvContent);
        tvPositive = view.findViewById(R.id.tvPositive);*/
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
            // 宽度设置为屏幕的0.6
            p.width = (int) (width * 0.6);
            window.setAttributes(p);
        }
        return this;
    }

    public CommonInputDialog setPositiveButtonClick(CharSequence text, View.OnClickListener onClickListener) {
        if (TextUtils.isEmpty(text)) {
            text = "";
        }
        if (tvPositive != null) {
            tvPositive.setText(text);
            tvPositive.setOnClickListener(onClickListener);
        }
        return this;
    }

    private void limitInput() {
        InputFilter typeFilter = (source, start, end, dest, dstart, dend) -> {
            Pattern p = Pattern.compile("[a-zA-Z|\u4e00-\u9fa5]+");
            Matcher m = p.matcher(source.toString());
            if (!m.matches()) return "";
            return null;
        };
        etInput.setFilters(new InputFilter[]{typeFilter});
    }


  /*  public CommonInputDialog setNegativeButtonClick(CharSequence text, View.OnClickListener onClickListener) {
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


}
