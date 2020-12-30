package com.tourcoo.smartpark.ui.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.apkfuns.logutils.LogUtils
import com.jakewharton.rxbinding2.widget.RxTextView
import com.kaopiz.kprogresshud.KProgressHUD
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.bean.BaseResult
import com.tourcoo.smartpark.bean.LocalImage
import com.tourcoo.smartpark.bean.ParkSpaceInfo
import com.tourcoo.smartpark.constant.ParkConstant.CAR_TYPE_GREEN
import com.tourcoo.smartpark.constant.ParkConstant.CAR_TYPE_NORMAL
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.control.RequestConfig
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver
import com.tourcoo.smartpark.core.retrofit.UploadProgressBody
import com.tourcoo.smartpark.core.retrofit.UploadRequestListener
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository.*
import com.tourcoo.smartpark.core.utils.FileUtil
import com.tourcoo.smartpark.core.utils.SizeUtil
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.event.OrcInitEvent
import com.tourcoo.smartpark.threadpool.ThreadPoolManager
import com.tourcoo.smartpark.ui.HomeActivity
import com.tourcoo.smartpark.ui.fee.ExitPayFeeEnterActivity
import com.tourcoo.smartpark.util.StringUtil
import com.tourcoo.smartpark.widget.dialog.IosAlertDialog
import com.tourcoo.smartpark.widget.keyboard.PlateKeyboardView
import com.tourcoo.smartpark.widget.orc.PredictorWrapper
import com.tourcoo.smartpark.widget.orc.RecogniseListener
import com.tourcoo.smartpark.widget.searchview.BSearchEdit
import com.tourcoo.smartpark.widget.selecter.PhotoAdapter
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_record_confirm.*
import kotlinx.android.synthetic.main.activity_report_fee_common.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.net.SocketException
import java.util.concurrent.TimeUnit


/**
 *@description : 确认登记
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年11月13日11:09
 * @Email: 971613168@qq.com
 */
class RecordCarInfoConfirmActivity : BaseTitleActivity(), View.OnClickListener, EasyPermissions.PermissionCallbacks {
    private var parkInfo: ParkSpaceInfo? = null
    private var photoAdapter: PhotoAdapter? = null
    private val mSelectLocalImagePathList = ArrayList<String>()
    private var hud: KProgressHUD? = null
    private var message: Message? = null
    private val mHandler = MyHandler(this)
    private var imageUri: Uri? = null
    private var compressPath: String? = null
    private var needOrc: Boolean = false
    private var keyboardView: PlateKeyboardView? = null
    private var carType = CAR_TYPE_NORMAL
    private var isOrcPhoto = false
    private var bSearchEdit: BSearchEdit? = null

    //后台返回的图片地址
    private val serviceImageUrlList = ArrayList<String>()
    private var currentSelectPosition = -1

    //权限参数
    private val needPermissions = arrayOfNulls<String>(3)
    private val parkingList = ArrayList<ParkSpaceInfo>()

    private var spaceKeyboardView: PlateKeyboardView? = null

