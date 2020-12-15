package com.tourcoo.smartpark.ui.fee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.PayResult
import com.tourcoo.smartpark.bean.settle.SettleDetail
import com.tourcoo.smartpark.constant.ParkConstant
import com.tourcoo.smartpark.core.CommonUtil
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.BaseObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.PayConstant.PAY_TYPE_CASH
import com.tourcoo.smartpark.ui.fee.PayConstant.PAY_TYPE_SCAN
import com.tourcoo.smartpark.ui.pay.PayResultActivity
import com.tourcoo.smartpark.ui.pay.ScanCodePayActivity
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.util.StringUtil.listParseIntArray
import com.tourcoo.smartpark.widget.dialog.IosAlertDialog
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_exit_pay_fee_settle_detail.*
import org.apache.commons.lang3.StringUtils
import kotlin.collections.ArrayList


/**
 *@description :离场收费详情(离场结算页面)
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月09日9:12
 * @Email: 971613168@qq.com
 */
class SettleFeeDetailActivity : BaseTitleActivity(), View.OnClickListener, OnRefreshListener {
    /**
     * 停车记录id
     */
    private var recordId = -1L
    private var parkId = -1L

    private var carId = -1L
    private var settleId = -1L
    private var needIgnore = false

    private var mArrearsIds: String? = null
    private var mPayType: Int? = null
    private val arrearsIdList: MutableList<Int> = ArrayList()
    private var payResult: PayResult? = null

    companion object {
        const val EXTRA_SETTLE_RECORD_ID = "EXTRA_SETTLE_RECORD_ID"
        const val EXTRA_PARK_ID = "EXTRA_PARK_ID"
        const val EXTRA_CAR_ID = "EXTRA_CAR_ID"
        const val EXTRA_ARREARS_IDS = "EXTRA_ARREARS_IDS"
        const val EXTRA_SCAN_RESULT = "EXTRA_SCAN_RESULT"
        const val EXTRA_PAY_RESULT = "EXTRA_PAY_RESULT"
        const val EXTRA_PAY_STATUS = "EXTRA_PAY_STATUS"
        const val REQUEST_CODE_FEE_RECORD = 1002
        const val REQUEST_CODE_PAY_BY_SCAN = 1003
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_exit_pay_fee_settle_detail
    }

    override fun initView(savedInstanceState: Bundle?) {
        recordId = intent?.getLongExtra(EXTRA_SETTLE_RECORD_ID, -1L)!!
        parkId = intent?.getLongExtra(EXTRA_PARK_ID, -1L)!!
        if (recordId < 0 || parkId < 0) {
            ToastUtil.showWarning("未获取到正确的停车数据")
            finish()
            return
        }
        llPayByCash.setOnClickListener(this)
        llPayByCode.setOnClickListener(this)
        tvFeeHistory.setOnClickListener(this)
        tvIgnoreHistoryFee.setOnClickListener(this)
        initRefreshLayout()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("离场收费")
    }

