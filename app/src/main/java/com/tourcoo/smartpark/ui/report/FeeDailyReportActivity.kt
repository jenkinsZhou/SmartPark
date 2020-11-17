package com.tourcoo.smartpark.ui.report

import android.os.Bundle
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView

/**
 *@description : 收费日报
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月16日15:42
 * @Email: 971613168@qq.com
 */
class FeeDailyReportActivity : BaseTitleActivity() {
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_daily
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("收费日报")
    }
}