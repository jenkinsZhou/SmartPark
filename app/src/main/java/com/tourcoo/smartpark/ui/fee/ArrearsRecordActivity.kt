package com.tourcoo.smartpark.ui.fee

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.ArrearsRecordHistoryAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.ArrearsRecord
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.ExitPayFeeDetailActivity.Companion.EXTRA_ARREARS_IDS
import com.tourcoo.smartpark.util.DoubleUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_arrears_record.*
import java.util.*

/**
 *@description : 欠费记录页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月07日10:43
 * @Email: 971613168@qq.com
 */
class ArrearsRecordActivity : BaseTitleActivity(), View.OnClickListener {
    private var carId: Long? = null
    private var selectAll = false
    private var historyAdapter: ArrearsRecordHistoryAdapter? = null
    private val arrearsIdArray = ArrayList<Long>()
    private val arrearsIds: String? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_arrears_record
    }

    override fun initView(savedInstanceState: Bundle?) {
        carId = intent?.getLongExtra(ExitPayFeeDetailActivity.EXTRA_CAR_ID, -1)
        if (carId!! < 0) {
            finish()
            ToastUtil.showWarning("未获取到车辆信息")
        }
        ivSelectAll.setOnClickListener(this)
        tvConfirm.setOnClickListener(this)
        tvSelectCount.setOnClickListener(this)
        initRecyclerView()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("欠费记录")
    }

    override fun loadData() {
        super.loadData()
        requestArrearsList()
        showBottomLayoutInfo(0)
    }

    private fun initRecyclerView() {
        rvArrears.layoutManager = LinearLayoutManager(mContext)
        historyAdapter = ArrearsRecordHistoryAdapter()
        historyAdapter!!.bindToRecyclerView(rvArrears)
        historyAdapter!!.setOnItemClickListener { adapter, view, position ->
            doSelect(position)
        }
    }


    private fun requestArrearsList() {
        ApiRepository.getInstance().requestArrearsList(carId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<List<ArrearsRecord>>>() {
            override fun onRequestSuccess(entity: BaseResult<List<ArrearsRecord>>?) {
                handleRequestSuccess(entity)
            }
        })
    }


    private fun handleRequestSuccess(entity: BaseResult<List<ArrearsRecord>>?) {
        if (entity == null) {
            return
        }
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            loadRecord(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }

    private fun loadRecord(data: List<ArrearsRecord>?) {
        if (historyAdapter?.emptyView == null) {
            val emptyView = LayoutInflater.from(mContext).inflate(R.layout.status_layout_car_data_empty, null)
            historyAdapter?.emptyView = emptyView
        }
        historyAdapter?.setNewData(data)
        showBottomLayoutInfo(0)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSelectAll, R.id.tvSelectCount -> {
                selectAll = if (selectAll) {
                    doSelectAll(selectAll)
                    false
                } else {
                    doSelectAll(selectAll)
                    true
                }
            }
            R.id.tvConfirm -> {
                doConfirmSelect()
            }
            else -> {
            }
        }
    }

    private fun doSelect(position: Int) {
        var currentInfo: ArrearsRecord?
        var selectCount = 0
        for (i in 0 until historyAdapter!!.data.size) {
            currentInfo = historyAdapter!!.data[i] as ArrearsRecord
            if (position == i) {
                currentInfo.isSelect = !currentInfo.isSelect
            }
            if (currentInfo.isSelect) {
                selectCount++
            }
        }
        historyAdapter!!.notifyDataSetChanged()
        showBottomLayoutInfo(selectCount)
    }

    private fun doSelectAll(select: Boolean) {
        var currentInfo: ArrearsRecord?
        for (i in 0 until historyAdapter!!.data.size) {
            currentInfo = historyAdapter!!.data[i] as ArrearsRecord
            currentInfo.isSelect = select
        }
        historyAdapter!!.notifyDataSetChanged()
        if (select) {
            showBottomLayoutInfo(historyAdapter!!.data.size)
        } else {
            showBottomLayoutInfo(0)
        }
    }

    private fun showBottomLayoutInfo(selectCount: Int) {
        var fee = 0.00
        for (arrearsRecord in historyAdapter!!.data) {
            if (arrearsRecord.isSelect) {
                fee = DoubleUtil.sum(fee, arrearsRecord.fee)
            }
        }
        val feeTotal = "¥ $fee"
        tvParkingFeeTotal.text = feeTotal
        val selectCountInfo = "全选 已选" + selectCount + "笔"
        tvSelectCount.text = selectCountInfo
        showSelectState()
    }

    private fun showSelectState() {
        val dataSize = historyAdapter!!.data.size
        if (getSelectCount() == dataSize && dataSize > 0) {
            ivSelectAll.setImageResource(R.mipmap.ic_checked_blue_small)
        } else {
            ivSelectAll.setImageResource(R.mipmap.ic_checked_gray)
        }
    }

    private fun getSelectCount(): Int {
        var count = 0
        for (datum in historyAdapter!!.data) {
            if (datum.isSelect) {
                count++
            }
        }
        return count
    }

    private fun doConfirmSelect() {
        arrearsIdArray.clear()
        for (arrearsRecord in historyAdapter!!.data) {
            if (arrearsRecord.isSelect) {
                arrearsIdArray.add(arrearsRecord.id)
            }
        }
        val intent = Intent()
        intent.putExtra(EXTRA_ARREARS_IDS, arrearsIdArray)
        setResult(RESULT_OK, intent)
        finish()
    }
}