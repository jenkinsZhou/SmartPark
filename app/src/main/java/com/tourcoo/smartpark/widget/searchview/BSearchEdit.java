package com.tourcoo.smartpark.widget.searchview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.tourcoo.smartpark.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月30日16:53
 * @Email: 971613168@qq.com
 */
public class BSearchEdit extends View {

    private Activity activity;
    private SearchPopupWindow searchPopupWindow;
    private View editText;
    private int widthPopup;
    private int textWidth = ViewGroup.LayoutParams.MATCH_PARENT, textHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    private int textSize = 14;
    private int textColor;
    private int line_color;
    private int line_height = 1;
    private int line_width = ViewGroup.LayoutParams.MATCH_PARENT;
    private boolean isLine = true;
    private int popup_bg;
    private BSearchEdit.TextClickListener textClickListener;
    private boolean isTimely = true;

    public BSearchEdit(Context context, @Nullable AttributeSet attrs, Activity activity) {
        super(context, attrs);
        this.activity = activity;
    }

    public BSearchEdit(Activity activity, View editText, int widthPopup) {
        super(activity);
        this.activity = activity;
        this.editText = editText;
        this.widthPopup = widthPopup;
        textColor = activity.getResources().getColor(R.color.grayA2A2A2);
        line_color = activity.getResources().getColor(R.color.grayE0E0E0);
        popup_bg = R.drawable.bs_popup_bg;
    }

    @SuppressLint("CheckResult")
    private void init() {
        searchPopupWindow = new SearchPopupWindow(activity, widthPopup);
        searchPopupWindow.setTextSize(textSize);
        searchPopupWindow.setTextColor(textColor);
        searchPopupWindow.setTextHeight(textHeight);
        searchPopupWindow.setTextWidth(textWidth);
        searchPopupWindow.setLine_bg(line_color);
        searchPopupWindow.setLine_height(line_height);
        searchPopupWindow.setLine_width(line_width);
        searchPopupWindow.setPopup_bg(popup_bg);
        searchPopupWindow.setIsLine(isLine);
        searchPopupWindow.build();
        searchPopupWindow.setTextClickListener(new SearchPopupWindow.TextClickListener() {
            @Override
            public void onTextClick(int position, String text) {
                if (textClickListener != null) {
                    textClickListener.onTextClick(position, text);
                    searchPopupWindow.dismiss();
                }
            }
        });
        if (isTimely) {
            if (editText instanceof TextView) {
                RxTextView.textChanges((TextView) editText)
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Function<CharSequence, String>() {
                            @Override
                            public String apply(CharSequence charSequence) {
                                return charSequence.toString();
                            }
                        })
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                if (s.length() > 0) {
                                    searchPopupWindow.showAsDropDown(editText);
                                }
                            }
                        });
            }

        }
    }

    //参数设置完毕，一定要build一下
    public BSearchEdit build() {
        init();
        return this;
    }

    public BSearchEdit setTextWidth(int textWidth) {
        this.textWidth = textWidth;
        return this;
    }

    public BSearchEdit setTextHeight(int textHeight) {
        this.textHeight = textHeight;
        return this;
    }

    public BSearchEdit setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public BSearchEdit setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public BSearchEdit setLine_color(int line_color) {
        this.line_color = line_color;
        return this;
    }

    public BSearchEdit setLine_height(int line_height) {
        this.line_height = line_height;
        return this;
    }

    public BSearchEdit setLine_width(int line_width) {
        this.line_width = line_width;
        return this;
    }

    public BSearchEdit setPopup_bg(int popup_bg) {
        this.popup_bg = popup_bg;
        return this;
    }

    public BSearchEdit setIsLine(boolean isLine) {
        this.isLine = isLine;
        return this;
    }

    public void setSearchList(ArrayList<String> list) {
        searchPopupWindow.setList(list);
    }

    public void clear() {
        searchPopupWindow.setList(new ArrayList<>());
    }

    public BSearchEdit setTimely(boolean timely) {
        isTimely = timely;
        return this;
    }

    public void showPopup() {
        if (!isTimely) {
            searchPopupWindow.showAsDropDown(editText);
        }
    }

    public void dismiss() {
        searchPopupWindow.dismiss();
    }

    //点击监听器
    public interface TextClickListener {
        void onTextClick(int position, String text);
    }

    public void setTextClickListener(BSearchEdit.TextClickListener textClickListener) {
        this.textClickListener = textClickListener;
    }
}
