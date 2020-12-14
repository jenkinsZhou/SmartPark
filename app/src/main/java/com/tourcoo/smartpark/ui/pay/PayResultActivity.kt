package com.tourcoo.smartpark.ui.pay

import android.os.Bundle
import android.os.RemoteException
import com.apkfuns.logutils.LogUtils
import com.newland.aidl.printer.AidlPrinter
import com.newland.aidl.printer.AidlPrinterListener
import com.newland.aidl.printer.PrinterCode
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.fee.PayResult
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.utils.StackUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.DeviceConnectListener
import com.tourcoo.smartpark.print.DeviceService
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.ui.fee.ExitPayFeeDetailActivity
import com.tourcoo.smartpark.ui.fee.ExitPayFeeDetailActivity.Companion.EXTRA_PARK_ID
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
    private var iPrinter: AidlPrinter? = null
    private var deviceService: DeviceService? = null
    private var printEnable = false
    private var parkId: Long? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_pay_result
    }

    override fun initView(savedInstanceState: Bundle?) {
        paySuccess = intent?.getBooleanExtra(ExitPayFeeDetailActivity.EXTRA_PAY_STATUS, false)
        parkId = intent?.getLongExtra(EXTRA_PARK_ID, -1L)
        payResult = intent?.getSerializableExtra(ExitPayFeeDetailActivity.EXTRA_PAY_RESULT) as PayResult?
        if (payResult == null || paySuccess == null) {
            ToastUtil.showWarning("未获取到支付结果")
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
//        ApiRepository.getInstance().requestPayCertificate()
        printContent()
    }
    
    private fun printContent(){
        try {
            val format = Bundle()
            format.putString("font", "normal")
            format.putString("align", "center")
            format.putString("zmFont", "24")
//            format.putBoolean("linefeed", false) //不换行
            iPrinter?.addText(format, "宜兴市机动车停车凭证")
            format.putString("font", "normal")
            format.putString("align", "left")
            format.putString("zmFont", "13")
            format.putBoolean("linefeed", false)
            iPrinter?.addText(format, "操作员:")

            format.putString("font", "normal")
            format.putString("align", "right")
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format,AccountHelper.getInstance().userInfo.name)

            iPrinter?.addText(format, "\n")
            iPrinter?.startPrinter(object : AidlPrinterListener.Stub() {
                @Throws(RemoteException::class)
                override fun onFinish() {
//                iPrinter.paperSkip(3);
//                    showMessage(context.getString(R.string.msg_print_script_success), MessageTag.NORMAL)
                }

                @Throws(RemoteException::class)
                override fun onError(arg0: Int, arg1: String) {
//                    showMessage(context.getString(R.string.msg_print_script_error).toString() + arg1, MessageTag.NORMAL)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
//            showMessage(context.getString(R.string.msg_print_script_error) + e, MessageTag.ERROR)
        }
    }
}