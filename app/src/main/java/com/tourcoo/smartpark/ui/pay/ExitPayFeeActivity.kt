package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import android.view.View
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.widget.dialog.AppUpdateDialog
import com.tourcoo.smartpark.widget.dialog.CommonInputDialog
import kotlinx.android.synthetic.main.activity_exit_pay_fee.*


/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月09日9:12
 * @Email: 971613168@qq.com
 */
class ExitPayFeeActivity : BaseTitleActivity(), View.OnClickListener {

    override fun getContentLayout(): Int {
        return R.layout.activity_exit_pay_fee
    }

    override fun initView(savedInstanceState: Bundle?) {
        llPayByCash.setOnClickListener(this)
        llPayByCode.setOnClickListener(this)
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("离场收费")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llPayByCash -> {
                val dialog: AppUpdateDialog = AppUpdateDialog(mContext).create()
                dialog.setPositiveButtonClick("立即更新") {
                    ToastUtil.showSuccess("立即更新")
                    dialog.dismiss()
                }
                dialog.show()
            }
            R.id.llPayByCode -> {
                val dialog: CommonInputDialog = CommonInputDialog(mContext).create()
                dialog.setPositiveButtonClick("确认") {
                    ToastUtil.showSuccess("确认")
                    dialog.dismiss()
                }
                dialog.show()
            }

            else -> {
            }
        }
    }
}