package com.tourcoo.smartpark.core.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;


import com.tourcoo.smartpark.R;

import java.lang.ref.SoftReference;

/**
 * @author :JenkinsZhou
 * @description :吐司工具类
 * @company :途酷科技
 * @date 2020年10月28日14:54
 * @Email: 971613168@qq.com
 */
public final class ToastUtil {
    @SuppressLint("StaticFieldLeak")
    private static Application mApp;
    private static int toastBackColor;
    private static SoftReference<Toast> mToast;

    /**
     * 初始化吐司工具类
     *
     * @param app 应用
     */
    public static void init(@NonNull final Application app) {
        mApp = app;
        toastBackColor = mApp.getResources().getColor(R.color.color_000000);
    }

    public static Application getApp() {
        return mApp;
    }

    public static void setToastBackColor(@ColorInt int color) {
        toastBackColor = color;
    }

    /**
     * 私有构造
     */
    private ToastUtil() {
        //避免初始化
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 检查上下文不能为空，必须先进性初始化操作
     */
    private static void checkContext() {
        if (mApp == null) {
            throw new NullPointerException("ToastUtils context is not null，please first init");
        }
    }


    /**
     * 吐司工具类    避免点击多次导致吐司多次，最后导致Toast就长时间关闭不掉了
     * 注意：这里如果传入context会报内存泄漏；传递activity..getApplicationContext()
     *
     * @param content       吐司内容
     */
    private static Toast toast;

    @SuppressLint("ShowToast")
    public static void showToast(String content) {
        if (!isMainThread()) {
            return;
        }
        checkContext();
        if (!checkNull(mToast)) {
            mToast.get().cancel();
        }
        Toast toast = Toast.makeText(mApp, "", Toast.LENGTH_SHORT);
        toast.setText(content);
        toast.show();
        mToast = new SoftReference<>(toast);
    }


    /**
     * 某些系统可能屏蔽通知
     * 1:检查 SystemUtils.isEnableNotification(BaseApplication.getApplication());
     * 2:替代方案 SnackBarUtils.showSnack(topActivity, noticeStr);
     * 圆角
     * 屏幕中间
     *
     * @param notice 内容
     */
    public static void showRoundRectToast(CharSequence notice) {
        if (!isMainThread()) {
            return;
        }
        checkContext();
        if (TextUtils.isEmpty(notice)) {
            return;
        }
        new Builder(mApp)
                .setDuration(Toast.LENGTH_SHORT)
                .setFill(false)
                .setGravity(Gravity.CENTER)
                .setOffset(0)
                .setTitle(notice)
                .setTextColor(Color.WHITE)
                .setBackgroundColor(toastBackColor)
                .setRadius(SizeUtil.dp2px(10f))
                .setElevation(SizeUtil.dp2px(0))
                .build()
                .show();
    }


    public static void showRoundRectToast(CharSequence notice, CharSequence desc) {
        if (!isMainThread()) {
            return;
        }
        checkContext();
        if (TextUtils.isEmpty(notice)) {
            return;
        }
        new Builder(mApp)
                .setDuration(Toast.LENGTH_SHORT)
                .setFill(false)
                .setGravity(Gravity.CENTER)
                .setOffset(0)
                .setDesc(desc)
                .setTitle(notice)
                .setTextColor(Color.WHITE)
                .setBackgroundColor(toastBackColor)
                .setRadius(SizeUtil.dp2px(10))
                .setElevation(SizeUtil.dp2px(0))
                .build()
                .show();
    }


    public static void showRoundRectToast(@LayoutRes int layout) {
        if (!isMainThread()) {
            return;
        }
        checkContext();
        if (layout == 0) {
            return;
        }
        new Builder(mApp)
                .setDuration(Toast.LENGTH_SHORT)
                .setFill(false)
                .setGravity(Gravity.CENTER)
                .setOffset(0)
                .setLayout(layout)
                .build()
                .show();
    }


    public static final class Builder {

        private Context context;
        private CharSequence title;
        private CharSequence desc;
        private int gravity = Gravity.TOP;
        private boolean isFill;
        private int yOffset;
        private int duration = Toast.LENGTH_SHORT;
        private int textColor = Color.WHITE;
        private int backgroundColor = Color.BLACK;
        private float radius;
        private int elevation;
        private int layout;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setDesc(CharSequence desc) {
            this.desc = desc;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setFill(boolean fill) {
            isFill = fill;
            return this;
        }

        public Builder setOffset(int yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setRadius(float radius) {
            this.radius = radius;
            return this;
        }

        public Builder setElevation(int elevation) {
            this.elevation = elevation;
            return this;
        }

        public Builder setLayout(@LayoutRes int layout) {
            this.layout = layout;
            return this;
        }

        public Builder setLayout(@LayoutRes int layout, CharSequence desc) {
            this.layout = layout;
            this.desc = desc;
            return this;
        }

        public Toast build() {
            if (!checkNull(mToast)) {
                mToast.get().cancel();
            }
            Toast toast = new Toast(context);
            if (isFill) {
                toast.setGravity(gravity | Gravity.FILL_HORIZONTAL, 0, yOffset);
            } else {
                toast.setGravity(gravity, 0, yOffset);
            }
            toast.setDuration(duration);
            toast.setMargin(0, 0);
            if (layout == 0) {
                CardView rootView = (CardView) LayoutInflater.from(context).inflate(R.layout.view_toast_custom, null);
                TextView textView = rootView.findViewById(R.id.toastTextView);
                TextView descTv = rootView.findViewById(R.id.desc);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //rootView.setElevation(elevation);
                    rootView.setCardElevation(elevation);
                }
                rootView.setRadius(radius);
                rootView.setCardBackgroundColor(backgroundColor);
                //rootView.setBackgroundColor(backgroundColor);
                textView.setTextColor(textColor);
                textView.setText(title);
                if (TextUtils.isEmpty(desc)) {
                    descTv.setVisibility(View.GONE);
                } else {
                    descTv.setText(desc);
                    descTv.setVisibility(View.VISIBLE);
                }
                toast.setView(rootView);
            } else {
                View view = LayoutInflater.from(context).inflate(layout, null);
                TextView tvToastContent = view.findViewById(R.id.tvToastContent);
                if (tvToastContent != null && desc != null) {
                    tvToastContent.setText(desc);
                }
                toast.setView(view);
            }
            mToast = new SoftReference<>(toast);
            return toast;
        }
    }


    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    private static boolean checkNull(SoftReference softReference) {
        if (softReference == null || softReference.get() == null) {
            return true;
        }
        return false;
    }


    /**
     * 成功吐司
     * @param content
     */
    public static void showSuccess(CharSequence content) {
        showCustomLayout(R.layout.toast_view_layout_success,content);
    }
    public static void showSuccess(int stringResource) {
        showSuccess(mApp.getText(stringResource));
    }
    /**
     * 失败吐司
     * @param content
     */
    public static void showFailed(CharSequence content) {
        showCustomLayout(R.layout.toast_view_layout_failed,content);
    }

    /**
     * 普通吐司
     * @param content
     */
    public static void showNormal(CharSequence content) {
        showCustomLayout(R.layout.toast_view_layout_normal,content);
    }
    public static void showNormal(int stringResource) {
        showNormal(mApp.getText(stringResource));
    }

    private static void showCustomLayout(int layout ,CharSequence content){
        if (!isMainThread()) {
            return;
        }
        checkContext();
        if (!isMainThread()) {
            return;
        }
        checkContext();
        new Builder(mApp)
                .setDuration(Toast.LENGTH_SHORT)
                .setFill(false)
                .setGravity(Gravity.CENTER)
                .setOffset(0)
//                .setLayout(R.layout.toast_view_layout_success, content)
                .setLayout(layout, content)
                .build()
                .show();
    }

}
