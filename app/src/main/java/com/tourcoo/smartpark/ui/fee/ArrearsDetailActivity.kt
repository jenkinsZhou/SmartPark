package com.tourcoo.smartpark.ui.fee

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.apkfuns.logutils.LogUtils
import com.basewin.aidl.OnPrinterListener
import com.basewin.define.GlobalDef
import com.basewin.models.BitmapPrintLine
import com.basewin.models.PrintLine
import com.basewin.models.TextPrintLine
import com.basewin.services.PrinterBinder
import com.basewin.services.ServiceManager
import com.basewin.zxing.utils.QRUtil
import com.newland.aidl.printer.AidlPrinter
import com.newland.aidl.printer.AidlPrinterListener
import com.newland.aidl.printer.PrinterCode
import com.pos.sdk.accessory.PosAccessoryManager
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.account.UserInfo
import com.tourcoo.smartpark.bean.fee.FeeCertificate
import com.tourcoo.smartpark.bean.fee.FeeDetail
import com.tourcoo.smartpark.config.AppConfig
import com.tourcoo.smartpark.constant.ParkConstant
import com.tourcoo.smartpark.core.CommonUtil
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.PrintConfig
import com.tourcoo.smartpark.print_old.DeviceConnectListener
import com.tourcoo.smartpark.print_old.DeviceService
import com.tourcoo.smartpark.print_old.PrintConstant
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.ui.record.WaitSettleListActivity
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_arrears_detail.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.text.SimpleDateFormat
import java.util.*

/**
 *@description : 欠费详情页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月21日15:40
 * @Email: 971613168@qq.com
 */
class ArrearsDetailActivity : BaseTitleActivity(), View.OnClickListener, OnRefreshListener, EasyPermissions.PermissionCallbacks {
    private var recordId: Long? = null
    private val handler = Handler(Looper.getMainLooper())
    private val printerCallback = PrinterListener()
    override fun getContentLayout(): Int {
        return R.layout.activity_arrears_detail
    }

    override fun initView(savedInstanceState: Bundle?) {
        recordId = intent?.getLongExtra(ParkConstant.EXTRA_RECORD_ID, -1)
        if (recordId == null || recordId!! < 0) {
            ToastUtil.showWarning("未获取到欠费信息")
            finish()
        }
        initRefreshLayout()
        initPrintClick()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("欠费详情")
    }


    private fun requestArrearsDetail() {
        ApiRepository.getInstance().requestArrearsDetail(recordId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<FeeDetail>>() {
            override fun onRequestSuccess(entity: BaseResult<FeeDetail>?) {
                commonRefreshLayout.finishRefresh()
                if (entity == null) {
                    return
                }
                if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
                    showFeeDetail(entity.data)
                } else {
                    ToastUtil.showFailed(entity.errMsg)
                }
            }

            override fun onRequestError(throwable: Throwable?) {
                super.onRequestError(throwable)
                commonRefreshLayout.finishRefresh(false)
            }

        })
    }

    private fun showFeeDetail(data: FeeDetail?) {
        if (data == null) {
            return
        }
        tvParkingName.text = StringUtil.getNotNullValueLine(data.parking)
        tvEnterTime.text = StringUtil.getNotNullValueLine(data.createdAt)
        tvExitTime.text = StringUtil.getNotNullValueLine(data.leaveAt)
        tvParkingDuration.text = StringUtil.getNotNullValueLine(data.duration)
        tvFeeCurrent.text = StringUtil.getNotNullValueLine("¥ " + data.fee)
        tvFeeArrears.text = StringUtil.getNotNullValueLine("¥ " + data.fee)
        tvFeeShould.text = StringUtil.getNotNullValueLine("¥ " + data.fee)
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
                tvPlantNum.background = CommonUtil.getDrawable(R.drawable.bg_radius_30_yellow_fbc95f)
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
        requestArrearsDetail()
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            else -> {
            }
        }

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        requestArrearsDetail()
    }

    private fun initRefreshLayout() {
        commonRefreshLayout.setEnableLoadMore(false)
        commonRefreshLayout.setOnRefreshListener(this)
        commonRefreshLayout.setRefreshHeader(ClassicsHeader(mContext))
    }


    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        val d1 = Date(time)
        return format.format(d1)
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
        try {
            requestPermissionAndPrint(result)
        }catch (e : Throwable){
            if (AppConfig.DEBUG_BODE) {
                ToastUtil.showFailed("打印出错：$e")
            } else {
                ToastUtil.showFailed(R.string.tips_print_error)
            }
        }


    }



    private fun doPrint(result: BaseResult<FeeCertificate>?) {
        if (result == null || result.data == null) {
            ToastUtil.showWarning("未获取到打印数据")
            return
        }
        printContent(result.data)
    }

    @AfterPermissionGranted(PrintConfig.REQUEST_PERMISSION)
    private fun requestPermissionAndPrint(baseResult: BaseResult<FeeCertificate>) {
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
                doPrint(baseResult)
            } else {
                //如果有权限 并且初始化了 直接打印
                LogUtils.i("打印机已经初始化")
                doPrint(baseResult)
            }
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(this, PrintConfig.REQUEST_PERMISSION, *perms)
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
//            doPrint(recordId)
            ToastUtil.showSuccess("授权成功，请重新点击打印按钮进行打印")
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
        } catch (e: Throwable) {
            handler.post {
                if (AppConfig.DEBUG_BODE) {
                    ToastUtil.showFailed("打印出错：$e")
                } else {
                    ToastUtil.showFailed("未匹配到对应打印模块或当前设备不支持打印")
                }
            }
        }
    }

}