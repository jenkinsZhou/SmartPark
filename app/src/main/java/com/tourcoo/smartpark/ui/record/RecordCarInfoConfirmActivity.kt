package com.tourcoo.smartpark.ui.record

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.apkfuns.logutils.LogUtils
import com.kaopiz.kprogresshud.KProgressHUD
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.retrofit.UploadProgressBody
import com.tourcoo.smartpark.core.retrofit.UploadRequestListener
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.utils.FileUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.widget.keyboard.InputCompleteListener
import com.tourcoo.smartpark.widget.keyboard.KeyboardUtils
import com.tourcoo.smartpark.widget.keyboard.KingKeyboard
import com.tourcoo.smartpark.widget.selecter.PhotoAdapter
import kotlinx.android.synthetic.main.activity_record_confirm.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.lang.ref.WeakReference
import java.net.SocketException


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
    private var hud: KProgressHUD? = null
    private var message: Message? = null
    private val mHandler = MyHandler(this)
    private var imageUri: Uri? = null
    private var compressPath: String? = null

    companion object {
        const val REQUEST_CODE_TAKE_PHOTO = 1001
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_record_confirm
    }

    override fun initView(savedInstanceState: Bundle?) {
//        llContentView.setOnClickListener(this)
        llTakePhoto.setOnClickListener(this)
        tvConfirmRecord.setOnClickListener(this)
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
        kingKeyboard.register(etPlantLetter, KingKeyboard.KeyboardType.LICENSE_PLATE)
        kingKeyboard.register(etPlantNumber1, KingKeyboard.KeyboardType.LICENSE_PLATE)
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
                takePhoto()
            }
            R.id.tvConfirmRecord -> {
                doUpload()
            }
            else -> {
            }
        }
    }


    private fun initPhotoAdapter() {
        photoRecyclerView.layoutManager = GridLayoutManager(mContext, 3)
        photoAdapter = PhotoAdapter(this, onAddPicClickListener)
        photoAdapter!!.list = ArrayList()
        photoAdapter!!.setSelectMax(6)
        photoRecyclerView.adapter = photoAdapter
        /*  photoRecyclerView.layoutManager = GridLayoutManager(mContext, 3)
          photoAdapter?.bindToRecyclerView(photoRecyclerView)
          val footView = View.inflate(mContext, R.layout.item_footer_view_add_image, null)
          photoAdapter?.addFooterView(footView, LinearLayout.HORIZONTAL)
          photoAdapter?.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
             *//* when (view.id) {
                R.id.rlAddImage -> {
                    ToastUtil.showNormal("添加图片")
                }
            }*//*

        })
        photoAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.ivLocalPhoto -> {
                    ToastUtil.showSuccess("查看图片")
                }
                R.id.ivDelete -> {
                    ToastUtil.showNormal("删除图片")
                }
                else -> {
                }
            }
        }*/
    }


    private fun parseFileList(imageList: ArrayList<String>): ArrayList<File> {
        var file: File?
        val fileList = ArrayList<File>()
        for (path in imageList) {
            if (!TextUtils.isEmpty(path)) {
                file = File(path)
                fileList.add(file)
            }
        }
        return fileList
    }

    private fun compressImagesAndUpload(fileList: ArrayList<File>) {
        Luban.with(this)
                .load(fileList)
                .ignoreBy(100)
                .setTargetDir(getPath())
                .filter { path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")) }
                .setCompressListener(object : OnCompressListener {
                    override fun onStart() {
                        showLoading("正在处理图片数据...")
                    }

                    override fun onSuccess(file: File?) {
                        closeLoading()
                        if (file == null) {
                            ToastUtil.showFailed("压缩失败")
                            return
                        }
                        //注意，file是后台约定的参数，如果是多图，files，如果是单张图片，file就行
                        //上传图片需要MultipartBody
                        uploadImages(file)
                    }

                    override fun onError(e: Throwable?) {
                        closeLoading()
                        ToastUtil.showFailed("图片压缩失败：原因:" + e.toString())
                    }
                }).launch()


    }


    private fun getPath(): String? {
        val path: String = FileUtil.getExternalFilesDir() + "/temp/image/"
        val dir = File(path)
        if (!dir.exists()) {
            //出问题的位置
            dir.mkdirs()
        }
        return path
    }

    /**
     * 这里是真正上传图片的方法
     */
    private fun uploadImages(file: File) {
        val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
        //注意，file是后台约定的参数，如果是多图，files，如果是单张图片，file就行
        //这里上传的是多图
        builder.addFormDataPart("file", file.name, RequestBody.create(MediaType.parse("image/*"), file))
        val requestBody: RequestBody = builder.build()
        val uploadProgressBody = UploadProgressBody(requestBody, object : UploadRequestListener {
            override fun onProgress(progress: Float, current: Long, total: Long) {
                message = mHandler.obtainMessage()
                message?.what = 1
                message?.arg1 = (progress * 100).toInt()
                mHandler.sendMessage(message!!)
            }

            override fun onFail(e: Throwable) {
                LogUtils.e("异常：$e")
                closeHudProgressDialog()
            }
        })
        showHudProgressDialog()
        ApiRepository.getInstance().apiService.uploadFiles(uploadProgressBody).enqueue(object : Callback<BaseResult<List<String>?>?> {
            override fun onResponse(call: Call<BaseResult<List<String>?>?>, response: Response<BaseResult<List<String>?>?>) {
                closeHudProgressDialog()
                //图片上传成功回调
                compressPath = file.path
                handleUploadSuccess(response)
            }

            override fun onFailure(call: Call<BaseResult<List<String>?>?>, t: Throwable) {
                LogUtils.e("上传图片：$t")
                if (t is SocketException) {
                    ToastUtil.showFailed("文件过大 请重新拍摄后重试")
                }
            }
        })
    }

    private fun doUpload() {
        if (mSelectImagePathList.isEmpty()) {
            ToastUtil.showNormal("请先拍摄照片")
            return
        }
        compressImagesAndUpload(parseFileList(mSelectImagePathList))
    }

    private class MyHandler(dataActivity: RecordCarInfoConfirmActivity) : Handler() {
        var personalDataActivity: WeakReference<RecordCarInfoConfirmActivity> = WeakReference<RecordCarInfoConfirmActivity>(dataActivity)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> personalDataActivity.get()?.updateProgress(msg.arg1)
                else -> {
                }
            }
        }

    }

    private fun updateProgress(progress: Int) {
        LogUtils.i("进度：$progress")
        hud?.setProgress(progress)
    }

    private fun initProgressDialog() {
        hud = KProgressHUD.create(mContext)
                .setStyle(KProgressHUD.Style.PIE_DETERMINATE)
                .setCancellable(false)
                .setAutoDismiss(false)
                .setMaxProgress(100)
        hud!!.setProgress(0)
    }


    private fun showHudProgressDialog() {
        if (hud != null) {
            hud!!.setProgress(0)
        } else {
            initProgressDialog()
        }
        hud!!.show()
    }

    private fun closeHudProgressDialog() {
        if (hud != null && hud!!.isShowing) {
            hud!!.setProgress(0)
            hud!!.dismiss()
        }
        hud = null
    }

    private fun handleUploadSuccess(response: Response<BaseResult<List<String>?>?>) {
        if (response.isSuccessful && response.body() != null) {
            val resp: BaseResult<List<String>?>? = response.body()
            val imageUrlList: ArrayList<String> = ArrayList()
            if (resp != null && resp.data != null) {
                imageUrlList.addAll(resp.data!!)
                photoAdapter?.list?.add(compressPath!!)
                photoAdapter?.notifyDataSetChanged()
            }
        } else {
            ToastUtil.showNormal("服务器异常，请稍后重试")
        }
    }

    private fun takePhoto() {
        // 跳转到系统的拍照界面
        val intentToTakePhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 指定照片存储位置为sd卡本目录下
        // 这里设置为固定名字 这样就只会只有一张temp图 如果要所有中间图片都保存可以通过时间或者加其他东西设置图片的名称
        val mTempPhotoPath = mContext.getExternalFilesDir(null).toString() + File.separator + "SmartPark" + File.separator + "photo.jpeg"
        //判断版本是否在7.0以上
        imageUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            FileProvider.getUriForFile(mContext, mContext.packageName + ".SmartParkFileProvider", File(mTempPhotoPath))
        } else {
            Uri.fromFile(File(mTempPhotoPath))
        }
        //下面这句指定调用相机拍照后的照片存储的路径
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intentToTakePhoto, REQUEST_CODE_TAKE_PHOTO)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_TAKE_PHOTO -> {
                if (imageUri != null && resultCode == Activity.RESULT_OK) {
                    mSelectImagePathList.clear()
                    val result = FileUtil.getFilePathByUri(imageUri)
                    scanFile(mContext, result)
                    LogUtils.i("获取的路径:$result")
                    mSelectImagePathList.add(result)
                    doUpload()
                }
            }
            else -> {

            }
        }
    }

    /**
     * 针对系统文夹只需要扫描,不用插入内容提供者,不然会重复
     *
     * @param context  上下文
     * @param filePath 文件路径
     */
    private fun scanFile(context: Context, filePath: String) {
        if (!FileUtil.isFile(filePath)) return
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = Uri.fromFile(File(filePath))
        context.sendBroadcast(intent)
    }

    private val onAddPicClickListener = PhotoAdapter.onAddPicClickListener {
        // 进入相册 以下是例子：不需要的api可以不写
        takePhoto()
    }

    override fun onDestroy() {
        val path =getPath()
        val success = FileUtil.deleteFolder(path)
        LogUtils.i("是否删除了图片数据:$success"+"文件路径:"+path)
        super.onDestroy()
    }
}