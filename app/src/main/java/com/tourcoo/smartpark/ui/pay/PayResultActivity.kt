package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import android.os.RemoteException
import com.apkfuns.logutils.LogUtils
import com.newland.aidl.printer.AidlPrinter
import com.newland.aidl.printer.AidlPrinterListener
import com.newland.aidl.printer.PrinterCode
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.account.UserInfo
import com.tourcoo.smartpark.bean.fee.PayCertificate
import com.tourcoo.smartpark.bean.fee.PayResult
import com.tourcoo.smartpark.constant.PayConstant
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.StackUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.DeviceConnectListener
import com.tourcoo.smartpark.print.DeviceService
import com.tourcoo.smartpark.print.PrintConstant.*
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity.Companion.EXTRA_PARK_ID
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_pay_result.*
import java.text.SimpleDateFormat
import java.util.*

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
    private var iPrinter: AidlPrinter? = null
    private var deviceService: DeviceService? = null
    private var printEnable = false
    private var recordId: Long? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_pay_result
    }

    override fun initView(savedInstanceState: Bundle?) {
        paySuccess = intent?.getBooleanExtra(SettleFeeDetailActivity.EXTRA_PAY_STATUS, false)
        recordId = intent?.getLongExtra(EXTRA_PARK_ID, -1L)
        payResult = intent?.getSerializableExtra(SettleFeeDetailActivity.EXTRA_PAY_RESULT) as PayResult?
        if (payResult == null || paySuccess == null) {
            ToastUtil.showWarning("未获取到支付结果")
            finish()
        }
        if (recordId == null) {
            ToastUtil.showWarning("未获取到停车信息")
            finish()
        }
        showPayResult()
        tvConfirm.setOnClickListener {
            if (paySuccess!!) {
                doPrint()
            } else {
                payRetry()
            }
        }
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
        StackUtil.getInstance().getActivity(SettleFeeDetailActivity::class.java)?.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (paySuccess!!) {
            backHome()
        } else {
            payRetry()
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


    private fun doPrint() {
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
                requestPayCertificate()
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

    /**
     * 获取打印凭条内容
     */
    private fun requestPayCertificate() {
        ApiRepository.getInstance().requestPayCertificate(recordId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<PayCertificate>>() {
            override fun onRequestSuccess(entity: BaseResult<PayCertificate>?) {
                handleCertificateCallback(entity)
            }
        })
//        printContent(AccountHelper.getInstance().userInfo,)
    }

    private fun printContent(userInfo: UserInfo, certificate: PayCertificate) {
        showLoading("正在打印...")
        try {
            //设置纸张大小为两英寸（5.08cm）
            iPrinter?.setPaperSize(0x00)
            //设置间距 0-60 默认为6
            iPrinter?.setPaperSize(6)
            val format = Bundle()
            format.putInt("zoom", 4)
            format.putString("font", FONT_SIZE_SMALL)
            format.putString("align", GRAVITY_CENTER)
            iPrinter?.addText(format, certificate.title)

            iPrinter?.addText(format, "\n")

            format.putString("font", FONT_SIZE_NORMAL)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_CENTER)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "停车人存根")

            iPrinter?.addText(format, "\n")

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "操作员:" + certificate.member)


            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "泊位号:" + certificate.spaceNumber)

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "车牌号:" + certificate.number)

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "停车点:" + certificate.parking)

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            iPrinter?.addText(format, "到达时间:")
            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_RIGHT)
            iPrinter?.addText(format, certificate.createdAt)
            iPrinter?.addText(format, "\n")

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "离开时间:" )

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_RIGHT)
            iPrinter?.addText(format,  certificate.leaveAt)
            iPrinter?.addText(format, "\n")

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "停车时长:" + certificate.duration)


            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "本次费用:RMB(元)" + certificate.fee)

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "历史费用:RMB(元)" + certificate.arrears)

            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "应收费用:RMB(元)" + certificate.arrears)

            /*    format.putString("font", FONT_SIZE_LARGE)
                format.putString("align", GRAVITY_LEFT)
                format.putInt("zoom", 3)
                format.putBoolean("linefeed", true)
                iPrinter?.addText(format, "请使用微信扫码缴费")*/
            format.putString("font", FONT_SIZE_SMALL)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            iPrinter?.addText(format, "\n")

            format.putInt("zoom", 4)
            format.putString("font", FONT_SIZE_SMALL)
            format.putString("align", GRAVITY_CENTER)
            iPrinter?.addText(format, "实收费用:RMB(元)" + "¥ "+certificate.totalFee)
            iPrinter?.addText(format, "line")
            format.putString("font", "normal")
            var payType = ""
            when (certificate.payType) {
                PayConstant.PAY_TYPE_ALI -> payType = "支付宝支付"
                PayConstant.PAY_TYPE_WEI_XIN -> payType = "微信支付"
                PayConstant.PAY_TYPE_CASH -> payType = "现金支付"
                else -> {
                }
            }
            format.putString("font", FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, payType + ":RMB(元)" + certificate.totalFee)

            format.putInt("hzFont", 9)
            format.putInt("zmFont", 38)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "\n")

            format.putInt("hzFont", 9)
            format.putInt("zmFont", 38)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "交易时间:" + certificate.leaveAt)

            iPrinter?.addText(format, "\n")

            format.putString("font", FONT_SIZE_NORMAL)
            format.putInt("hzFont", 9)
            format.putInt("zmFont", 38)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "订单编号:" + certificate.outTradeNo)


            iPrinter?.addText(format, "\n")
            /*   format.putString("align", "center")
               format.putInt("height", 200)
               iPrinter?.addQrCode(format, StringUtil.getNotNullValueLine(certificate.codeContent))
               iPrinter?.addText(format, "\n")
               iPrinter?.addText(format, "\n")*/

            format.putString("font", FONT_SIZE_NORMAL)
            format.putInt("hzFont", 9)
            format.putInt("zmFont", 38)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "凭条打印时间:" + getCurrentTime())

            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "line")
            format.putString("font", "normal")

            iPrinter?.addText(format, "\n")
            format.putString("font", FONT_SIZE_SMALL)
            format.putInt("hzFont", 9)
            format.putInt("zmFont", 38)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, certificate.btw)
            format.putString("font", FONT_SIZE_SMALL)
            format.putInt("zoom", 3)
            format.putString("align", GRAVITY_LEFT)
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


    private fun handleCertificateCallback(result: BaseResult<PayCertificate>?) {
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
        //真正执行打印的地方
        printContent(AccountHelper.getInstance().userInfo, result.data!!)
    }


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val d1 = Date(time)
        return format.format(d1)
    }
}