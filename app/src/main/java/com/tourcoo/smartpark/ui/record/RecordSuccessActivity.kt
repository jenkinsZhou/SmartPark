package com.tourcoo.smartpark.ui.record

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.apkfuns.logutils.LogUtils
import com.basewin.aidl.OnPrinterListener
import com.basewin.define.GlobalDef
import com.basewin.models.BitmapPrintLine
import com.basewin.models.PrintLine
import com.basewin.models.TextPrintLine
import com.basewin.services.PrinterBinder
import com.basewin.services.ServiceManager
import com.basewin.zxing.utils.QRUtil
import com.pos.sdk.accessory.PosAccessoryManager
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.FeeCertificate
import com.tourcoo.smartpark.bean.fee.PayCertificate
import com.tourcoo.smartpark.config.AppConfig
import com.tourcoo.smartpark.constant.ParkConstant
import com.tourcoo.smartpark.constant.PayConstant
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.NetworkUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.PrintConfig
import com.tourcoo.smartpark.print.PrintConfig.REQUEST_PERMISSION
import com.tourcoo.smartpark.print.PrintConfig.printSdkInitStatus
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_record_success.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.text.SimpleDateFormat
import java.util.*

/**
 *@description : 登记成功
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2021年01月05日16:27
 * @Email: 971613168@qq.com
 */
class RecordSuccessActivity : BaseTitleActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private val handler = Handler(Looper.getMainLooper())
    private val printerCallback: PrinterListener = PrinterListener()
    private var recordId: Long? = null


    override fun getContentLayout(): Int {
        return R.layout.activity_record_success
    }

    override fun initView(savedInstanceState: Bundle?) {
        tvPrintCertify.setOnClickListener(this)
        recordId = intent?.getLongExtra(ParkConstant.EXTRA_RECORD_ID, -1)
        if (recordId == null || recordId!! < 0) {
            ToastUtil.showWarning("未获取到收费信息")
//            finish()
        }
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("登记结果")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvPrintCertify -> {
                requestPermissionAndPrint(recordId)
            }
            else -> {
            }
        }
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
            textPrintLine.content = "到达时间:" + certificate.createdAt
            textPrintLine.size = TextPrintLine.FONT_NORMAL
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "离开时间:" + certificate.leaveAt
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
            textPrintLine.content = "  "
            textPrintLine.size = 20
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = "实收费用"
            textPrintLine.size = 44
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = "¥" + certificate.totalFee
            textPrintLine.size = 44
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.size = 36
            textPrintLine.content = PrintConfig.STR_LINE_SHORT
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            var payType = ""
            when (certificate.payType) {
                PayConstant.PAY_TYPE_ALI -> payType = "支付宝支付"
                PayConstant.PAY_TYPE_WEI_XIN -> payType = "微信支付"
                PayConstant.PAY_TYPE_CASH -> payType = "现金支付"
                else -> {
                }
            }

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = payType + ":RMB(元)" + certificate.totalFee
            textPrintLine.size = 32
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = "  "
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
        } catch (e: java.lang.Exception) {
            handler.post {
                if (AppConfig.DEBUG_BODE) {
                    ToastUtil.showFailed("打印出错：$e")
                } else {
                    ToastUtil.showFailed("打印机出错")
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
            // TODO 打印出错
            // print error
            Log.e(TAG, "print error errorcode = $errorCode detail = $detail")
            handler.post(Runnable {
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
            ToastUtil.showSuccess("授权成功，请重新点击打印按钮进行打印")
        }
    }

    private fun doPrint(recordId: Long?) {
        if (recordId == null) {
            ToastUtil.showWarning("未获取到收费员信息")
            return
        }
        if (!NetworkUtil.isConnected(mContext)) {
            ToastUtil.showFailed(R.string.exception_network_not_connected)
            return
        }
        requestFeeCertificate(recordId)
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
        //真正执行打印的地方

        printContent(result.data!!)
    }


    @AfterPermissionGranted(REQUEST_PERMISSION)
    private fun requestPermissionAndPrint(recordId: Long?) {
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
            if (!PrintConfig.printSdkInitStatus) {
                ServiceManager.getInstence().init(applicationContext)
                PrintConfig.printSdkInitStatus = true
                LogUtils.d("打印机未初始化")
                doPrint(recordId)
            } else {
                //如果有权限 并且初始化了 直接打印
                LogUtils.i("打印机已经初始化")
                doPrint(recordId)
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


    private fun printContent(certificate: FeeCertificate) {
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
            textPrintLine.size = 44
            textPrintLine.content = certificate.title
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)



            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "收费单位:" + certificate.parking
            textPrintLine.size = 30
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "操作员:" + certificate.member
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "泊位号:" + certificate.spaceNumber
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)



            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "车牌号:" + certificate.number
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "停车点:" + certificate.parking
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "到达时间:"
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.RIGHT
            textPrintLine.content = certificate.createdAt
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "请使用微信扫码缴费"
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "二维码:"
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)
            val ewm = QRUtil.getRQBMP(StringUtil.getNotNullValueLine(certificate.codeContent), 300)
            val bitmapPrintLine = BitmapPrintLine()
            bitmapPrintLine.type = PrintLine.BITMAP
            bitmapPrintLine.position = PrintLine.CENTER
            bitmapPrintLine.bitmap = ewm
            ServiceManager.getInstence().printer.addPrintLine(bitmapPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.size = 20
            textPrintLine.content = "凭条打印时间:" + getCurrentTime()
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.size = TextPrintLine.FONT_LARGE
            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.STR_LINE_SHORT
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.size = 18
            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = StringUtil.getNotNullValueLine(certificate.btw)
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)


            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.LINE_FEED
            textPrintLine.size = 36
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)
            ServiceManager.getInstence().printer.beginPrint(printerCallback)
        } catch (e: java.lang.Exception) {
            handler.post {
                if (AppConfig.DEBUG_BODE) {
                    ToastUtil.showFailed("打印出错：$e")
                } else {
                    ToastUtil.showFailed("打印机出错")
                }
            }
        }
    }
}