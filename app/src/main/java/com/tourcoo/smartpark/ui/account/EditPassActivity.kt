package com.tourcoo.smartpark.ui.account

import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseObserver
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.util.SignTool
import com.trello.rxlifecycle3.android.ActivityEvent
import kotlinx.android.synthetic.main.activity_edit_pass.*

/**
 *@description :
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月05日10:55
 * @Email: 971613168@qq.com
 */
class EditPassActivity : BaseTitleActivity() {
    companion object {
        const val VISIBLE_STATUS = R.id.tag_pass_visible
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_edit_pass
    }

    override fun initView(savedInstanceState: Bundle?) {
        tvConfirm.setOnClickListener {
//            etNewPass.setText(SignTool.getSignatureMD5(mContext, etOldPass.text.toString()))
            doEditPass()
        }
        listenPassVisible(etOldPass, ivEyeOldPass)
        listenPassVisible(etNewPass, ivEyeNewPass)
        listenPassVisible(etRePass, ivEyeRePass)
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("修改密码")
    }

    private fun doEditPass() {
        if (TextUtils.isEmpty(etOldPass.text.toString())) {
            ToastUtil.showWarning("请输入原密码")
            return
        }
        if (TextUtils.isEmpty(etNewPass.text.toString())) {
            ToastUtil.showWarning("请输入新密码")
            return
        }
        if (TextUtils.isEmpty(etRePass.text.toString())) {
            ToastUtil.showWarning("请再次输入新密码")
            return
        }
        if (etRePass.text.toString() != etNewPass.text.toString()) {
            ToastUtil.showWarning("两次密码输入不一致")
            return
        }
        requestResetNewPass(etOldPass.text.toString(), etNewPass.text.toString(), etRePass.text.toString())
    }


    private fun requestResetNewPass(oldPass: String, newPass: String, rePass: String) {
        ApiRepository.getInstance().requestEditPass(oldPass, newPass, rePass).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseObserver<BaseResult<Any?>?>() {
            override fun onRequestSuccess(entity: BaseResult<Any?>?) {
                if (entity?.code == RequestConfig.REQUEST_CODE_SUCCESS) {
                    ToastUtil.showSuccess(entity.errMsg)
                    Handler().postDelayed(Runnable { finish() }, 300)
                } else {
                    ToastUtil.showNormal(entity?.errMsg)
                }
            }
        })
    }

    private fun listenPassVisible(editText: EditText, imageView: ImageView) {
        //默认不可见
        imageView.setTag(VISIBLE_STATUS, false)
        imageView.setImageResource(R.mipmap.ic_eye_blue_open)
        editText.transformationMethod = PasswordTransformationMethod.getInstance()
        imageView.setOnClickListener {
            if (imageView.getTag(VISIBLE_STATUS) != null) {
                val isVisible = imageView.getTag(VISIBLE_STATUS) as Boolean
                if (isVisible) {
                    imageView.setImageResource(R.mipmap.ic_eye_blue_open)
                    editText.transformationMethod = PasswordTransformationMethod.getInstance()
                    imageView.setTag(VISIBLE_STATUS, !isVisible)
                } else {
                    imageView.setImageResource(R.mipmap.ic_eye_blue_closed)
                    editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    imageView.setTag(VISIBLE_STATUS, !isVisible)

                }
                editText.setSelection(editText.text.toString().length)
            }
        }
    }
}