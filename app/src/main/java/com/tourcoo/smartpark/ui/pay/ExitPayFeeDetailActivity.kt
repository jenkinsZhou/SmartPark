package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import android.view.View
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
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.dialog.AppUpdateDialog
import com.tourcoo.smartpark.widget.dialog.CommonInputDialog
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_exit_pay_fee_settle_detail.*


/**
 *@description :离场收费详情(离场结算页面)
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月09日9:12
 * @Email: 971613168@qq.com
 */
class ExitPayFeeDetailActivity : BaseTitleActivity(), View.OnClickListener, OnRefreshListener {
    private var recordId = -1L

    companion object {
        const val EXTRA_SETTLE_RECORD_ID = "EXTRA_SETTLE_RECORD_ID"
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_exit_pay_fee_settle_detail
    }

    override fun initView(savedInstanceState: Bundle?) {
        recordId = intent?.getLongExtra(EXTRA_SETTLE_RECORD_ID, -1L)!!
        llPayByCash.setOnClickListener(this)
        llPayByCode.setOnClickListener(this)
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
                    ToastUtil.showSuccess("立即更新")
                    dialog.dismiss()
                }
                dialog.show()
            }
            R.id.llPayByCode -> {
                ToastUtil.showSuccess("点击了")
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
        requestSettleInfo(true)
    }

    private fun showSettleInfo(settleDetail: SettleDetail?) {
        if (settleDetail == null) {
            return
        }
        tvEnterTime.text = StringUtil.getNotNullValueLine(settleDetail.createdAt)
        tvExitTime.text = StringUtil.getNotNullValueLine(settleDetail.leaveAt)
        tvFeeCurrent.text = StringUtil.getNotNullValueLine("¥ " + settleDetail.fee)
        tvFeeHistory.text = StringUtil.getNotNullValueLine("¥ " + settleDetail.arrears)
        tvFeeShould.text = StringUtil.getNotNullValueLine("¥ " + settleDetail.count)
        tvFeeReally.text = StringUtil.getNotNullValueLine("")
        showCarInfo(settleDetail)
    }


    private fun requestSettleInfo(needIgnore: Boolean) {
        ApiRepository.getInstance().requestSpaceSettleDetail(recordId, needIgnore).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<SettleDetail>>() {
            override fun onRequestSuccess(entity: BaseResult<SettleDetail>?) {
                handleRequestSuccess(entity)
            }

            override fun onRequestError(throwable: Throwable?) {
                super.onRequestError(throwable)
                settleRefreshLayout.finishRefresh(false)
            }

        })
    }


    private fun handleRequestSuccess(entity: BaseResult<SettleDetail>?) {
        if (entity == null) {
            settleRefreshLayout.finishRefresh(false)
            return
        }
        settleRefreshLayout.finishRefresh(true)
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            showSettleInfo(entity.data)

        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }

    private fun showCarInfo(detail: SettleDetail){
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

}