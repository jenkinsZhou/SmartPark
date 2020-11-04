package com.tourcoo.smartpark.core.widget.dialog.loading;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.tourcoo.smartpark.R;


public class IosLoadingDialog extends Dialog {
    public Context context;
    public CharSequence loadingText;
    private TextView tvLoadingText;

    public IosLoadingDialog(Context context) {
        super(context, R.style.frame_loading_dialog);
        this.context = context;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogWindowStyle);
        }
    }

    public IosLoadingDialog(Context context, String loadingText) {
        super(context, R.style.frame_loading_dialog);
        this.context = context;
        this.loadingText = loadingText;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogWindowStyle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_loading_dialog);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        if (TextUtils.isEmpty(loadingText)) {
            tvLoadingText.setVisibility(View.GONE);
        } else {
            tvLoadingText.setText(loadingText);
        }
    }


    public IosLoadingDialog setLoadingText(String loadingText) {
        if (TextUtils.isEmpty(loadingText)) {
            return this;
        }
        this.loadingText = loadingText;
        if (tvLoadingText == null) {
            return this;
        }
        tvLoadingText.setText(loadingText);
        View view = getCurrentFocus();
        if (view != null) {
            view.postInvalidate();
        }
        return this;
    }

    public IosLoadingDialog setLoadingText(CharSequence loadingText) {
        if (TextUtils.isEmpty(loadingText)) {
            return this;
        }
        this.loadingText = loadingText;
        if (tvLoadingText == null) {
            return this;
        }
        tvLoadingText.setText(loadingText);
        View view = getCurrentFocus();
        if (view != null) {
            view.postInvalidate();
        }
        return this;
    }

    @Override
    public void show() {
        if (((Activity) context).isFinishing()) {
            return;
        }
        setLoadingText(loadingText);
        super.show();
    }
}