    override fun loadData() {
        super.loadData()
        requestSettleInfo(needIgnore)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llPayByCash -> {
//                showPayCashConfirm()
                payResult = PayResult()

                skipPayResult(true)
            }
            R.id.llPayByCode -> {
                skipScanCode()
            }
            R.id.tvIgnoreHistoryFee -> {
                requestSettleInfo(!needIgnore)
                needIgnore = !needIgnore
            }
            R.id.tvFeeHistory -> {
                skipArrearsRecord()
            }

            else -> {
            }
        }
    }


    private fun initRefreshLayout() {
        settleRefreshLayout.setEnableLoadMore(false)
        settleRefreshLayout.setOnRefreshListener(this)
        settleRefreshLayout.setRefreshHeader(ClassicsHeader(mContext))
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        requestSettleInfo(needIgnore)
    }

    private fun showSettleInfo(settleDetail: SettleDetail?, ignore: Boolean) {
        if (settleDetail == null) {
            return
        }
        if (ignore) {
            tvIgnoreHistoryFee.text = StringUtil.getNotNullValueLine("[已忽略]")
        } else {
            tvIgnoreHistoryFee.text = StringUtil.getNotNullValueLine("[忽略]")
        }
        tvEnterTime.text = StringUtil.getNotNullValueLine(settleDetail.createdAt)
        tvExitTime.text = StringUtil.getNotNullValueLine(settleDetail.leaveAt)
        tvFeeCurrent.text = StringUtil.getNotNullValueLine("¥ " + settleDetail.fee)
        tvFeeHistory.text = StringUtil.getNotNullValueLine("¥ " + settleDetail.arrears)
        tvFeeShould.text = StringUtil.getNotNullValueLine("¥ " + settleDetail.count)
        tvFeeReally.text = StringUtil.getNotNullValueLine("")
        showCarInfo(settleDetail)
        showTitleInfoByCondition(settleDetail)
    }


    private fun requestSettleInfo(needIgnore: Boolean) {
        ApiRepository.getInstance().requestSpaceSettleDetail(recordId, needIgnore, mArrearsIds).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<SettleDetail>>() {
            override fun onRequestSuccess(entity: BaseResult<SettleDetail>?) {
                handleRequestSuccess(entity, needIgnore)
            }

            override fun onRequestError(throwable: Throwable?) {
                super.onRequestError(throwable)
                settleRefreshLayout.finishRefresh(false)
            }

        })
    }


    private fun handleRequestSuccess(entity: BaseResult<SettleDetail>?, ignore: Boolean) {
        if (entity == null) {
            settleRefreshLayout.finishRefresh(false)
            return
        }
        settleRefreshLayout.finishRefresh(true)
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
            carId = entity.data.carId
            settleId = entity.data.id
            arrearsIdList.clear()
            if (entity.data.arrearsId != null) {
                arrearsIdList.addAll(entity.data.arrearsId)
            }
            showSettleInfo(entity.data, ignore)

        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }

    private fun showCarInfo(detail: SettleDetail) {
        tvSpaceNum.text = StringUtil.getNotNullValueLine(detail.number)
        tvPlantNum.text = StringUtil.getNotNullValueLine(detail.carNumber)
        when (detail.type) {
            ParkConstant.CAR_TYPE_NORMAL -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_blue_5087ff)
            }
            ParkConstant.CAR_TYPE_YELLOW -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_green_4ebf8b)
            }
            ParkConstant.CAR_TYPE_GREEN -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_green_4ebf8b)
            }
            else -> {
            }
        }
    }

    private fun showTitleInfoByCondition(detail: SettleDetail) {
        val rightLayout = mTitleBar?.getLinearLayout(Gravity.END) ?: return
        val rightTextView = View.inflate(mContext, R.layout.view_right_title, null)
        rightLayout.removeAllViews()
        if (detail.count > 0) {
            rightLayout.addView(rightTextView)
            rightTextView.setOnClickListener {
                showSignConfirm()
            }
            setViewGone(rightTextView, true)
        } else {
            setViewGone(rightTextView, false)
        }
    }


    private fun requestFlagArrears(parkId: Long) {
        ApiRepository.getInstance().requestFlagArrears(parkId).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<Any>>() {
            override fun onRequestSuccess(entity: BaseResult<Any>?) {
                if (entity == null) {
                    return
                }
                if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
                    ToastUtil.showSuccess(entity.errMsg)
                } else {
                    ToastUtil.showFailed(entity.errMsg)
                }
            }
        })
    }

    private fun skipArrearsRecord() {
        val intent = Intent()
        intent.putExtra(EXTRA_CAR_ID, carId)
        intent.setClass(this@SettleFeeDetailActivity, PayArrearsRecordActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_FEE_RECORD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_FEE_RECORD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val arrearsIds = data.getSerializableExtra(EXTRA_ARREARS_IDS) as ArrayList<Long>?
                    if (arrearsIds != null) {
                        mArrearsIds = StringUtils.join(arrearsIds, ",")
                    }
                }
            }
            REQUEST_CODE_PAY_BY_SCAN -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val scanCode = StringUtil.getNotNullValue(data.getStringExtra(EXTRA_SCAN_RESULT))
                    doPayByScan(scanCode)
                }
            }
            else -> {
            }
        }
    }


    private fun doPayByScan(scanCode: String?) {
        mPayType = PAY_TYPE_SCAN
        if (settleId == -1L) {
            ToastUtil.showWarning("未获取到结算信息")
            return
        }
        requestPay(scanCode)
    }

    /**
     * 现金支付
     */
    private fun doPayByCash() {
        mPayType = PAY_TYPE_CASH
        requestPay(null)
    }

    private fun requestPay(scanCode: String?) {
        showLoading("正在支付...")
        ApiRepository.getInstance().requestPay(settleId, mPayType!!, scanCode, listParseIntArray(arrearsIdList)).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseObserver<BaseResult<PayResult?>>() {
            override fun onRequestSuccess(entity: BaseResult<PayResult?>?) {
                handlePaySuccess(entity)
            }

            override fun onRequestError(throwable: Throwable?) {
                super.onRequestError(throwable)
                closeLoading()
            }
        })
    }


    private fun skipScanCode() {
        val intent = Intent()
        intent.setClass(this@SettleFeeDetailActivity, ScanCodePayActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_PAY_BY_SCAN)
    }

    private fun handlePaySuccess(entity: BaseResult<PayResult?>?) {
        closeLoading()
        if (entity == null) {
            return
        }
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
            payResult = entity.data
            ToastUtil.showSuccess(entity.errMsg)
            skipPayResult(true)
        } else {
            ToastUtil.showFailed(entity.errMsg)
            skipPayResult(false)
        }
    }


    private fun showPayCashConfirm() {
        IosAlertDialog(mContext)
                .init()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setTitle("现金支付")
                .setMsg("确定使用现金支付吗?")
                .setPositiveButton("立即支付", View.OnClickListener { doPayByCash() })
                .setNegativeButton("取消", View.OnClickListener {
                }).show()
    }

    private fun skipPayResult(success: Boolean) {
        val intent = Intent()
        intent.putExtra(EXTRA_PAY_RESULT, payResult)
        intent.putExtra(EXTRA_PAY_STATUS, success)
        intent.putExtra(EXTRA_PARK_ID,settleId )
        intent.setClass(this@SettleFeeDetailActivity, PayResultActivity::class.java)
        startActivity(intent)
    }

    /**
     * 标为欠费弹窗
     */
    private fun showSignConfirm() {
        IosAlertDialog(mContext)
                .init()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setTitle("标为欠费")
                .setMsg("确定要标为欠费吗?")
                .setPositiveButton("确定", View.OnClickListener { requestFlagArrears(recordId) })
                .setNegativeButton("取消", View.OnClickListener {
                }).show()
    }
}