package com.tourcoo.smartpark.ui.record

import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import com.apkfuns.logutils.LogUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.retrofit.UploadProgressBody
import com.tourcoo.smartpark.core.retrofit.UploadRequestListener
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.widget.keyboard.InputCompleteListener
import com.tourcoo.smartpark.widget.keyboard.KeyboardUtils
import com.tourcoo.smartpark.widget.keyboard.KingKeyboard
import com.tourcoo.smartpark.widget.selecter.GlideEngine
import com.tourcoo.smartpark.widget.selecter.PhotoAdapter
import kotlinx.android.synthetic.main.activity_record_confirm.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


/**
 *@description : 确认登记
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月13日11:09
 * @Email: 971613168@qq.com
 */
class RecordCarInfoConfirmActivity : BaseTitleActivity(), View.OnClickListener {

    private var mEditTexts: MutableList<EditText>? = null
    private var photoAdapter: PhotoAdapter? = null
    private lateinit var kingKeyboard: KingKeyboard
    private var mOnCompleteListener: InputCompleteListener? = null
    private val mSelectImagePathList = ArrayList<String>()
    override fun getContentLayout(): Int {
        return R.layout.activity_record_confirm
    }

    override fun initView(savedInstanceState: Bundle?) {
        llContentView.setOnClickListener(this)
        llTakePhoto.setOnClickListener(this)
        tvConfirm.setOnClickListener(this)
        initKeyboard()
        initPhotoAdapter()
        takePhoto.text = "车辆拍照"
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("确认登记")
    }

