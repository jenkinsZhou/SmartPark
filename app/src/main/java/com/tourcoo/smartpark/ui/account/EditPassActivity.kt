package com.tourcoo.smartpark.ui.account

import android.content.Intent
import android.os.Bundle
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.pay.ExitPayFeeDetailActivity
import kotlinx.android.synthetic.main.activity_edit_pass.*

/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月05日10:55
 * @Email: 971613168@qq.com
 */
class EditPassActivity : BaseTitleActivity() {
    override fun getContentLayout(): Int {
        return R.layout.activity_edit_pass
    }

    override fun initView(savedInstanceState: Bundle?) {
        tvConfirm.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@EditPassActivity, ExitPayFeeDetailActivity::class.java)
            startActivity(intent)
        }
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("修改密码")
    }
}