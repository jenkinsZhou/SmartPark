package com.tourcoo.smartpark.ui.report

import android.Manifest
import android.content.Intent
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
import com.tourcoo.smartpark.bean.report.DailyReport
import com.tourcoo.smartpark.config.AppConfig
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.PrintConfig
import com.tourcoo.smartpark.print.PrintConfig.REQUEST_PERMISSION
import com.tourcoo.smartpark.print.PrintConfig.printSdkInitStatus
import com.tourcoo.smartpark.ui.pay.PayResultActivity
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_report_fee_daily.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.text.SimpleDateFormat
import java.util.*

/**
 *@description : 收费日报
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月09日9:30
 * @Email: 971613168@qq.com
 */
class DailyFeeReportActivity : BaseTitleActivity(), EasyPermissions.PermissionCallbacks {
    private var dailyReport: DailyReport? = null
    private var printSdkHasInit = false

    private val handler = Handler(Looper.getMainLooper())

    private val printerCallback: PrinterListener = PrinterListener()
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_daily
    }

    override fun initView(savedInstanceState: Bundle?) {
        llFeeRecord.setOnClickListener {
            doSkipReportList()
        }

        llPayPrintCode.setOnClickListener {
            requestPermissionAndPrint()

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
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
            dailyReport = entity.data
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
        intent.setClass(this@DailyFeeReportActivity, DailyFeeRecordListActivity::class.java)
        startActivity(intent)
    }




    private fun doPrintDailyReport(dailyReport: DailyReport?) {
        if (dailyReport == null) {
            ToastUtil.showWarning("未获取到日报数据")
            return
        }
        printContent(dailyReport)
    }


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:SS")
        val d1 = Date(time)
        return format.format(d1)
    }


    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    private fun printContent(certificate: DailyReport) {
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
            textPrintLine.content = "收费日报"
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)



            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "收费单位:" + certificate.parking
            textPrintLine.size = 30
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "收费员姓名:" + certificate.name
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "收费员编号:" + certificate.number
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.size =  TextPrintLine.FONT_LARGE
            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.STR_LINE_SHORT
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.size = 30
            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "总车次(次):" + certificate.carNum
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "总应收(元):" + "¥" + certificate.theoreticalIncome
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "总实收(元):" + "¥" + certificate.actualIncome
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.size = TextPrintLine.FONT_LARGE
            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.STR_LINE_SHORT
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.size = 30
            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "线上支付(元):" + "¥ " + certificate.onlineIncome
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.content = "线下支付(元):" + "¥ " + certificate.offlineIncome
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.CENTER
            textPrintLine.content = PrintConfig.LINE_FEED_SHORT
            textPrintLine.size = 36
            ServiceManager.getInstence().printer.addPrintLine(textPrintLine)

            textPrintLine.position = PrintLine.LEFT
            textPrintLine.size = 20
            textPrintLine.content = "凭条打印时间:" + getCurrentTime()
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
                ServiceManager.getInstence().init(applicationContext)
                printSdkHasInit = true
                printSdkInitStatus = true
                LogUtils.d("打印机未初始化")
                doPrintDailyReport(dailyReport)
            } else {
                //如果有权限 并且初始化了 直接打印
                LogUtils.i("打印机已经初始化")
                doPrintDailyReport(dailyReport)
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, REQUEST_PERMISSION, *perms)
                            .setRationale("Dear users\n need to apply for storage Permissions for\n your better use of this application")
                            .setNegativeButtonText("NO")
                            .setPositiveButtonText("YES")
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
         doPrintDailyReport(dailyReport)
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

            /*  if (errorCode == PrinterBinder.PRINTER_ERROR_NO_PAPER) {
                  //Toast.makeText(MainActivity.this, "paper runs out during printing", Toast.LENGTH_SHORT).show();
              }
              if (errorCode == PrinterBinder.PRINTER_ERROR_OVER_HEAT) {
              }
              if (errorCode == PrinterBinder.PRINTER_ERROR_OTHER) {
              }*/

            //handler.sendMessageDelayed(null, 1000);
//            handler.sendEmptyMessageDelayed(1, 1000)
        }
    }
}