    companion object {
        const val REQUEST_CODE_TAKE_PHOTO = 1001

        /**
         * 随便赋值的一个唯一标识码
         */
        const val REQUEST_PERMISSION_STORAGE = 100
        const val MIN_LENGTH_PLANT_NUM = 7

        const val LENGTH_CAR_TYPE_GREEN = 8
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_record_confirm
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        parkInfo = intent?.getParcelableExtra(HomeActivity.EXTRA_SPACE_INFO)
        showParkInfo()
        llTakePhoto.setOnClickListener(this)
        tvConfirmRecord.setOnClickListener(this)
        needPermissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE
        needPermissions[1] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        needPermissions[2] = Manifest.permission.CAMERA
        keyboardView = PlateKeyboardView(mContext)
        initPhotoAdapter()
        takePhoto.text = "车辆拍照"
        if (!PredictorWrapper.isInitSuccess()) {
            showLoading("正在初始化组件...")
        }
        initInputLayout()
        llParkingPlace.post {
            initSearchView(llParkingPlace.width.toFloat())
        }
        initSearchInput()
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("确认登记")
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.llTakePhoto -> {
                if (EasyPermissions.hasPermissions(this, *needPermissions)) {
                    needOrc = true
                    takePhoto()
                } else {
                    checkPermission()
                }

            }
            R.id.tvConfirmRecord -> {
                doSignParkSpace()
            }
            R.id.plantInputLayout -> {
                keyboardView?.showKeyboard(plantInputLayout)
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
        photoAdapter!!.setOnItemDeleteClickListener { position, v ->
            if (position < 0) {
                return@setOnItemDeleteClickListener
            }
            if (photoAdapter!!.list.size < position || serviceImageUrlList.size < position) {
                LogUtils.tag(TAG).e("删除被拦截")
                return@setOnItemDeleteClickListener
            }
            val deletePath = photoAdapter!!.list[position].serviceImageUrl
            val success = serviceImageUrlList.remove(deletePath)
            LogUtils.tag(TAG).d("数据删除状态:$success，删除的路径--->$deletePath--剩余长度:" + serviceImageUrlList.size)
        }
        photoRecyclerView.adapter = photoAdapter

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
        compressPath = file.path
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
        getInstance().apiService.uploadFiles(uploadProgressBody).enqueue(object : Callback<BaseResult<List<String>?>?> {
            override fun onResponse(call: Call<BaseResult<List<String>?>?>, response: Response<BaseResult<List<String>?>?>) {
                closeHudProgressDialog()
                //图片上传成功回调
                handleUploadSuccess(response, isOrcPhoto)
            }

            override fun onFailure(call: Call<BaseResult<List<String>?>?>, t: Throwable) {
                LogUtils.e("上传图片失败：$t")
                closeHudProgressDialog()
                if (t is SocketException) {
                    ToastUtil.showFailed("文件过大 请重新拍摄后重试")
                }
            }
        })
    }

    private fun doUpload() {
        if (mSelectLocalImagePathList.isEmpty()) {
            ToastUtil.showNormal("请先拍摄照片")
            return
        }
        compressImagesAndUpload(parseFileList(mSelectLocalImagePathList))
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
        if (progress >= 100) {
            hud?.dismiss()
        }
    }

    private fun initProgressDialog() {
        hud = KProgressHUD.create(mContext, KProgressHUD.Style.PIE_DETERMINATE)
                .setCancellable(false)
                .setAutoDismiss(true)
                .setMaxProgress(100)
    }


    private fun showHudProgressDialog() {
        initProgressDialog()
        hud!!.show()
    }

    private fun closeHudProgressDialog() {
        if (hud != null) {
            hud!!.dismiss()
        }
        hud = null
    }

    private fun handleUploadSuccess(response: Response<BaseResult<List<String>?>?>, isOrcPhoto: Boolean) {
        if (response.isSuccessful && response.body() != null) {
            val resp: BaseResult<List<String>?>? = response.body()
            if (resp != null && resp.data != null && resp.data!!.isNotEmpty()) {
                val serviceImageUrl = StringUtil.getNotNullValue(resp.data!![0])
                if (isOrcPhoto) {
                    //这里直接上传
                    requestAddParkingSpace(getUrlImageList(serviceImageUrl))
                } else {
                    serviceImageUrlList.add(serviceImageUrl)
                    addDataToRecyclerView(compressPath, serviceImageUrl, needOrc)
                }

            }
        } else {
            ToastUtil.showNormal("服务器异常，请稍后重试")
        }
    }

    private fun takePhoto() {
        val intentToTakePhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // 指定照片存储位置为sd卡本目录下
        // 这里设置为固定名字 这样就只会只有一张temp图 如果要所有中间图片都保存可以通过时间或者加其他东西设置图片的名称
        val mTempPhotoPath = mContext.getExternalFilesDir(null)!!.absolutePath + File.separator + "Pictures" + File.pathSeparator + "photo.jpeg"
        //判断版本是否在7.0以上
        imageUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            FileProvider.getUriForFile(mContext, "com.tourcoo.smartpark.SmartParkFileProvider", File(mTempPhotoPath))
        } else {
            Uri.fromFile(File(mTempPhotoPath))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intentToTakePhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                    mSelectLocalImagePathList.clear()
                    val currentImagePath = FileUtil.getPath(imageUri)
                    scanFile(mContext, currentImagePath)
                    LogUtils.i("获取的路径:$currentImagePath")
                    mSelectLocalImagePathList.add(currentImagePath)
                    if (needOrc) {
                        //这里先不上传 先识别 然后直接将图片添加到recyclerview里面
                        compressImagesAndAddToDataToRecyclerview(File(currentImagePath))
                    } else {
                        //这里如果不是拍照识别车牌的话 就先上传
                        doUpload()
                    }
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
        needOrc = false
        isOrcPhoto = false
        takePhoto()
    }

    override fun onDestroy() {
        val path = getPath()
        val success = FileUtil.deleteFolder(path)
        LogUtils.i("是否删除了图片数据:$success" + "文件路径:" + path)
        EventBus.getDefault().unregister(this)
        PredictorWrapper.release()
        mHandler.removeCallbacksAndMessages(null)
        spaceKeyboardView = null
        keyboardView = null
        super.onDestroy()
    }


    private fun doRecognisePlant(photoPath: String) {
        if (!PredictorWrapper.isInitSuccess()) {
            //todo 目前先拦截
            ToastUtil.showNormalDebug("当前车牌识别sdk未初始化")
            return
        }
        ThreadPoolManager.getThreadPoolProxy().execute(Runnable {
            try {
                PredictorWrapper.setRecogniseListener(object : RecogniseListener {
                    override fun recogniseSuccess(result: com.baidu.vis.ocrplatenumber.Response?) {
                        LogUtils.tag("识别成功").i(result)
                        handleReconSuccessCallback(result)
                        closeHudProgressDialog()
                    }

                    override fun recogniseFailed() {
                        closeHudProgressDialog()
//                ToastUtil.showFailed("识别失败")
                    }

                })
                ThreadPoolManager.getThreadPoolProxy().execute(Runnable {
                    val bitmap: Bitmap = BitmapFactory.decodeFile(photoPath)
                    PredictorWrapper.syncTestOneImage(mContext, bitmap)
                })
            } catch (e: Exception) {
                ToastUtil.showFailedDebug("车牌识别失败:$e")
                e.printStackTrace()
            }
        })


    }

    private fun handleReconSuccessCallback(result: com.baidu.vis.ocrplatenumber.Response?) {
        if (result == null) {
            return
        }
        plantInputLayout?.text = result.plate_number
    }

    private fun fillBluePlant(number: String) {
        if (number.length < 7) {
            return
        }

    }

    private fun fillGreenPlant(number: String) {
        if (number.length < 8) {
            return
        }
        fillBluePlant(number)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: OrcInitEvent?) {
        closeLoading()
        LogUtils.tag(TAG).i("onMessageEvent--->event=" + JSON.toJSON(event))
        if (event == null) {
            return
        }
    }

    private fun addDataToRecyclerView(imagePath: String?, imageUrl: String?, isOrc: Boolean) {
        val localImage = LocalImage(StringUtil.getNotNullValue(imagePath), isOrc)
        //将后台返回的图片地址赋值给本地实体
        localImage.serviceImageUrl = imageUrl
        photoAdapter?.addData(localImage)
    }

    private fun compressImagesAndAddToDataToRecyclerview(file: File) {
        ThreadPoolManager.getThreadPoolProxy().execute(Runnable {
            Luban.with(this)
                    .load(file)
                    .ignoreBy(100)
                    .setTargetDir(getPath())
                    .filter { path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")) }
                    .setCompressListener(object : OnCompressListener {
                        override fun onStart() {
                            showLoading("正在处理图片数据...")
                        }

                        override fun onSuccess(compressFile: File?) {
                            closeLoading()
                            if (compressFile == null) {
                                ToastUtil.showFailed("压缩失败")
                                return
                            }
                            doRecognisePlant(compressFile.path)
                            //这里不做上传 直接显示到列表中
                            addDataToRecyclerView(compressFile.path, null, true)
                        }

                        override fun onError(e: Throwable?) {
                            closeLoading()
                            ToastUtil.showFailed("图片压缩失败：原因:" + e.toString())
                        }
                    }).launch()
        })

    }

    private fun initInputLayout() {
//设置字体大小
        plantInputLayout.setTextSize(16f)
//设置字体颜色
        plantInputLayout.setTextColor(ContextCompat.getColor(mContext, R.color.colorGraySmallText))
        plantInputLayout.setOnClickListener(this)
        //获取输入的内容
        plantInputLayout.text
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        //将结果传入EasyPermissions中
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String?>) {
        takePhoto()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String?>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //这个方法有个前提是，用户点击了“不再询问”后，才判断权限没有被获取到
            mHandler.postDelayed(Runnable { showSetting() }, 500)
        } else if (!EasyPermissions.hasPermissions(this, *needPermissions)) {
            //这里响应的是除了AppSettingsDialog这个弹出框，剩下的两个弹出框被拒绝或者取消的效果
            exitApp()
        }
    }

    private fun showSetting() {
        IosAlertDialog(mContext)
                .init()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .setTitle("权限申请")
                .setMsg("应用需要您授予相关权限 请前往授权管理页面授权")
                .setPositiveButton("前往授权", View.OnClickListener { skipDetailSettingIntent() })
                .setNegativeButton("退出应用", View.OnClickListener { exitApp() }).show()
    }


    private fun exitApp() {
        ToastUtil.showNormal("您未授予相关权限")
        finish()
    }

    private fun skipDetailSettingIntent() {
        val intent = Intent()
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra("com.android.settings.ApplicationPkgName", packageName)
        }
        try {
            startActivityForResult(intent, REQUEST_PERMISSION_STORAGE)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 检查权限
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_STORAGE)
    private fun checkPermission() {
        mHandler.postDelayed({
            if (!EasyPermissions.hasPermissions(this, *needPermissions)) {
                EasyPermissions.requestPermissions(this, "需要获取相关权限", REQUEST_PERMISSION_STORAGE, *needPermissions)
            }
        }, 300)
    }

    private fun showParkInfo() {
        if (parkInfo == null) {
            tvParkNumber.setText("")
            return
        }
        tvParkNumber.setText(StringUtil.getNotNullValue(parkInfo!!.number))
    }

    /**
     * 来车登记（车辆入库）
     */
    private fun requestAddParkingSpace(imageUrlList: List<String>) {
        val imageArray = StringUtil.listParseStringArray(imageUrlList)
        if (imageArray.isEmpty()) {
            ToastUtil.showNormal("请至少上传一张图片")
            return
        }
        if (parkInfo == null) {
            ToastUtil.showWarning("请选择车位")
            return
        }
        getInstance().requestAddParkingSpace(parkInfo!!.id, plantInputLayout.text, carType, imageArray).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<Any>>() {
            override fun onRequestSuccess(entity: BaseResult<Any>?) {
                if (entity == null) {
                    return
                }
                if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS) {
                    ToastUtil.showSuccess(entity.errMsg)
                    handleSignSuccessCallback()
                } else {
                    ToastUtil.showNormal(entity.errMsg)
                }
            }

        })
    }


    private fun doSignParkSpace() {
        val orcPhotoPosition = photoAdapter!!.orcPhotoIndex
        if (orcPhotoPosition < 0) {
            //说明没有拍照识别 直接登记
            if (photoAdapter!!.serviceUrlList.isEmpty()) {
                ToastUtil.showWarning("至少要上传一张带有车牌的图片")
                return
            }
            val plantNum = plantInputLayout.text
            if (TextUtils.isEmpty(plantNum) || plantNum.length < MIN_LENGTH_PLANT_NUM) {
                ToastUtil.showNormal("请输入正确的车牌号")
                return
            }
            if (plantNum.length == LENGTH_CAR_TYPE_GREEN) {
                carType = CAR_TYPE_GREEN
            } else {
                carType = CAR_TYPE_NORMAL
            }
            isOrcPhoto = false
            requestAddParkingSpace(photoAdapter!!.serviceUrlList)

        } else {
            //说明有拍照识别的照片 需要先上传
            isOrcPhoto = true
            if (parkInfo == null || parkInfo!!.id < 0) {
                ToastUtil.showNormal("未获取到车位信息")
                return
            }
            if (carType < 0) {
                ToastUtil.showNormal("未获取到车辆类型")
                return
            }
            val plantNum = plantInputLayout.text
            if (TextUtils.isEmpty(plantNum) || plantNum.length < MIN_LENGTH_PLANT_NUM) {
                ToastUtil.showNormal("请输入正确的车牌号")
                return
            }
            if (plantNum.length == LENGTH_CAR_TYPE_GREEN) {
                carType = CAR_TYPE_GREEN
            } else {
                carType = CAR_TYPE_NORMAL
            }
            compressImagesAndUpload(parseFileList(mSelectLocalImagePathList))
        }
    }


    private fun getUrlImageList(orcImageUrl: String?): ArrayList<String> {
        val imageList = ArrayList<String>()
        if (!TextUtils.isEmpty(orcImageUrl)) {
            imageList.add(orcImageUrl!!)
        }
        for (localImage in photoAdapter!!.list) {
            if (!TextUtils.isEmpty(localImage.serviceImageUrl)) {
                imageList.add(localImage.serviceImageUrl)
            }
        }
        return imageList
    }

    private fun handleSignSuccessCallback() {
        setResult(RESULT_OK)
        finish()
    }


    private fun initSearchView(widthPx: Float) {
        //第三个必须要设置窗体的宽度，单位dp
        bSearchEdit = BSearchEdit(this, llParkingPlace, SizeUtil.px2dp(widthPx))
        bSearchEdit!!.setTimely(false)
        bSearchEdit!!.build()
        bSearchEdit!!.setTextClickListener { position, text ->
            currentSelectPosition = position
            tvParkNumber.setText(text!!)
            try {
                tvParkNumber.setSelection(text.length)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bSearchEdit!!.setOnClickListener {
            spaceKeyboardView?.showKeyboard(tvParkNumber, InputType.TYPE_CLASS_NUMBER)
        }
    }

    private fun showSpaceList(parkingInfoList: List<ParkSpaceInfo>?) {
        if (parkingInfoList == null) {
            return
        }
        parkingList.clear()
        val parkingStrList = ArrayList<String>()
        parkingInfoList.forEach {
            parkingStrList.add(StringUtil.getNotNullValue(it.number))
            parkingList.add(it)
        }
        bSearchEdit!!.setSearchList(parkingStrList)
        bSearchEdit!!.showPopup()
    }

    private fun requestParkUnusedList() {
        if (TextUtils.isEmpty(tvParkNumber.text.toString())) {
            return
        }
        if (StringUtil.judgeContainsLetter(tvParkNumber.text.toString())) {
            bSearchEdit?.clear()
            return
        }
        getInstance().requestParkUnusedList(tvParkNumber.text.toString()).compose(bindUntilEvent(ActivityEvent.DESTROY)).subscribe(object : BaseLoadingObserver<BaseResult<List<ParkSpaceInfo>>>() {
            override fun onRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
                handleRequestSuccess(entity)
            }

            override fun onRequestError(throwable: Throwable?) {
                super.onRequestError(throwable)
                feeRecordRefreshLayout.finishRefresh(false)
            }

        })
    }

    private fun handleRequestSuccess(entity: BaseResult<List<ParkSpaceInfo>>?) {
        if (entity == null) {
            ToastUtil.showNormal(R.string.exception_service_out)
            return
        }
        if (entity.code == RequestConfig.REQUEST_CODE_SUCCESS && entity.data != null) {
            showSpaceList(entity.data)
        } else {
            ToastUtil.showFailed(entity.errMsg)
        }
    }


    @SuppressLint("CheckResult")
    private fun listenInput(editText: EditText) {
        RxTextView.textChanges(editText)
                .debounce(ExitPayFeeEnterActivity.DELAY_TIME, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { charSequence -> charSequence.toString() }
                .subscribe { s ->
                    requestParkUnusedList()
                }
    }


    private fun initSearchInput() {
        tvParkNumber.setFocusable(true)
        tvParkNumber.setFocusableInTouchMode(true)
        disableShowSoftInput(tvParkNumber)
        tvParkNumber.requestFocus()
        spaceKeyboardView = PlateKeyboardView(mContext)
        spaceKeyboardView?.isAutoShowProvince = false
        tvParkNumber.setOnClickListener(View.OnClickListener {
            spaceKeyboardView?.showKeyboard(tvParkNumber, InputType.TYPE_CLASS_NUMBER)
        })
        llParkingPlace.setOnClickListener {
            spaceKeyboardView?.showKeyboard(tvParkNumber, InputType.TYPE_CLASS_NUMBER)
        }
        spaceKeyboardView?.setOnKeyboardFinishListener({
        }, "关闭")
        listenInput(tvParkNumber)
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

}