package com.tourcoo.smartpark.ui.record

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.apkfuns.logutils.LogUtils
import com.jakewharton.rxbinding2.widget.RxTextView
import com.newland.aidl.printer.AidlPrinter
import com.newland.aidl.printer.AidlPrinterListener
import com.newland.aidl.printer.PrinterCode
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.WaitSettleAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.ParkSpaceInfo
import com.tourcoo.smartpark.bean.account.UserInfo
import com.tourcoo.smartpark.bean.fee.FeeCertificate
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.BaseObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.print.DeviceConnectListener
import com.tourcoo.smartpark.print.DeviceService
import com.tourcoo.smartpark.print.PrintConstant
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.ui.fee.ExitPayFeeEnterActivity
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.keyboard.PlateKeyboardView
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_exit_pay_fee_enter.*
import kotlinx.android.synthetic.main.activity_report_fee_common.*
import kotlinx.android.synthetic.main.activity_report_fee_common.etPlantNum
import kotlinx.android.synthetic.main.activity_report_fee_common.ivDeleteSmall
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *@description : JenkinsZhou
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月15日17:16
 * @Email: 971613168@qq.com
 */
class WaitSettleListActivity : BaseTitleActivity(), OnRefreshListener {
    private var iPrinter: AidlPrinter? = null
    private var deviceService: DeviceService? = null
    private var printEnable = false
    private var adapter: WaitSettleAdapter? = null
    private var keyboardView: PlateKeyboardView? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_common
    }


    override fun initView(savedInstanceState: Bundle?) {
        setViewGone(llSearch, true)
        initRefresh()
        initSearchInput()
        initPrinter()
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
                    doPrint(info.recordId)
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


    private fun doPrint(recordId: Long) {
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
                requestFeeCertificate(recordId)
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
    private fun requestFeeCertificate(recordId: Long?) {
        ApiRepository.getInstance().requestFeeCertificate(recordId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<FeeCertificate>>() {
            override fun onRequestSuccess(entity: BaseResult<FeeCertificate>?) {
                handleCertificateCallback(entity)
            }
        })
//        printContent(AccountHelper.getInstance().userInfo,)
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
        printContent(AccountHelper.getInstance().userInfo, result.data!!)
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

    private fun skipSettle(recordId: Long, parkId: Long) {
        LogUtils.i("recordId=" + recordId + ",parkId=" + parkId)
        val intent = Intent()
        intent.putExtra(SettleFeeDetailActivity.EXTRA_SETTLE_RECORD_ID, recordId)
        intent.putExtra(SettleFeeDetailActivity.EXTRA_PARK_ID, parkId)
        intent.setClass(mContext, SettleFeeDetailActivity::class.java)
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
}