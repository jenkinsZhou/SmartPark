package com.tourcoo.smartpark.ui.pay

import android.content.Intent
import android.os.Bundle
import cn.bingoogolapple.qrcode.core.QRCodeView
import com.tourcoo.smartpark.R
import com.tourcoo.smartpark.core.base.activity.BaseTitleActivity
import com.tourcoo.smartpark.core.utils.ToastUtil
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView
import com.tourcoo.smartpark.ui.fee.ExitPayFeeDetailActivity
import com.tourcoo.smartpark.util.StringUtil
import kotlinx.android.synthetic.main.activity_scan_code_layout.*

/**
 *@description : JenkinsZhou
 *@company :途酷科技
 * @author :JenkinsZhou
 * @date 2020年12月08日14:58
 * @Email: 971613168@qq.com
 */
class ScanCodePayActivity : BaseTitleActivity(), QRCodeView.Delegate {
    override fun getContentLayout(): Int {
        return R.layout.activity_scan_code_layout
    }

    override fun initView(savedInstanceState: Bundle?) {
        zxingview.setDelegate(this)
    }

    override fun setTitleBar(titleBar: TitleBarView?) {
        titleBar?.setTitleMainText("扫码支付")
    }


    override fun onStart() {
        super.onStart()
        zxingview.startCamera() // 打开后置摄像头开始预览，但是并未开始识别

//        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别

        //        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        zxingview.startSpotAndShowRect() // 显示扫描框，并开始识别

    }

    override fun onStop() {
        zxingview.stopCamera() // 关闭摄像头预览，并且隐藏扫描框
        super.onStop()
    }

    override fun onDestroy() {
        zxingview.onDestroy() // 销毁二维码扫描控件
        super.onDestroy()
    }

    /*  private fun vibrate() {
          val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
          vibrator.vibrate(200)
      }*/

    override fun onScanQRCodeSuccess(result: String?) {
        handleScanSuccessCallback(result)
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        var tipText: String = zxingview.getScanBoxView().getTipText()
        val ambientBrightnessTip = "\n环境过暗，请打开闪光灯"
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                zxingview.getScanBoxView().setTipText(tipText + ambientBrightnessTip)
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip))
                zxingview.getScanBoxView().setTipText(tipText)
            }
        }
    }

    override fun onScanQRCodeOpenCameraError() {
        ToastUtil.showFailed("打开相机失败")
    }

    private fun handleScanSuccessCallback(result: String?) {
        val intent = Intent()
        intent.putExtra(ExitPayFeeDetailActivity.EXTRA_SCAN_RESULT, StringUtil.getNotNullValue(result))
        setResult(RESULT_OK, intent)
        finish()
    }
}