    private fun initKeyboard() {
        mEditTexts = ArrayList()
        kingKeyboard = KingKeyboard(this, keyboardParent)
        //然后将EditText注册到KingKeyboard即可
        kingKeyboard.register(etPlantName, KingKeyboard.KeyboardType.LICENSE_PLATE)
        kingKeyboard.register(etPlantLetter, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.register(etPlantNumber1, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.register(etPlantNumber2, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.register(etPlantNumber3, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.register(etPlantNumber4, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.register(etPlantNumber5, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.register(etPlantNumber6, KingKeyboard.KeyboardType.LICENSE_PLATE_MODE_CHANGE)
        kingKeyboard.setKeyboardCustom(R.xml.keyboard_custom)
        setupEditText(etPlantName)
        setupEditText(etPlantLetter)
        setupEditText(etPlantNumber1)
        setupEditText(etPlantNumber2)
        setupEditText(etPlantNumber3)
        setupEditText(etPlantNumber4)
        setupEditText(etPlantNumber5)
        etPlantNumber1.requestFocus()
        //设置"用户名"提示文字的大小
        val s = SpannableString("新能源")
        val textSize = AbsoluteSizeSpan(9, true)
        s.setSpan(textSize, 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        etPlantNumber6.hint = s
        kingKeyboard.setVibrationEffectEnabled(true)
    }

    private fun setupEditText(editText: EditText) {
        mEditTexts!!.add(editText)
        editText.addTextChangedListener(InnerTextWatcher(editText))
    }

    private inner class InnerTextWatcher(var innerEditText: EditText) : TextWatcher {
        var maxLength: Int = KeyboardUtils.getMaxLength(innerEditText)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            val count = s.length
            if (maxLength == 0) {
                return
            }
            if (count >= maxLength) {
                focusNext(innerEditText)
            } else if (count == 0) {
                focusLast(innerEditText)
            }
        }

    }


    fun focusNext(et: EditText?) {
        val index = mEditTexts!!.indexOf(et)
        if (index < mEditTexts!!.size - 1) {
            val nextEt = mEditTexts!![index + 1]
            nextEt.requestFocus()
            nextEt.setSelection(nextEt.text.toString().length)
        } else {
            if (mOnCompleteListener != null) {
                val editable = Editable.Factory.getInstance().newEditable("")
                for (editText in mEditTexts!!) {
                    editable.append(editText.text)
                }
                mOnCompleteListener!!.onComplete(editable, editable.toString())
            }
            et!!.setSelection(et.text.toString().length)
        }
    }


    private fun focusLast(et: EditText?) {
        val index = mEditTexts!!.indexOf(et)
        if (index != 0) {
            val lastEt = mEditTexts!![index - 1]
            lastEt.requestFocus()
            lastEt.setSelection(lastEt.text.toString().length)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.llContentView -> {
                kingKeyboard.hideKeyboard()
            }
            R.id.llTakePhoto -> {
                selectPicture()
            }
            else -> {
            }
        }
    }


    private fun selectPicture() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .imageEngine(GlideEngine.createGlideEngine())
                .forResult(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: List<LocalMedia?>) {
                        // 结果回调
                        photoAdapter?.setNewData(result)
                        mSelectImagePathList.clear()
                        for (localMedia in result) {
                            if (localMedia != null) {
                                mSelectImagePathList.add(localMedia.realPath)
                            }
                        }


                    }

                    override fun onCancel() {
                        // 取消
                    }
                })
    }

    private fun initPhotoAdapter() {
        photoAdapter = PhotoAdapter()
        photoRecyclerView.layoutManager = GridLayoutManager(mContext, 3)
        photoAdapter?.bindToRecyclerView(photoRecyclerView)
        photoAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.ivLocalPhoto -> {
                    ToastUtil.showSuccess("查看图片")
                }
                R.id.ivDelete -> {
                    ToastUtil.showNormal("删除图片")
                }
                R.id.tvConfirm -> {
                    test(mSelectImagePathList)
                }

                else -> {
                }
            }
        }
    }


    private fun test(imageList: ArrayList<String>) {
        var file: File
        val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
        //注意，file是后台约定的参数，如果是多图，files，如果是单张图片，file就行
        for (imagePath in imageList) {
            //这里上传的是多图
            LogUtils.i("路径："+imagePath)
            file = File(imagePath)
            builder.addFormDataPart("files[]", file.name, RequestBody.create(MediaType.parse("image/*"), file))
        }
        val requestBody: RequestBody = builder.build()
        val uploadProgressBody = UploadProgressBody(requestBody, object : UploadRequestListener {
            override fun onProgress(progress: Float, current: Long, total: Long) {
                /*   message = mHandler.obtainMessage()
                   message.what = 1
                   message.arg1 = (progress * 100).toInt()
                   mHandler.sendMessage(message)*/
                LogUtils.d("进度:" + progress)
            }

            override fun onFail(e: Throwable) {
                /*  TourCooLogUtil.e("异常：$e")
                  closeHudProgressDialog()*/
                LogUtils.e("进度:" + e.toString())
            }
        })
        ApiRepository.getInstance().apiService.uploadFiles(uploadProgressBody).enqueue(object : Callback<BaseResult<List<String?>?>?> {
            override fun onResponse(call: Call<BaseResult<List<String?>?>?>, response: Response<BaseResult<List<String?>?>?>) {
//                closeHudProgressDialog();
                val resp = response.body()
                if (resp != null) {
                    if (resp.code == 1 && resp.data != null) {
                        ToastUtil.showSuccess("请求成功")
                        /*   val imageUrlList: List<String> = java.util.ArrayList(resp.code)
                           if (imageUrlList.isEmpty()) {
   //                            ToastUtil.show("未获取到图片链接");
                               return
                           }*/
                        /*   //获取用户头像url
                           avatarUrl = imageUrlList[0]
                           imageDiskPathList.clear()
                           //将头像路径置为空 不然会死循环
                           avatarPath = ""
                           //再修改头像
                           doEditUserInfo()*/
                    } else {
                        ToastUtil.showFailed(resp.errMsg)
                    }
                }
            }

            override fun onFailure(call: Call<BaseResult<List<String?>?>?>, t: Throwable) {
                ToastUtil.showFailed("上传失败：$t")
            }
        })
    }
}