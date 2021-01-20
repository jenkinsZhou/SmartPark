package com.tourcoo.smartpark.widget.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月20日14:27
 * @Email: 971613168@qq.com
 */
public class PlantLayout extends LinearLayout {
    private Context context;
    private TextView[] TextViews;
    private OnFrameTouchListener mTouchListener = new OnFrameTouchListener();
    private static int ITEM_VIEW_COUNT = 8;
    private OnSelect onSelect;
    private static final int[] VIEW_IDS = new int[]{
            R.id.tv_pass1, R.id.tv_pass2, R.id.tv_pass3,
            R.id.tv_pass4, R.id.tv_pass5, R.id.tv_pass6,
            R.id.tv_pass7, R.id.tv_pass8
    };

    public TextView[] getTextViews() {
        return TextViews;
    }

    public String getText() {
        String license = "";
        for (int i = 0; i < TextViews.length; i++) {
            license += TextViews[i].getText().toString().trim();
        }
        return license;
    }


    public PlantLayout(Context context) {
        this(context, null);
    }

    public PlantLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public PlantLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

        TextViews = new TextView[ITEM_VIEW_COUNT];
        int textsLength = ITEM_VIEW_COUNT;
        for (int i = 0; i < textsLength; i++) {
            //textview放进数组中，方便修改操作
            TextViews[i] = addTextView(context, VIEW_IDS[i]);
            TextViews[i].setOnTouchListener(mTouchListener);
            if (i == textsLength - 1) {
                TextViews[i].setBackground(CommonUtil.getDrawable(R.drawable.ic_bg_car_green));
            }
            addView(TextViews[i]);
        }
        //第一个输入框默认设置点中效果
        TextViews[0].requestFocus();
        setTextViewsBackground(0);
    }

    private TextView addTextView(Context context, int id) {
        TextView tv = new TextView(context);
        tv.setId(id);
        tv.setGravity(Gravity.CENTER);
        tv.setMinEms(1);
        LinearLayout.LayoutParams params = (LayoutParams) getLayoutParams();
        int marginLeft = SizeUtil.dp2px(7);
        params.setMargins(marginLeft, 0, 0, 0);
        int width = SizeUtil.getScreenWidth() - SizeUtil.dp2px(30) - (marginLeft * ITEM_VIEW_COUNT);
        int tvSize = (width / ITEM_VIEW_COUNT);
        params.width = tvSize;
        params.height = tvSize;
        tv.setLayoutParams(params);
        tv.setFocusableInTouchMode(true);
        tv.setFocusable(true);

        return tv;
    }

    public void setTextSize(float size) {
        for (TextView tv : TextViews) {
            tv.setTextSize(size);
        }
    }

    public void setTextColor(int color) {
        for (TextView tv : TextViews) {
            tv.setTextColor(color);
        }
    }


    /**
     * 显示输入框的TouchListener
     */
    private class OnFrameTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setFocusable(true);
                tv.requestFocus();
             /*   String tvString = (String) tv.getText();
                if (TextUtils.isEmpty(tvString)) {
                    ToastUtil.showFailed("被拦截");
                    return false;
                }*/
                int viewId = tv.getId();
                for (int i = 0; i < ITEM_VIEW_COUNT; i++) {
                    if (viewId == VIEW_IDS[i]) {
                        TextViews[i].requestFocus();
                        if (onSelect != null) {
//                            ToastUtil.showNormal("点击了位置：" + i);
                            onSelect.select(i);
                        }
                    }
                }
            }
            return false;
        }
    }

    public void clearFocus() {
        for (int position = 0; position < ITEM_VIEW_COUNT; position++) {
            if (position == 0) {
                TextViews[0].setBackgroundResource(R.drawable.license_plate_first_view_all_gray);
                TextViews[0].clearFocus();
            }
            if (position < ITEM_VIEW_COUNT - 2 && position >= 1) {
                TextViews[position].setBackgroundResource(R.drawable.license_plate_first_view_all_gray);
                TextViews[position].clearFocus();
            }
            if (position == ITEM_VIEW_COUNT - 2) {
                TextViews[ITEM_VIEW_COUNT - 2].setBackgroundResource(R.drawable.license_plate_first_view_all_gray);
                TextViews[ITEM_VIEW_COUNT - 2].clearFocus();
            }
            if (position == ITEM_VIEW_COUNT - 1) {
                TextViews[ITEM_VIEW_COUNT - 1].setBackgroundResource(R.drawable.license_plate_first_view_all_gray);
                TextViews[ITEM_VIEW_COUNT - 1].clearFocus();
                setGreenCarBackground(position);
            }

        }

    }

    public void setTextViewsBackground(int position) {
        for (int i = 0; i < TextViews.length; i++) {
            if (position == i) {
                TextViews[i].setBackgroundResource(R.drawable.license_plate_first_view_blue);
            } else {
                TextViews[i].setBackgroundResource(R.drawable.license_plate_first_view_all_gray);
                setGreenCarBackground(i);
            }
        }
    }

    public void setOnSelect(OnSelect onSelect) {
        this.onSelect = onSelect;
    }

    interface OnSelect {
        void select(int select);
    }

    public void setText(String value) {
        if (TextUtils.isEmpty(value)) {
            clearInput();
            return;
        }
        if (value.length() > ITEM_VIEW_COUNT) {
            value = value.substring(0, ITEM_VIEW_COUNT);
        }
        for (int i = 0; i < value.length(); i++) {
            TextViews[i].setText(value.substring(i, i + 1));
            setGreenCarBackground(i);
        }
    }

    public void clearInput() {
        for (int i = 0; i < ITEM_VIEW_COUNT; i++) {
            if (i < TextViews.length) {
                TextViews[i].setText("");
                setGreenCarBackground(i);
            }
        }
    }

    private void setGreenCarBackground(int index) {
        if (index == TextViews.length - 1) {
            if( TextUtils.isEmpty( TextViews[index].getText().toString())){
                TextViews[index].setBackgroundResource(R.drawable.ic_bg_car_green);
            }else {
                TextViews[index].setBackgroundResource(R.drawable.license_plate_first_view_all_gray);
            }

        }
    }
}
