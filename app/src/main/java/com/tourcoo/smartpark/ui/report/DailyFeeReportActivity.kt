package com.tourcoo.smartpark.ui.report

import android.content.Intent
import android.os.Bundle
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.report.DailyReport
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.pay.ScanCodePayActivity
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_report_fee_daily.*

/**
 *@description : 收费日报
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月09日9:30
 * @Email: 971613168@qq.com
 */
class DailyFeeReportActivity : BaseTitleActivity() {
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_daily
    }

    override fun initView(savedInstanceState: Bundle?) {
        llFeeRecord.setOnClickListener {
            doSkipReportList()
        }

        llPayPrintCode.setOnClickListener {
           ToastUtil.showWarning("打印拼条")
        }
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("收费日报")
    }

    override fun loadData() {
        super.loadData()
        requestDailyReport()
    }

    private fun requestDailyReport() {
        ApiRepository.getInstance().requestDailyReport().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<DailyReport>>() {
            override fun onRequestSuccess(entity: BaseResult<DailyReport>?) {
                handleRequestSuccess(entity)
            }
        })
    }

    private fun handleRequestSuccess(entity: BaseResult<DailyReport>?) {
        if (entity == null) {
            return
        }
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            showDailyReport(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }

    private fun showDailyReport(data: DailyReport?) {
        tvParkingName.text = data?.parking
        tvDate.text = data?.date
        tvTollManName.text = data?.name
        tvTollManNum.text = data?.number
        tvTotalCarCount.text = data?.carNum
        tvTotalShouldIncome.text = StringUtil.getNotNullValue("¥ " + data?.theoreticalIncome)
        tvTotalReallyIncome.text = StringUtil.getNotNullValue("¥ " + data?.actualIncome)
        tvOnlineIncome.text = StringUtil.getNotNullValue("¥ " + data?.onlineIncome)
        tvOffLineIncome.text = StringUtil.getNotNullValue("¥ " + data?.offlineIncome)
    }

    private fun doSkipReportList() {
        val intent = Intent()
        intent.setClass(this@DailyFeeReportActivity, FeeRecordActivity::class.java)
        startActivity(intent)
    }
}