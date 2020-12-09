package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import com.baidu.liantian.b.s
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.fee.PayResult
import com.tourcoo.smartpark.bean.settle.SettleDetail
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.utils.StackUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.ExitPayFeeDetailActivity
import com.tourcoo.smartpark.util.StringUtil
import kotlinx.android.synthetic.main.activity_pay_result.*

/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月05日11:42
 * @Email: 971613168@qq.com
 */
class PayResultActivity : BaseTitleActivity() {
    private var paySuccess: Boolean? = null
    private var payResult: PayResult? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_pay_result
    }

    override fun initView(savedInstanceState: Bundle?) {
        paySuccess = intent?.getBooleanExtra(ExitPayFeeDetailActivity.EXTRA_PAY_STATUS, false)
        payResult = intent?.getSerializableExtra(ExitPayFeeDetailActivity.EXTRA_PAY_RESULT) as PayResult?
        if (payResult == null || paySuccess == null) {
            ToastUtil.showWarning("未获取到支付结果")
            finish()
        }
        showPayResult()
        tvConfirm.setOnClickListener {
            if (paySuccess!!) {
                ToastUtil.showWarning("打印凭条")
            } else {
                payRetry()
            }
        }
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("缴费成功")
    }


    private fun showPayResult() {
        if (paySuccess!!) {
            ivPayStatus.setImageResource(R.mipmap.ic_pay_success)
            tvPayStatus.text = "本次停车费已支付成功"
            tvPayResult.text = StringUtil.getNotNullValueLine("实收费用" + payResult?.trueFee + "元")
            tvConfirm.text = "打印凭条"
        } else {
            ivPayStatus.setImageResource(R.mipmap.ic_pay_failed)
            tvPayStatus.text = "本次停车费支付失败"
            tvPayResult.text = "请返回重新支付"
            tvConfirm.text = "重新缴费"
        }
    }

    /**
     * 重新支付
     */
    private fun payRetry() {
        finish()
    }

    private fun backHome() {
        StackUtil.getInstance().getActivity(ExitPayFeeDetailActivity::class.java)?.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (paySuccess!!) {
            backHome()
        } else {
            payRetry()
        }
    }
}