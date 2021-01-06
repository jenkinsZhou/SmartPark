package com.tourcoo.smartpark.ui.record

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.apkfuns.logutils.LogUtils
import com.basewin.aidl.OnPrinterListener
import com.basewin.define.GlobalDef
import com.basewin.models.BitmapPrintLine
import com.basewin.models.PrintLine
import com.basewin.models.TextPrintLine
import com.basewin.services.PrinterBinder
import com.basewin.services.ServiceManager
import com.basewin.zxing.utils.QRUtil
import com.jakewharton.rxbinding2.widget.RxTextView
import com.pos.sdk.accessory.PosAccessoryManager
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.WaitSettleAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.park.ParkSpaceInfo
import com.tourcoo.smartpark.bean.fee.FeeCertificate
import com.tourcoo.smartpark.config.AppConfig
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.BaseObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.NetworkUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.PrintConfig
import com.tourcoo.smartpark.print.PrintConfig.REQUEST_PERMISSION
import com.tourcoo.smartpark.ui.fee.ExitPayFeeEnterActivity
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.keyboard.PlateKeyboardView
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_report_fee_common.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *@description : 待结算页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月15日17:16
 * @Email: 971613168@qq.com
 */
class WaitSettleListActivity : BaseTitleActivity(), OnRefreshListener, EasyPermissions.PermissionCallbacks {
    private var adapter: WaitSettleAdapter? = null
    private var keyboardView: PlateKeyboardView? = null

    private val handler = Handler(Looper.getMainLooper())

    private val printerCallback: PrinterListener = PrinterListener()

    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_common
    }


    override fun initView(savedInstanceState: Bundle?) {
        setViewGone(llSearch, true)
        initRefresh()
        initSearchInput()
    }

    override fun loadData() {
        super.loadData()
        getExitSpaceList(true)
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("待缴费订单")
    }

    private fun initRefresh() {
        feeRecordRefreshLayout.setEnableLoadMore(false)
        feeRecordRefreshLayout.setOnRefreshListener(this)
        feeRecordRefreshLayout.setRefreshHeader(ClassicsHeader(mContext))
        feeRecordRecyclerView.layoutManager = LinearLayoutManager(mContext)
        adapter = WaitSettleAdapter()
        adapter!!.bindToRecyclerView(feeRecordRecyclerView)
        adapter!!.setOnItemChildClickListener { adapter, view, position ->
            val info = adapter!!.data[position] as ParkSpaceInfo?
            when (view?.id) {
                R.id.tvPrintCertify -> {
                    if (info == null || info.recordId < 0) {
                        ToastUtil.showWarning("未获取到对应记录")
                        return@setOnItemChildClickListener
                    }

                    try {
                        requestPermissionAndPrint(info.recordId)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        if (AppConfig.DEBUG_BODE) {
                            ToastUtil.showFailed(e.toString())
                        } else {
                            ToastUtil.showFailed("未找到打印机或当前设备不支持打印功能")
                        }
                    }


                }
                R.id.tvSettle -> {
                    skipSettle(info!!.recordId, info.id)
                }
                else -> {
                }
            }

        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        getExitSpaceList(true)
    }


    private fun requestSpaceSettleList(plantNum: String?, needShowLoading: Boolean) {
        if (needShowLoading) {
            ApiRepository.getInstance().requestSpaceSettleList(plantNum).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<List<ParkSpaceInfo>>>() {
                override fun onRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
                    handleRequestSuccess(entity)
                }

                override fun onRequestError(throwable: Throwable?) {
                    super.onRequestError(throwable)
                    feeRecordRefreshLayout.finishRefresh(false)
                }

            })
        } else {
            ApiRepository.getInstance().requestSpaceSettleList(plantNum).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseObserver<BaseResult<List<ParkSpaceInfo>>>() {
                override fun onRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
                    handleRequestSuccess(entity)
                }

                override fun onRequestError(throwable: Throwable?) {
                    super.onRequestError(throwable)
                    feeRecordRefreshLayout.finishRefresh(false)
                }

            })
        }
    }


    private fun loadSpaceData(parkSpaceInfoList: List<ParkSpaceInfo>?) {
        if (parkSpaceInfoList == null) {
            return
        }
        if (adapter!!.emptyView == null) {
            val emptyView = LayoutInflater.from(mContext).inflate(R.layout.multi_status_layout_empty, null)
            adapter!!.emptyView = emptyView
            emptyView?.setOnClickListener {
                getExitSpaceList(true)
            }
        }
        adapter!!.setNewData(parkSpaceInfoList)
    }

    private fun getExitSpaceList(needShowLoading: Boolean) {
        requestSpaceSettleList(etPlantNum.text.toString(), needShowLoading)
    }


    private fun handleRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
        if (entity == null) {
            feeRecordRefreshLayout.finishRefresh(false)
            return
        }
        feeRecordRefreshLayout.finishRefresh(true)
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            loadSpaceData(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
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
        if (!PrintConfig.printSdkInitStatus) {
            ToastUtil.showFailed("未找到打印机或当前设备不支持打印功能")
            return
        }
        printContent(result.data!!)
    }


    private fun getCurrentTime(): String? {
        val time = System.currentTimeMillis() //long now = android.os.SystemClock.uptimeMillis();
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:SS")
        val d1 = Date(time)
        return format.format(d1)
    }


    override fun onDestroy() {
        keyboardView = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }


    private fun skipSettle(recordId: Long, parkId: Long) {
        LogUtils.i("recordId=" + recordId + ",parkId=" + parkId)
        val intent = Intent()
        intent.putExtra(SettleFeeDetailActivity.EXTRA_SETTLE_RECORD_ID, recordId)
        intent.putExtra(SettleFeeDetailActivity.EXTRA_PARK_ID, parkId)
        intent.setClass(mContext, SettleFeeDetailActivity::class.java)
        startActivity(intent)
    }


    /**
     * 禁止Edittext弹出软件盘，光标依然正常显示。
     */
    private fun disableShowSoftInput(editTest: EditText) {
        val cls = EditText::class.java
        var method: Method
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(editTest, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            method = cls.getMethod("setSoftInputShownOnFocus", Boolean::class.javaPrimitiveType)
            method.setAccessible(true)
            method.invoke(editTest, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initSearchInput() {
        keyboardView = PlateKeyboardView(mContext)
        disableShowSoftInput(etPlantNum)
        etPlantNum.setOnClickListener(View.OnClickListener {
            keyboardView?.showKeyboard(etPlantNum, InputType.TYPE_CLASS_PHONE)
        })
        keyboardView?.setOnKeyboardFinishListener(object : PlateKeyboardView.OnKeyboardFinishListener {
            override fun onFinish(input: String?) {
//                getExitSpaceList(true)
            }
        }, "关闭")
        listenInput(etPlantNum, ivDeleteSmall)
    }


    @SuppressLint("CheckResult")
    private fun listenInput(editText: EditText, imageView: ImageView) {
        setViewVisible(imageView, !TextUtils.isEmpty(editText.text.toString()))
        imageView.setOnClickListener { v: View? -> editText.setText("") }
        RxTextView.textChanges(editText)
                .debounce(ExitPayFeeEnterActivity.DELAY_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { charSequence -> charSequence.toString() }
                .subscribe { s ->
                    getExitSpaceList(false)
                }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                setViewVisible(imageView, s.isNotEmpty())
            }
        })
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
                try {
                    ServiceManager.getInstence().init(applicationContext)
                } catch (e: Throwable) {
                    PrintConfig.printSdkInitStatus = false
                }

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
}