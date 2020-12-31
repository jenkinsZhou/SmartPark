package com.tourcoo.smartpark.ui.message

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kingja.loadsir.callback.Callback.OnReloadListener
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.message.MessageAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.fee.ArrearsHistoryRecord
import com.tourcoo.smartpark.bean.message.MessageInfo
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.multi_status.MultiStatusErrorCallback
import com.tourcoo.smartpark.core.multi_status.MultiStatusLoadingCallback
import com.tourcoo.smartpark.core.retrofit.BaseObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.widget.dialog.IosAlertDialog
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_message_record.*

/**
 *@description : 消息列表
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月30日16:41
 * @Email: 971613168@qq.com
 */
class MessageListActivity : BaseTitleActivity(), View.OnClickListener, OnReloadListener {
    private var messageAdapter: MessageAdapter? = null
    private var loadService: LoadService<*>? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_message_record
    }

    override fun initView(savedInstanceState: Bundle?) {
        initRecyclerView()
        initStatusManager()
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
                    showFeeEnd()
                }
                R.id.tvFeeCalculateContinue -> {
                    showFeeContinue()
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
        loadService?.showCallback(MultiStatusLoadingCallback::class.java)
        ApiRepository.getInstance().requestMessageList().compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseObserver<BaseResult<List<MessageInfo>>>() {
            override fun onRequestSuccess(entity: BaseResult<List<MessageInfo>>?) {
                loadService?.showSuccess()
                handleRequestSuccess(entity)
            }

            override fun onRequestError(throwable: Throwable?) {
                super.onRequestError(throwable)
                loadService?.showCallback(MultiStatusErrorCallback::class.java)
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
        showTitleInfoByCondition(data)
    }


    override fun loadData() {
        super.loadData()
        requestMessageList()
    }

    private fun showTitleInfoByCondition(data: List<MessageInfo>?) {
        val rightLayout = mTitleBar?.getLinearLayout(Gravity.END) ?: return
        val rightTextView = View.inflate(mContext, R.layout.view_right_title, null) as TextView
        rightLayout.removeAllViews()
        if (data != null && data.isNotEmpty()) {
            rightLayout.addView(rightTextView)
            rightTextView.text = "一键结束"
            rightTextView.setOnClickListener {
//                showSignConfirm()
                ToastUtil.showNormal("一键结束")
            }
            setViewGone(rightTextView, true)
        } else {
            setViewGone(rightTextView, false)
        }
    }

    private fun initStatusManager() {
        //这里实例化多状态管理类
        loadService = LoadSir.getDefault().register(rvMessage, this)
    }

    override fun onReload(v: View?) {
        requestMessageList()
    }


    /**
     * 继续计费
     */
    private fun showFeeContinue() {
        IosAlertDialog(mContext)
                .init()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setTitle("继续计费")
                .setMsg("确定要继续计费吗?")
                .setPositiveButton("确定", View.OnClickListener {
//                    requestFlagArrears(recordId)
                    ToastUtil.showSuccess("执行计费")
                })
                .setNegativeButton("取消", View.OnClickListener {
                }).show()
    }


    private fun showFeeEnd() {
        IosAlertDialog(mContext)
                .init()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setTitle("确认结束")
                .setMsg("确定结束当前计费?")
                .setPositiveButton("确定", View.OnClickListener {
//                    requestFlagArrears(recordId)
                    ToastUtil.showSuccess("执行结束")
                })
                .setNegativeButton("取消", View.OnClickListener {
                }).show()
    }
}