package com.tourcoo.smartpark.ui.fee

import android.os.Bundle
import android.os.RemoteException
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.apkfuns.logutils.LogUtils
import com.newland.aidl.printer.AidlPrinter
import com.newland.aidl.printer.AidlPrinterListener
import com.newland.aidl.printer.PrinterCode
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.account.UserInfo
import com.tourcoo.smartpark.bean.fee.FeeCertificate
import com.tourcoo.smartpark.bean.fee.FeeDetail
import com.tourcoo.smartpark.constant.ParkConstant
import com.tourcoo.smartpark.constant.ParkConstant.EXTRA_RECORD_ID
import com.tourcoo.smartpark.core.CommonUtil
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.DeviceConnectListener
import com.tourcoo.smartpark.print.DeviceService
import com.tourcoo.smartpark.print.PrintConstant
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.dialog.IosAlertDialog
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_arrears_detail.*
import kotlinx.android.synthetic.main.activity_fee_detail.*
import kotlinx.android.synthetic.main.activity_fee_detail.commonRefreshLayout
import kotlinx.android.synthetic.main.activity_fee_detail.tvEnterTime
import kotlinx.android.synthetic.main.activity_fee_detail.tvExitTime
import kotlinx.android.synthetic.main.activity_fee_detail.tvFeeCurrent
import kotlinx.android.synthetic.main.activity_fee_detail.tvFeeShould
import kotlinx.android.synthetic.main.activity_fee_detail.tvParkingDuration
import kotlinx.android.synthetic.main.activity_fee_detail.tvParkingName
import kotlinx.android.synthetic.main.activity_fee_detail.tvPlantNum
import kotlinx.android.synthetic.main.activity_fee_detail.tvSpaceNum
import java.text.SimpleDateFormat
import java.util.*

/**
 *@description : 收费详情页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月21日15:00
 * @Email: 971613168@qq.com
 */
class FeeDetailActivity : BaseTitleActivity(), View.OnClickListener, OnRefreshListener {
    private var iPrinter: AidlPrinter? = null
    private var deviceService: DeviceService? = null
    private var printEnable = false
    private var recordId: Long? = null
    private var mNeedIgnore = false
    override fun getContentLayout(): Int {
        return R.layout.activity_fee_detail
    }

    override fun initView(savedInstanceState: Bundle?) {
        recordId = intent?.getLongExtra(EXTRA_RECORD_ID, -1)
        if (recordId == null || recordId!! < 0) {
            ToastUtil.showWarning("未获取到收费信息")
            finish()
        }
        tvIgnoreHistoryFee.setOnClickListener(this)
        initRefreshLayout()
        initPrinter()
        initPrintClick()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("收费详情")
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
                .setPositiveButton("确定", View.OnClickListener { requestFlagArrears(recordId!!) })
                .setNegativeButton("取消", View.OnClickListener {
                }).show()
    }

