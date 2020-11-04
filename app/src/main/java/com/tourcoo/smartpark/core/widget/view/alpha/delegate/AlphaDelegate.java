package com.tourcoo.smartpark.core.widget.view.alpha.delegate;

import android.view.View;

import com.tourcoo.smartpark.core.widget.view.alpha.AlphaViewHelper;


/**
 * Function: 控制View alpha度代理类
 * Description:
 */
public class AlphaDelegate {

    private View mView;
    private AlphaViewHelper mAlphaViewHelper;

    public AlphaDelegate(View view) {
        this.mView = view;
    }

    public AlphaViewHelper getAlphaViewHelper() {
        if (mAlphaViewHelper == null) {
            mAlphaViewHelper = new AlphaViewHelper(mView);
        }
        return mAlphaViewHelper;
    }
}
