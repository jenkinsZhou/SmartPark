package com.tourcoo.smartpark.ui.report

import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import com.apkfuns.logutils.LogUtils
import com.newland.aidl.printer.AidlPrinter
import com.newland.aidl.printer.AidlPrinterListener
import com.newland.aidl.printer.PrinterCode
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.report.DailyReport
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
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_report_fee_daily.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *@description : 收费日报
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月09日9:30
 * @Email: 971613168@qq.com
 */
class DailyFeeReportActivity : BaseTitleActivity() {
    private var iPrinter: AidlPrinter? = null
    private var deviceService: DeviceService? = null
    private var printEnable = false
    private var dailyReport: DailyReport? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_daily
    }

    override fun initView(savedInstanceState: Bundle?) {
        llFeeRecord.setOnClickListener {
            doSkipReportList()
        }

        llPayPrintCode.setOnClickListener {
            doPrintDailyReport(dailyReport)
        }
        initPrinter()
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


    /**
     * 真正打印凭条的地方
     */
    private fun printContent(report: DailyReport) {
        printEnable = false
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
            iPrinter?.addText(format, "收费日报")

            iPrinter?.addText(format, "\n")

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "收费单位:" + report.parking)


            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "收费员姓名:" + report.name)

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "收费员编号:" + report.number)

            iPrinter?.addText(format, "line")
            format.putString("font", "normal")

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "总车次(次):" + report.carNum)

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "总应收(元):" + "¥" + report.theoreticalIncome)

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "总实收(元):" + "¥" + report.actualIncome)

            iPrinter?.addText(format, "line")
            format.putString("font", "normal")

            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "线上支付(元):" + "¥ " + report.onlineIncome)


            format.putString("font", PrintConstant.FONT_SIZE_LARGE)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "线下支付(元):" + "¥ " + report.offlineIncome)

            iPrinter?.addText(format, "\n")

            format.putString("font", PrintConstant.FONT_SIZE_NORMAL)
            format.putInt("zoom", 3)
            format.putString("align", PrintConstant.GRAVITY_LEFT)
            format.putBoolean("linefeed", true)
            iPrinter?.addText(format, "凭条打印时间:" + getCurrentTime())




            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.addText(format, "\n")
            iPrinter?.startPrinter(object : AidlPrinterListener.Stub() {
                @Throws(RemoteException::class)
                override fun onFinish() {
                    printEnable =true
                    ToastUtil.showSuccess("打印完成")
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
            printEnable =true
            closeLoading()
        }
    }


    private fun doPrintDailyReport(dailyReport: DailyReport?) {
        if (dailyReport == null) {
            ToastUtil.showWarning("未获取到日报数据")
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
                printContent(dailyReport)
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


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:SS")
        val d1 = Date(time)
        return format.format(d1)
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
}