package com.tourcoo.smartpark.ui.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.message.MessageAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.ArrearsHistoryRecord
import com.tourcoo.smartpark.bean.message.MessageInfo
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_arrears_record.rvArrears
import kotlinx.android.synthetic.main.activity_message_record.*

/**
 *@description : 消息列表
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月30日16:41
 * @Email: 971613168@qq.com
 */
class MessageListActivity : BaseTitleActivity(), View.OnClickListener {
    private var messageAdapter: MessageAdapter? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_message_record
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerView()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("通知消息")
    }

    override fun onClick(v: View?) {
    }


    private fun initRecyclerView() {
        rvMessage.layoutManager = LinearLayoutManager(mContext)
        messageAdapter = MessageAdapter()
        messageAdapter!!.bindToRecyclerView(rvMessage)
        messageAdapter!!.setOnItemClickListener { adapter, view, position ->
            doSelect(position)
        }
        messageAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view?.id) {
                R.id.tvFeeCalculateEnd -> {

                }
                R.id.tvFeeCalculateContinue -> {

                }
                else -> {
                }
            }
        }
    }

    private fun doSelect(position: Int) {
        var currentInfo: MessageInfo?
        var selectCount = 0
        for (i in 0 until messageAdapter!!.data.size) {
            currentInfo = messageAdapter!!.data[i] as MessageInfo
            if (position == i) {
                currentInfo.isSelect = !currentInfo.isSelect
            }
            if (currentInfo.isSelect) {
                selectCount++
            }
        }
        messageAdapter!!.notifyDataSetChanged()
    }

    private fun doSelectAll(select: Boolean) {
        var currentInfo: ArrearsHistoryRecord?
        for (i in 0 until messageAdapter!!.data.size) {
            currentInfo = messageAdapter!!.data[i] as ArrearsHistoryRecord
            currentInfo.isSelect = select
        }
        messageAdapter!!.notifyDataSetChanged()
    }

    private fun requestMessageList() {
        ApiRepository.getInstance().requestMessageList().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<List<MessageInfo>>>() {
            override fun onRequestSuccess(entity: BaseResult<List<MessageInfo>>?) {
                handleRequestSuccess(entity)
            }
        })
    }


    private fun handleRequestSuccess(entity: BaseResult<List<MessageInfo>>?) {
        if (entity == null) {
            return
        }
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
            loadRecord(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }


    private fun loadRecord(data: List<MessageInfo>?) {
        if (messageAdapter?.emptyView == null) {
            val emptyView = LayoutInflater.from(mContext).inflate(R.layout.multi_status_layout_empty, null)
            messageAdapter?.emptyView = emptyView
            emptyView.setOnClickListener {
                requestMessageList()
            }
        }
        /*  var selectCount = 0
          if (receiveIdArray != null && receiveIdArray!!.isNotEmpty()) {
              for (index in 0 until receiveIdArray!!.size) {
                  data!!.forEach {
                      if (receiveIdArray!![index] == it.id) {
                          it.isSelect = true
                          selectCount++
                      }
                  }
              }
          }*/
        messageAdapter?.setNewData(data)
    }


    override fun loadData() {
        super.loadData()
        requestMessageList()

    }
}