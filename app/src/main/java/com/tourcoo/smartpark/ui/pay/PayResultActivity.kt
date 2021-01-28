package com.tourcoo.smartpark.ui.pay

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.apkfuns.logutils.LogUtils
import com.basewin.aidl.OnPrinterListener
import com.basewin.define.GlobalDef
import com.basewin.models.PrintLine
import com.basewin.models.TextPrintLine
import com.basewin.services.PrinterBinder
import com.basewin.services.ServiceManager
import com.pos.sdk.accessory.PosAccessoryManager
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.PayCertificate
import com.tourcoo.smartpark.bean.fee.PayResult
import com.tourcoo.smartpark.config.AppConfig
import com.tourcoo.smartpark.constant.PayConstant
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.NetworkUtil
import com.tourcoo.smartpark.core.utils.StackUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.PrintConfig
import com.tourcoo.smartpark.print.PrintConfig.*
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity.Companion.EXTRA_PARK_ID
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_pay_result.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.PermissionRequest
import java.text.SimpleDateFormat
import java.util.*

/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月05日11:42
 * @Email: 971613168@qq.com
 */
class PayResultActivity : BaseTitleActivity(), PermissionCallbacks {
    private var paySuccess: Boolean? = null
    private var payResult: PayResult? = null
    private var recordId: Long? = null
    private val handler = Handler(Looper.getMainLooper())
    private val printerCallback: PrinterListener = PrinterListener()

    private var printSdkHasInit = false
    override fun getContentLayout(): Int {
        return R.layout.activity_pay_result
    }

