package com.tourcoo.smartpark.ui.fee

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.ArrearsRecordHistoryAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.ArrearsHistoryRecord
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity.Companion.EXTRA_ARREARS_IDS
import com.tourcoo.smartpark.util.DoubleUtil
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_arrears_record.*
import java.util.*

/**
 *@description : 欠费记录选择页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月07日10:43
 * @Email: 971613168@qq.com
 */
class PayArrearsRecordActivity : BaseTitleActivity(), View.OnClickListener {
    private var carId: Long? = null
    private var selectAll = false
    private var historyAdapter: ArrearsRecordHistoryAdapter? = null
    private val arrearsIdArray = ArrayList<Long>()
    private var receiveIdArray: ArrayList<Long>? = null
    private val arrearsIds: String? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_arrears_record
    }

    override fun initView(savedInstanceState: Bundle?) {
        carId = intent?.getLongExtra(SettleFeeDetailActivity.EXTRA_CAR_ID, -1)
        if (intent?.getSerializableExtra(EXTRA_ARREARS_IDS) != null) {
            receiveIdArray = intent?.getSerializableExtra(EXTRA_ARREARS_IDS) as ArrayList<Long>
        }

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
        requestArrearsHistoryList()
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


    private fun requestArrearsHistoryList() {
        ApiRepository.getInstance().requestArrearsHistoryList(carId!!).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<List<ArrearsHistoryRecord>>>() {
            override fun onRequestSuccess(entity: BaseResult<List<ArrearsHistoryRecord>>?) {
                handleRequestSuccess(entity)
            }
        })
    }


    private fun handleRequestSuccess(entity: BaseResult<List<ArrearsHistoryRecord>>?) {
        if (entity == null) {
            return
        }
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            loadRecord(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }

    private fun loadRecord(data: List<ArrearsHistoryRecord>?) {
        if (historyAdapter?.emptyView == null) {
            val emptyView = LayoutInflater.from(mContext).inflate(R.layout.multi_status_layout_empty, null)
            historyAdapter?.emptyView = emptyView
            emptyView.setOnClickListener {
                requestArrearsHistoryList()
            }
        }
        var selectCount = 0
        if (receiveIdArray != null && receiveIdArray!!.isNotEmpty()) {
            for (index in 0 until receiveIdArray!!.size) {
                data!!.forEach {
                    if (receiveIdArray!![index] == it.id) {
                        it.isSelect = true
                        selectCount++
                    }
                }
            }
        }
        historyAdapter?.setNewData(data)
        showBottomLayoutInfo(selectCount)
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
        var currentInfo: ArrearsHistoryRecord?
        var selectCount = 0
        for (i in 0 until historyAdapter!!.data.size) {
            currentInfo = historyAdapter!!.data[i] as ArrearsHistoryRecord
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
        var currentInfo: ArrearsHistoryRecord?
        for (i in 0 until historyAdapter!!.data.size) {
            currentInfo = historyAdapter!!.data[i] as ArrearsHistoryRecord
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