package com.tourcoo.smartpark.ui.report

import android.os.Bundle
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.FeeRecordAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.PageBean
import com.tourcoo.smartpark.bean.fee.DailyFeeRecord
import com.tourcoo.smartpark.core.UiManager
import com.tourcoo.smartpark.core.base.activity.BaseRefreshLoadActivity
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.trello.rxlifecycle3.android.ActivityEvent
import java.util.*

/**
 *@description : 收费记录列表页面
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月09日13:52
 * @Email: 971613168@qq.com
 */
class DailyFeeRecordListActivity : BaseRefreshLoadActivity<DailyFeeRecord>() {
    private var adapter: FeeRecordAdapter? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_common
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("收费记录")
    }



    override fun getAdapter(): BaseQuickAdapter<DailyFeeRecord, BaseViewHolder> {
        adapter = FeeRecordAdapter()
        return adapter!!
    }

    override fun loadPageData(page: Int) {
        requestDailyRecordList(page)
    }

    private fun requestDailyRecordList(page: Int) {
        ApiRepository.getInstance().requestDailyRecordList(page).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<PageBean<DailyFeeRecord>>>(iHttpRequestControl) {
            override fun onRequestSuccess(entity: BaseResult<PageBean<DailyFeeRecord>>?) {
                UiManager.getInstance().httpRequestControl.httpRequestSuccess(iHttpRequestControl, if (entity == null || entity.data == null || entity.data.list == null) ArrayList<DailyFeeRecord?>() else entity.data.list!!, null)
            }
        })
    }



}