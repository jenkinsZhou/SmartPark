package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.ParkSpaceInfo
import com.tourcoo.smartpark.bean.settle.SettleDetail
import com.tourcoo.smartpark.constant.CarConstant
import com.tourcoo.smartpark.core.CommonUtil
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.DrawableUtil
import com.tourcoo.smartpark.core.utils.SizeUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.dialog.AppUpdateDialog
import com.tourcoo.smartpark.widget.dialog.CommonInputDialog
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_exit_pay_fee_settle_detail.*
import java.util.*


/**
 *@description :离场收费详情(离场结算页面)
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月09日9:12
 * @Email: 971613168@qq.com
 */
class ExitPayFeeDetailActivity : BaseTitleActivity(), View.OnClickListener, OnRefreshListener {
    private var recordId = -1L

    /**
     * 停车记录id
     */
    private var parkId = -1L
    private var needIgnore = false

    companion object {
        const val EXTRA_SETTLE_RECORD_ID = "EXTRA_SETTLE_RECORD_ID"
        const val EXTRA_PARK_ID = "EXTRA_PARK_ID"
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_exit_pay_fee_settle_detail
    }

    override fun initView(savedInstanceState: Bundle?) {
        recordId = intent?.getLongExtra(EXTRA_SETTLE_RECORD_ID, -1L)!!
        parkId = intent?.getLongExtra(EXTRA_PARK_ID, -1L)!!
        if(recordId<0||parkId<0){
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
        requestSettleInfo(false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llPayByCash -> {
                val dialog: AppUpdateDialog = AppUpdateDialog(mContext).create()
                dialog.setPositiveButtonClick("立即更新") {
                    ToastUtil.showWarning("立即更新")
                    dialog.dismiss()
                }
                dialog.show()
            }
            R.id.llPayByCode -> {
                ToastUtil.showFailed("点击了")
            }
            R.id.tvIgnoreHistoryFee -> {
                requestSettleInfo(!needIgnore)
                needIgnore = !needIgnore
            }
            R.id.tvFeeHistory -> {
                ToastUtil.showSuccess("欠费详情")
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
        ApiRepository.getInstance().requestSpaceSettleDetail(recordId, needIgnore).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<SettleDetail>>() {
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
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            showSettleInfo(entity.data, ignore)

        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }

    private fun showCarInfo(detail: SettleDetail) {
        tvSpaceNum.text = StringUtil.getNotNullValueLine(detail.number)
        tvPlantNum.text = StringUtil.getNotNullValueLine(detail.carNumber)
        when (detail.type) {
            CarConstant.CAR_TYPE_NORMAL -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_blue_5087ff)
            }
            CarConstant.CAR_TYPE_YELLOW -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_green_4ebf8b)
            }
            CarConstant.CAR_TYPE_GREEN -> {
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
                requestFlagArrears(parkId)
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
}