    private fun requestFeeDetail(needIgnore: Boolean) {
        ApiRepository.getInstance().requestFeeDetail(recordId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<FeeDetail>>() {
            override fun onRequestSuccess(entity: BaseResult<FeeDetail>?) {
                commonRefreshLayout.finishRefresh(true)
                if (entity == null) {
                    return
                }
                if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
                    showFeeDetail(entity.data, needIgnore)
                    mNeedIgnore = !mNeedIgnore
                } else {
                    ToastUtil.showFailed(entity.errMsg)
                }
            }

            override fun onRequestError(throwable: Throwable?) {
                commonRefreshLayout.finishRefresh(false)
                super.onRequestError(throwable)
            }
        })
    }

    private fun showFeeDetail(data: FeeDetail?, ignore: Boolean) {
        if (data == null) {
            return
        }

        if (ignore) {
            tvIgnoreHistoryFee.text = StringUtil.getNotNullValueLine("[已忽略]")
        } else {
            tvIgnoreHistoryFee.text = StringUtil.getNotNullValueLine("[忽略]")
        }
        tvParkingName.text = StringUtil.getNotNullValueLine(data.parking)
        tvEnterTime.text = StringUtil.getNotNullValueLine(data.createdAt)
        tvExitTime.text = StringUtil.getNotNullValueLine(data.leaveAt)
        tvParkingDuration.text = StringUtil.getNotNullValueLine(data.duration)
        tvFeeCurrent.text = StringUtil.getNotNullValueLine("¥ " + data.fee)
        tvFeeHistory.text = StringUtil.getNotNullValueLine("¥ " + data.arrears)
        tvFeeShould.text = StringUtil.getNotNullValueLine("¥ " + data.totalFee)
        showCarInfo(data)
    }

    private fun showCarInfo(detail: FeeDetail) {
        tvSpaceNum.text = StringUtil.getNotNullValueLine(detail.number)
        tvPlantNum.text = StringUtil.getNotNullValueLine(detail.carNumber)
        when (detail.type) {
            ParkConstant.CAR_TYPE_NORMAL -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_blue_5087ff)
            }
            ParkConstant.CAR_TYPE_YELLOW -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.shape_gradient_radius_30_green_4ebf8b)
            }
            ParkConstant.CAR_TYPE_GREEN -> {
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.shape_gradient_radius_30_green_4ebf8b)
            }
            else -> {
            }
        }
    }

    override fun loadData() {
        super.loadData()
        requestFeeDetail(mNeedIgnore)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvIgnoreHistoryFee -> {
                requestFeeDetail(!mNeedIgnore)
            }
            else -> {
            }
        }

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        requestFeeDetail(mNeedIgnore)
    }

    private fun initRefreshLayout() {
        commonRefreshLayout.setEnableLoadMore(false)
        commonRefreshLayout.setOnRefreshListener(this)
        commonRefreshLayout.setRefreshHeader(ClassicsHeader(mContext))
    }


    /**
     * 获取打印凭条内容
     */
    private fun requestFeeCertificate(recordId: Long?) {
        ApiRepository.getInstance().requestFeeCertificate(recordId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<FeeCertificate>>() {
            override fun onRequestSuccess(entity: BaseResult<FeeCertificate>?) {
                handleCertificateCallback(entity)
            }
        })
    }


    private fun handleCertificateCallback(result: BaseResult<FeeCertificate>?) {
        if (result == null) {
            ToastUtil.showFailed("服务器数据异常")
            return
        }
        if (result.code != RequestConfig.REQUEST_CODE_SUCCESS) {
            ToastUtil.showFailed(result.errMsg)
            return
        }
        if (result.data == null) {
            ToastUtil.showFailed("服务器数据异常")
            return
        }
       doPrint(result)
    }


    private fun printContent(userInfo: UserInfo, certificate: FeeCertificate) {
        showLoading("正在打印...")
        try {
            //设置纸张大小为两英寸（5.08cm）
            iPrinter?.setPaperSize(0x00)
            //设置间距 0-60 默认为6
            iPrinter?.setPaperSize(6)
            val format = Bundle()
            format.putInt("zoom", 4)
            format.putString("font", PrintConstant.FONT_SIZE_SMALL)
            format.putString("align", PrintConstant.GRAVITY_CENTER)
            iPrinter?.addText(format, "宜兴车辆停车凭证")

            iPrinter?.addText(format, "\n")

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "操作员:" + userInfo.name)


            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "泊位号:" + certificate.spaceNumber)

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "车牌号:" + certificate.number)

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "停车点:" + certificate.parking)

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            iPrinter?.addText(format, "到达时间:")
            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_RIGHT)
            iPrinter?.addText(format, certificate.createdAt)
            iPrinter?.addText(format, "\n")

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putInt("zoom", 3)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "请使用微信扫码缴费")


            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putInt("zoom", 3)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "二维码:")
            iPrinter?.addText(format, "\n")


            format.putString("align", "center")
            format.putInt("height", 280)
            iPrinter?.addQrCode(format, StringUtil.getNotNullValueLine(certificate.codeContent))
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")

            format.putString("font", PrintConstant.FONT_SIZE_NORMAL)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "凭条打印时间:" + getCurrentTime())


            iPrinter?.addText(format, "line")
            format.putString("font", "normal")


            format.putString("font", PrintConstant.FONT_SIZE_SMALL)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "免责声明:本泊位系统利用爱神的箭阿萨德爱仕达大所大所阿斯蒂芬斯蒂芬")
            format.putString("font", PrintConstant.FONT_SIZE_SMALL)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.startPrinter(object : AidlPrinterListener.Stub() {
                @Throws(RemoteException::class)
                override fun onFinish() {
                    closeLoading()
                }

                @Throws(RemoteException::class)
                override fun onError(arg0: Int, arg1: String) {
                    ToastUtil.showFailedDebug("打印出错:$arg1")
                    closeLoading()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showFailedDebug("打印故障:$e")
            closeLoading()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        disconnectPrinter()
    }

    private fun disconnectPrinter() {
        deviceService?.disconnect()
        deviceService?.connectListener = null
        iPrinter = null
        printEnable = false
        deviceService = null
    }


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val d1 = Date(time)
        return format.format(d1)
    }

    private fun initPrinter() {
        deviceService = DeviceService(mContext, object : DeviceConnectListener {
            override fun deviceConnectSuccess() {
                LogUtils.i("deviceConnectSuccess")
                if (deviceService != null) {
                    if (iPrinter == null) {
                        iPrinter = DeviceService.getPrinter()
                    }
                    printEnable = true
                }
            }

            override fun deviceDisConnected() {
                printEnable = false
                LogUtils.e("deviceDisConnected")
            }

            override fun deviceConnecting() {
                printEnable = false
                LogUtils.d("deviceConnecting")
            }

            override fun deviceNoConnect() {
                printEnable = false
                LogUtils.e("deviceNoConnect")
            }

        })
        deviceService?.connect()
    }

    private fun initPrintClick() {
        val rightLayout = mTitleBar?.getLinearLayout(Gravity.END) ?: return
        rightLayout.removeAllViews()
        val rightTextView = View.inflate(mContext, R.layout.view_right_title, null) as TextView
        rightTextView.text = "打印凭条"
        rightLayout.addView(rightTextView)
        rightTextView.setOnClickListener {
            requestFeeCertificate(recordId)
        }
    }


    private fun doPrint(result: BaseResult<FeeCertificate>) {
        if (AccountHelper.getInstance().userInfo == null) {
            ToastUtil.showWarning("未获取到收费员信息")
            return
        }
        if (!printEnable) {
            ToastUtil.showWarning("打印机繁忙或未连接")
            return
        }
        if (iPrinter == null) {
            ToastUtil.showWarning("打印机未连接")
            return
        }
        when (iPrinter?.status) {
            //正常
            PrinterCode.PrinterState.PRINTER_NORMAL -> {
                //真正执行打印的地方
                printContent(AccountHelper.getInstance().userInfo, result.data!!)
            }
            PrinterCode.PrinterState.PRINTER_OUTOF_PAPER -> {
                //打印机缺纸
                ToastUtil.showWarning("打印机缺纸")
                return
            }

            PrinterCode.PrinterState.PRINTER_HEAT_LIMITED -> {
                //打印机缺纸
                ToastUtil.showWarning("打印机超温")
                return
            }

            PrinterCode.PrinterState.PRINTER_BUSY -> {
                //打印机缺纸
                ToastUtil.showWarning("打印机繁忙")
                return
            }
            else -> {
                ToastUtil.showWarning("打印机不可用")
                return
            }
        }
    }
}