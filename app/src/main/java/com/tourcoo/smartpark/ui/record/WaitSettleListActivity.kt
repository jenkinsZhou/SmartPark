package com.tourcoo.smartpark.ui.record

import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.apkfuns.logutils.LogUtils
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
import com.tourcoo.smartpark.bean.fee.PayCertificate
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
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity
import com.tourcoo.smartpark.util.StringUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_exit_pay_fee_enter.*
import kotlinx.android.synthetic.main.activity_report_fee_common.*
import java.text.SimpleDateFormat
import java.util.*

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
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_common
    }


    override fun initView(savedInstanceState: Bundle?) {
        initRefresh()
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
                    exitRefresh.finishRefresh(false)
                }

            })
        } else {
            ApiRepository.getInstance().requestSpaceSettleList(plantNum).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseObserver<BaseResult<List<ParkSpaceInfo>>>() {
                override fun onRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
                    handleRequestSuccess(entity)
                }

                override fun onRequestError(throwable: Throwable?) {
                    super.onRequestError(throwable)
                    exitRefresh.finishRefresh(false)
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
        requestSpaceSettleList("", needShowLoading)
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
                requestPayCertificate(recordId)
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
    private fun requestPayCertificate(recordId: Long?) {
        ApiRepository.getInstance().requestPayCertificate(recordId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<PayCertificate>>() {
            override fun onRequestSuccess(entity: BaseResult<PayCertificate>?) {
                handleCertificateCallback(entity)
            }
        })
//        printContent(AccountHelper.getInstance().userInfo,)
    }

    private fun printContent(userInfo: UserInfo, certificate: PayCertificate) {
        try {
            //设置纸张大小为两英寸（5.08cm）
            iPrinter?.setPaperSize(0x00)
            //设置间距 0-60 默认为6
            iPrinter?.setPaperSize(15)
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
            format.putInt("height", 200)
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
                    ToastUtil.showSuccess("打印完成")
                }

                @Throws(RemoteException::class)
                override fun onError(arg0: Int, arg1: String) {
                    ToastUtil.showFailedDebug("打印出错:$arg1")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showFailedDebug("打印故障:$e")
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
        val intent = Intent()
        intent.putExtra(SettleFeeDetailActivity.EXTRA_SETTLE_RECORD_ID, recordId)
        intent.putExtra(SettleFeeDetailActivity.EXTRA_SETTLE_RECORD_ID, parkSpaceInfo.getRecordId())
        intent.putExtra(SettleFeeDetailActivity.EXTRA_PARK_ID, parkId)
        ,
        intent.setClass(mContext, SettleFeeDetailActivity::class.java)
        startActivity(intent)
    }
}