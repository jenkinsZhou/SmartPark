package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView

/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月05日11:42
 * @Email: 971613168@qq.com
 */
class PayResultActivity : BaseTitleActivity() {
    override fun getContentLayout(): Int {
        return R.layout.activity_pay_result
    }

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("缴费成功")
    }
}