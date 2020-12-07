package com.tourcoo.smartpark.ui.fee

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.adapter.home.GridParkAdapter
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.ParkSpaceInfo
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.BaseObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.SizeUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.ExitPayFeeDetailActivity.Companion.EXTRA_SETTLE_RECORD_ID
import com.tourcoo.smartpark.util.GridDividerItemDecoration
import com.tourcoo.smartpark.widget.keyboard.PlateKeyboardView
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_exit_pay_fee_enter.*
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit


/**
 *@description : 离场收费入口
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月03日10:13
 * @Email: 971613168@qq.com
 */
class ExitPayFeeEnterActivity : BaseTitleActivity(), OnRefreshListener {
    private var gridParkAdapter: GridParkAdapter? = null
    private var keyboardView: PlateKeyboardView? = null
    override fun getContentLayout(): Int {
        return R.layout.activity_exit_pay_fee_enter
    }

    companion object {
        const val DELAY_TIME = 400L
    }

    override fun initView(savedInstanceState: Bundle?) {
        ivSearchSmall.setOnClickListener(View.OnClickListener {
            getExitSpaceList(true)
        })
        initRefresh()
        initSearchInput()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("离场收费")
    }

    override fun loadData() {
        super.loadData()
        getExitSpaceList(true)
    }

    private fun initRefresh() {
        exitRefresh.setEnableLoadMore(false)
        exitRefresh.setOnRefreshListener(this)
        exitRefresh.setRefreshHeader(ClassicsHeader(mContext))
        exitRecyclerView.layoutManager = GridLayoutManager(mContext, 3)
        exitRecyclerView.addItemDecoration(GridDividerItemDecoration(SizeUtil.dp2px(7f), ContextCompat.getColor(this, R.color.whiteF5F5F5), false))
        gridParkAdapter = GridParkAdapter()
        gridParkAdapter!!.bindToRecyclerView(exitRecyclerView)
        gridParkAdapter!!.setOnItemClickListener { adapter, view, position ->
            val parkSpaceInfo = gridParkAdapter!!.data[position]
            skipSettle(parkSpaceInfo.recordId,parkSpaceInfo.id)
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
        if (gridParkAdapter!!.emptyView == null) {
            val emptyView = LayoutInflater.from(mContext).inflate(R.layout.status_layout_car_data_empty, null)
            gridParkAdapter!!.emptyView = emptyView
        }
        gridParkAdapter!!.setNewData(parkSpaceInfoList)
    }

    private fun getExitSpaceList(needShowLoading: Boolean) {
        requestSpaceSettleList(etPlantNum.text.toString(), needShowLoading)
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
            keyboardView?.showKeyboard(etPlantNum,InputType.TYPE_CLASS_PHONE)
        })
        keyboardView?.setOnKeyboardFinishListener(object : PlateKeyboardView.OnKeyboardFinishListener {
            override fun onFinish(input: String?) {
//                getExitSpaceList(true)
            }
        }, "关闭")
        listenInput(etPlantNum, ivDeleteSmall)
    }

    private fun handleRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
        if (entity == null) {
            exitRefresh.finishRefresh(false)
            return
        }
        exitRefresh.finishRefresh(true)
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
            loadSpaceData(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }


    @SuppressLint("CheckResult")
    private fun listenInput(editText: EditText, imageView: ImageView) {
        setViewVisible(imageView, !TextUtils.isEmpty(editText.text.toString()))
        imageView.setOnClickListener { v: View? -> editText.setText("") }
        RxTextView.textChanges(editText)
                .debounce(DELAY_TIME, TimeUnit.MILLISECONDS)
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


    private fun skipSettle(recordId: Long,parkId: Long) {
        val intent = Intent()
        intent.putExtra(EXTRA_SETTLE_RECORD_ID, recordId)
        intent.putExtra(ExitPayFeeDetailActivity.EXTRA_PARK_ID, parkId)
        intent.setClass(mContext, ExitPayFeeDetailActivity::class.java)
        startActivity(intent)
    }
}