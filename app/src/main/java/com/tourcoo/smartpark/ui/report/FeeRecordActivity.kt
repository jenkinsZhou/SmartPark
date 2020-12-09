package com.tourcoo.smartpark.ui.report

import android.os.Bundle
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.fee.FeeRecordAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.PageBean
import com.tourcoo.smartpark.bean.fee.FeeRecord
import com.tourcoo.smartpark.core.UiManager
import com.tourcoo.smartpark.core.base.activity.BaseRefreshLoadActivity
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.trello.rxlifecycle3.android.ActivityEvent
import java.util.*

/**
 *@description : JenkinsZhou
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月09日13:52
 * @Email: 971613168@qq.com
 */
class FeeRecordActivity : BaseRefreshLoadActivity<FeeRecord>() {
    private var adapter: FeeRecordAdapter? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_report_fee_list
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("收费记录")
    }

    override fun onReload(v: View?) {
        ToastUtil.showWarning("点击了重试")
    }

    override fun getAdapter(): BaseQuickAdapter<FeeRecord, BaseViewHolder> {
        adapter = FeeRecordAdapter()
        return adapter!!
    }

    override fun loadPageData(page: Int) {
        requestDailyRecordList(page)
    }

    private fun requestDailyRecordList(page: Int) {
        ApiRepository.getInstance().requestDailyRecordList(page).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<PageBean<FeeRecord>>>() {
            override fun onRequestSuccess(entity: BaseResult<PageBean<FeeRecord>>?) {
                UiManager.getInstance().httpRequestControl.httpRequestSuccess(iHttpRequestControl, if (entity == null || entity.data == null || entity.data.list == null) ArrayList<FeeRecord?>() else entity.data.list!!, null)
            }
        })
    }

}