    override fun initView(savedInstanceState: Bundle?) {
        paySuccess = intent?.getBooleanExtra(SettleFeeDetailActivity.EXTRA_PAY_STATUS, false)
        recordId = intent?.getLongExtra(EXTRA_PARK_ID, -1L)
        payResult = intent?.getSerializableExtra(SettleFeeDetailActivity.EXTRA_PAY_RESULT) as PayResult?
        if (paySuccess == null) {
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
                try {
                    requestPermissionAndPrint()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    if (AppConfig.DEBUG_BODE) {
                        ToastUtil.showFailed(e.toString())
                    } else {
                        ToastUtil.showFailed(R.string.tips_print_error)
                    }
                }
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
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    private fun doPrint() {
        if(!NetworkUtil.isConnected(mContext)){
            ToastUtil.showFailed("网络未连接")
            return
        }
        //真正执行打印的地方
        if (!printSdkInitStatus) {
            ToastUtil.showFailed(R.string.tips_print_error)
            return
        }
        requestPayCertificate()
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
        printContent(result.data!!)
    }


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val d1 = Date(time)
        return format.format(d1)
    }

    private fun printContent(certificate: PayCertificate?) {
        if (certificate == null) {
            ToastUtil.showFailed("未获取到打印数据")
            return
        }
        try {
            val spVersion = PosAccessoryManager.getDefault().getVersion(PosAccessoryManager.VERSION_TYPE_SP)
            var spState = spVersion.substring(spVersion.length - 2).trim { it <= ' ' }
            when (spState) {
                "1", "2" -> spState = "Normal"
                "0" -> spState = "Locked"
                "3" -> spState = "Sensor Broken"
                else -> {
                }
            }
            if (spState == "Locked") {
                ToastUtil.showWarning("打印模块被锁定 无法打印")
                return
            }
            if (spState == "Sensor Broken") {
                ToastUtil.showWarning("打印模块传感器损坏或异常 无法打印")
                return
            }
            ServiceManager.getInstence().printer.cleanCache()
            ServiceManager.getInstence().printer.setPrintGray(PrintConfig.PRINT_GRAY_LEVEL)
            ServiceManager.getInstence().printer.setLineSpace(1)
            //set print type
            ServiceManager.getInstence().printer.printTypesettingType = GlobalDef.ANDROID_TYPESETTING
            val textPrintLine = TextPrintLine()
            textPrintLine.type = PrintLine.TEXT
            textPrintLine.position = TextPrintLine.CENTER
            textPrintLine.size = 40
            textPrintLine.content = certificate.title
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)



            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = "停车人存根"
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = "  "
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "操作员:" + certificate.member
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "泊位号:" + certificate.spaceNumber
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "车牌号:" + certificate.number
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "停车点:" + certificate.parking
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "到达时间:"+certificate.createdAt
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "离开时间:"+certificate.leaveAt
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "停车时长:" + certificate.duration
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "历史欠费:RMB(元)" + certificate.arrears
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "应收费用:RMB(元)" + certificate.totalFee
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content ="  "
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = "实收费用"
            textPrintLine.size = 44
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content =  "¥"+certificate.totalFee
            textPrintLine.size = 44
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.size = 36
            textPrintLine.content = PrintConfig.STR_LINE_SHORT
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            val payType = StringUtil.getNotNullValue(certificate.payType,"其他")
          /*  payType = when (certificate.payType) {
                PayConstant.PAY_TYPE_ALI -> "支付宝支付"
                PayConstant.PAY_TYPE_WEI_XIN -> "微信支付"
                PayConstant.PAY_TYPE_CASH -> "现金支付"
                PayConstant.PAY_TYPE_MINI-> "小程序支付"
                PayConstant.PAY_TYPE_FREE-> "免费"
                else -> {
                    "其他支付"
                }
            }*/

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content =  payType + "¥" + certificate.totalFee
            textPrintLine.size = 32
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content ="  "
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "订单编号:" + certificate.outTradeNo
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)






            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "交易时间:" + certificate.leaveAt
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "凭条打印时间:" + getCurrentTime()
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.size = TextPrintLine.FONT_LARGE
            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.STR_LINE_SHORT
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = certificate.btw
            textPrintLine.size = 18
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.LINE_FEED
            textPrintLine.size = 36
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)
            ServiceManager.getInstence().printer.beginPrint(printerCallback)
        } catch (e: Throwable) {
            handler.post {
                if (AppConfig.DEBUG_BODE) {
                    ToastUtil.showFailed("打印出错：$e")
                } else {
                    ToastUtil.showFailed(R.string.tips_print_error)
                }
            }
        }
    }

    inner class PrinterListener : OnPrinterListener {
        private val TAG = "Print"
        override fun onStart() {
            handler.post {
                showLoading("正在打印...")
            }
        }

        override fun onFinish() {
            // TODO 打印结束
            // End of the print
            Log.e(TAG, "onFinish")
            handler.postDelayed(Runnable {
                closeLoading()
            }, 300)
        }

        override fun onError(errorCode: Int, detail: String) {
            // print error
            Log.e(TAG, "print error errorcode = $errorCode detail = $detail")
            handler.post(Runnable {
                closeLoading()
                when (errorCode) {
                    PrinterBinder.PRINTER_ERROR_NO_PAPER -> {
                        ToastUtil.showWarning("打印机缺纸")
                    }
                    PrinterBinder.PRINTER_ERROR_OVER_HEAT -> {
                        ToastUtil.showWarning("打印机过热")
                    }
                    PrinterBinder.PRINTER_ERROR_OTHER -> {
                        ToastUtil.showWarning("打印机繁忙或被占用")
                    }
                    else -> {
                        ToastUtil.showWarning("打印机未知异常")
                    }
                }
            })


        }
    }


    @AfterPermissionGranted(REQUEST_PERMISSION)
    private fun requestPermissionAndPrint() {
        val perms = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                "com.pos.permission.SECURITY",
                "com.pos.permission.ACCESSORY_DATETIME",
                "com.pos.permission.ACCESSORY_LED",
                "com.pos.permission.ACCESSORY_BEEP",
                "com.pos.permission.ACCESSORY_RFREGISTER",
                "com.pos.permission.CARD_READER_ICC",
                "com.pos.permission.CARD_READER_PICC",
                "com.pos.permission.CARD_READER_MAG",
                "com.pos.permission.COMMUNICATION",
                "com.pos.permission.PRINTER",
                "com.pos.permission.ACCESSORY_RFREGISTER",
                "com.pos.permission.EMVCORE"
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            if (!printSdkInitStatus) {
                printSdkInitStatus = try {
                    ServiceManager.getInstence().init(applicationContext)
                    true
                }catch (th : Throwable){
                    false
                }
                LogUtils.d("打印机未初始化")
                doPrint()
            } else {
                //如果有权限 并且初始化了 直接打印
                LogUtils.i("打印机已经初始化")
                doPrint()
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, REQUEST_PERMISSION, *perms)
                            .setRationale("请授予存储权限")
                            .setNegativeButtonText("否")
                            .setPositiveButtonText("是")
                            .build()
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this)
                    .setTitle("提示")
                    .setRationale("需要授予部分权限")
                    .setNegativeButton("拒绝")
                    .setPositiveButton("前往设置")
                    .setRequestCode(0x001)
                    .build()
                    .show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            Log.i("Granted", "onRequestPermissionsResult:" + requestCode)
            doPrint()
        }
    }


}