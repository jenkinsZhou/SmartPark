package com.tourcoo.smartpark.ui.account.login

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.ImageView
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.account.ParkingInfo
import com.tourcoo.smartpark.bean.account.TokenInfo
import com.tourcoo.smartpark.bean.account.UserInfo
import com.tourcoo.smartpark.core.CommonUtil
import com.tourcoo.smartpark.core.base.mvp.BaseMvpTitleActivity
import com.tourcoo.smartpark.core.utils.SizeUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.HomeActivity
import com.tourcoo.smartpark.ui.account.AccountHelper
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.searchview.BSearchEdit
import kotlinx.android.synthetic.main.activity_login.*


/**
 *@description : 登录注册
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月24日9:41
 * @Email: 971613168@qq.com
 */
class LoginActivity : BaseMvpTitleActivity<LoginPresenter>(), LoginContract.LoginView, View.OnClickListener {
    private var bSearchEdit: BSearchEdit? = null
    private var currentSelectPosition = -1

    private val parkingList = ArrayList<ParkingInfo>()
    override fun getContentLayout(): Int {
        return R.layout.activity_login
    }

    override fun initView(savedInstanceState: Bundle?) {
        llSelectParking.setOnClickListener(this)
        etCurrentParking.setOnClickListener(this)
        tvLogin.setOnClickListener(this)
        etCurrentParking.inputType = InputType.TYPE_NULL
        llSelectParking.post {
            initSearchView(llSelectParking.width.toFloat())
        }
        listenInput(etUserName, ivClearUser)

        listenInput(etPass, ivClearPass)
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.visibility = View.GONE
    }

    override fun loadPresenter() {
    }

    override fun createPresenter(): LoginPresenter {
        return LoginPresenter()
    }

    override fun showParkingList(parkingInfoList: MutableList<ParkingInfo>?) {
        if (parkingInfoList == null) {
            return
        }
        parkingList.clear()
        val parkingStrList = ArrayList<String>()
        parkingInfoList.forEach {
            parkingStrList.add(StringUtil.getNotNullValue(it.name))
            parkingList.add(it)
        }
        bSearchEdit!!.setSearchList(parkingStrList)
        bSearchEdit!!.showPopup()
        if (parkingInfoList.isEmpty()) {
            ToastUtil.showNormal("当前账号不存在或没有绑定停车场")
        }
    }

    /**
     * 登录成功
     */
    override fun loginSuccess(tokenInfo: TokenInfo?) {
        if (tokenInfo == null) {
            return
        }
        AccountHelper.getInstance().accessToken = tokenInfo.token
        AccountHelper.getInstance().isNeedResetPass = tokenInfo.isNeedUpdatePassword
        presenter.requestUserInfo()
    }

    override fun loginFailed() {
        closeLoadingDialog()
    }

    override fun showUserInfo(userInfo: UserInfo?) {
        closeLoadingDialog()
        if (userInfo == null) {
            return
        }
        AccountHelper.getInstance().userInfo = userInfo
        AccountHelper.getInstance().isNeedResetPass = userInfo.isNeedResetPass
        skipHome()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llSelectParking, R.id.etCurrentParking -> {
                getParkingList()
            }
            R.id.tvLogin -> {
                doLogin()
            }
            else -> {
            }
        }
    }

    private fun initSearchView(widthPx: Float) {
        //第三个必须要设置窗体的宽度，单位dp
        bSearchEdit = BSearchEdit(this, llSelectParking, SizeUtil.px2dp(widthPx))
        bSearchEdit!!.setTimely(false)
        bSearchEdit!!.build()
        bSearchEdit!!.setTextClickListener { position, text ->
            currentSelectPosition = position
            etCurrentParking.setText(text!!)
        }
    }

    private fun getParkingList() {
        if (TextUtils.isEmpty(etUserName.text.toString())) {
            ToastUtil.showNormal("请先输入用户名")
            return
        }
        presenter.getParkingList(etUserName.text.toString())
    }

    private fun doLogin() {
        if (TextUtils.isEmpty(etUserName.text.toString())) {
            ToastUtil.showNormal("请输入用户名")
            return
        }
        if (TextUtils.isEmpty(etCurrentParking.text.toString())) {
            ToastUtil.showNormal("请选择停车场")
            return
        }
        if (TextUtils.isEmpty(etPass.text.toString())) {
            ToastUtil.showNormal("请输入密码")
            return
        }
        if (parkingList.size <= currentSelectPosition) {
            ToastUtil.showNormal("未获取到停车场信息")
            return
        }
        showLoadingDialog("正在登录...")
        presenter.requestLogin(etUserName.text.toString(), etPass.text.toString(), parkingList[currentSelectPosition].id)
    }


    private fun skipHome() {
        CommonUtil.startActivity(mContext, HomeActivity::class.java)
        finish()
    }

    private fun listenInput(editText: EditText, imageView: ImageView) {
        imageView.setOnClickListener {
            editText.setText("")
            if (imageView.id == ivClearUser.id) {
                etCurrentParking.setText("")
            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                setViewVisible(imageView, s != null && s.toString().isNotEmpty())
            }

        })
        editText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // 此处为得到焦点时的处理内容
                bSearchEdit?.dismiss()
            }
        }
    }
}