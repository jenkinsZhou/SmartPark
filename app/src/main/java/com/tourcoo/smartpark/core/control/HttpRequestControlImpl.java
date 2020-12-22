package com.tourcoo.smartpark.core.control;

import android.accounts.AccountsException;
import android.accounts.NetworkErrorException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.kingja.loadsir.core.LoadService;
import com.tourcoo.SmartParkApplication;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.core.multi_status.MultiStatusErrorCallback;
import com.tourcoo.smartpark.core.multi_status.MultiStatusNetErrorCallback;
import com.tourcoo.smartpark.core.utils.NetworkUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月22日11:10
 * @Email: 971613168@qq.com
 */
public class HttpRequestControlImpl implements HttpRequestControl {
    private static String TAG = "HttpRequestControlImpl";


    @Override
    public void httpRequestSuccess(IHttpRequestControl httpRequestControl, BaseResult<?> data) {
        if (httpRequestControl == null) {
            return;
        }
        LoadService statusLayoutManager = httpRequestControl.getStatusLayoutManager();
        if (statusLayoutManager == null) {
            ToastUtil.showWarning("未获取到多状态管理实例");
            return;
        }
        if (data == null) {
            statusLayoutManager.showCallback(MultiStatusErrorCallback.class);
            return;
        }
        if (data.getCode() != RequestConfig.REQUEST_CODE_SUCCESS) {
            ToastUtil.showNormal(data.getErrMsg());
            return;
        }
        httpRequestControl.handleSuccessData(data);
        statusLayoutManager.showSuccess();
    }

    @Override
    public void httpRequestError(IHttpRequestControl httpRequestControl, Throwable e) {
        int reason = R.string.exception_other_error;
        if (!NetworkUtil.isConnected(SmartParkApplication.getContext())) {
            reason = R.string.exception_network_not_connected;
        } else {
            //网络异常--继承于AccountsException
            if (e instanceof NetworkErrorException) {
                reason = R.string.exception_network_error;
                //账户异常
            } else if (e instanceof AccountsException) {
                reason = R.string.exception_accounts;
                //连接异常--继承于SocketException
            } else if (e instanceof ConnectException) {
                reason = R.string.exception_connect;
                //socket异常
            } else if (e instanceof SocketException) {
                reason = R.string.exception_socket;
                // http异常
            } else if (e instanceof HttpException) {
                reason = R.string.exception_http;
                //DNS错误
            } else if (e instanceof UnknownHostException) {
                reason = R.string.exception_unknown_host;
            } else if (e instanceof JsonSyntaxException
                    || e instanceof JsonIOException
                    || e instanceof JsonParseException) {
                //数据格式化错误
                reason = R.string.exception_json_syntax;
            } else if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
                reason = R.string.exception_time_out;
            } else if (e instanceof ClassCastException) {
                reason = R.string.exception_class_cast;
            }
        }
        if (httpRequestControl == null || httpRequestControl.getStatusLayoutManager() == null) {
            ToastUtil.showNormal(reason);
            return;
        }
        LoadService statusManager = httpRequestControl.getStatusLayoutManager();
        if (!NetworkUtil.isConnected(SmartParkApplication.getContext())) {
            statusManager.showCallback(MultiStatusNetErrorCallback.class);
            return;
        }
        statusManager.showCallback(MultiStatusErrorCallback.class);
    }
}
