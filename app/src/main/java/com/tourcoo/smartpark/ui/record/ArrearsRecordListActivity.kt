package com.tourcoo.smartpark.ui.record

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.ArrearsRecordAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.PageBean
import com.tourcoo.smartpark.bean.fee.ArrearsRecord
import com.tourcoo.smartpark.constant.ParkConstant.EXTRA_RECORD_ID
import com.tourcoo.smartpark.core.UiManager
import com.tourcoo.smartpark.core.base.activity.BaseRefreshLoadActivity
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.ArrearsDetailActivity
import com.tourcoo.smartpark.ui.fee.FeeDetailActivity
import com.tourcoo.smartpark.ui.fee.SettleFeeDetailActivity
import com.trello.rxlifecycle3.android.ActivityEvent
import java.util.ArrayList

/**
 *@description : 欠费记录
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月10日14:04
 * @Email: 971613168@qq.com
 */
class ArrearsRecordListActivity : BaseRefreshLoadActivity<ArrearsRecord>() {

    private var adapter: ArrearsRecordAdapter? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_common
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("欠费记录")
    }


    override fun getAdapter(): BaseQuickAdapter<ArrearsRecord, BaseViewHolder> {
        adapter = ArrearsRecordAdapter()
        return adapter!!
    }

    override fun loadPageData(page: Int) {
        requestArrearsRecord(page)
    }

    private fun requestArrearsRecord(page: Int) {
        ApiRepository.getInstance().requestArrearsRecordList(page).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<PageBean<ArrearsRecord>>>(iHttpRequestControl) {
            override fun onRequestSuccess(entity: BaseResult<PageBean<ArrearsRecord>>?) {
                UiManager.getInstance().httpRequestControl.httpRequestSuccess(iHttpRequestControl, if (entity == null || entity.data == null || entity.data.list == null) ArrayList() else entity.data.list!!, null)
            }
        })
    }

    override fun onItemClicked(adapter: BaseQuickAdapter<ArrearsRecord, BaseViewHolder>?, view: View?, position: Int) {
        super.onItemClicked(adapter, view, position)
        val item = adapter!!.data[position] as ArrearsRecord
        skipArrearsDetail(item.id)
    }

    private fun skipArrearsDetail(recordId: Long) {
        val intent = Intent()
        intent.putExtra(EXTRA_RECORD_ID, recordId)
        intent.setClass(mContext, ArrearsDetailActivity::class.java)
        startActivity(intent)
    }
}