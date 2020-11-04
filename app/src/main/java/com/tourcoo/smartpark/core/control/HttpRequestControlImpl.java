package com.tourcoo.smartpark.core.control;

import android.accounts.AccountsException;
import android.accounts.NetworkErrorException;

import com.apkfuns.logutils.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.kingja.loadsir.core.LoadService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.tourcoo.SmartParkApplication;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.multi_status.EmptyStatusCallback;
import com.tourcoo.smartpark.core.multi_status.ErrorStatusCallback;
import com.tourcoo.smartpark.core.utils.NetworkUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.reactivex.annotations.NonNull;
import retrofit2.HttpException;

import static com.tourcoo.smartpark.core.control.RequestConstant.FIRST_PAGE;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月28日11:12
 * @Email: 971613168@qq.com
 */
@SuppressWarnings("unchecked")
public class HttpRequestControlImpl implements HttpRequestControl {

    private static String TAG = "HttpRequestControlImpl";

    @Override
    public void httpRequestSuccess(IHttpRequestControl httpRequestControl, List<?> dataList, OnHttpDataListener httpDataListener) {
        if (httpRequestControl == null) {
            return;
        }
        SmartRefreshLayout smartRefreshLayout = httpRequestControl.getRefreshLayout();
        BaseQuickAdapter adapter = httpRequestControl.getRecyclerAdapter();
        LoadService statusLayoutManager = httpRequestControl.getStatusLayoutManager();
        int page = httpRequestControl.getCurrentPage();
        int size = httpRequestControl.getPageSize();
        if (smartRefreshLayout != null) {
            smartRefreshLayout.finishRefresh();
        }
        if (adapter == null) {
            return;
        }
        adapter.loadMoreComplete();
        if (dataList == null || dataList.isEmpty()) {
            if (page == FIRST_PAGE) {
                //第一页都没有数据
                adapter.setNewData(new ArrayList());
                //显示空布局
                statusLayoutManager.showCallback(EmptyStatusCallback.class);
                if (httpDataListener != null) {
                    httpDataListener.empty();
                }
            } else {
                //说明当前不是第一页 但是没有数据 说明没有更多了
                adapter.loadMoreEnd();
                if (httpDataListener != null) {
                    httpDataListener.onNoMore();
                }
            }
            return;
        }
        //到了这里说明有数据返回
        //显示正常布局
        statusLayoutManager.showSuccess();
        if (smartRefreshLayout != null) {
            if (smartRefreshLayout.getState() == RefreshState.Refreshing || page == FIRST_PAGE) {
                adapter.setNewData(new ArrayList());
                LogUtils.i("---->", "----");
            }
            adapter.addData(dataList);
            if (httpDataListener != null) {
                httpDataListener.onNext();
            }
            if (dataList.size() < size) {
                //说明没有更多了
                adapter.loadMoreEnd();
                if (httpDataListener != null) {
                    //回调给监听接口
                    httpDataListener.onNoMore();
                }
            }
        } else {
            LogUtils.tag(TAG).e("smartRefreshLayout为null ");
        }
    }

    @Override
    public void httpRequestError(IHttpRequestControl httpRequestControl, @NonNull Throwable e) {
        LogUtils.e(TAG, "httpRequestError:" + e.getMessage());
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
        SmartRefreshLayout smartRefreshLayout = httpRequestControl.getRefreshLayout();
        BaseQuickAdapter adapter = httpRequestControl.getRecyclerAdapter();
        LoadService statusManager = httpRequestControl.getStatusLayoutManager();
        int page = httpRequestControl.getCurrentPage();
        if (smartRefreshLayout != null) {
            smartRefreshLayout.finishRefresh(false);
        }
        if (adapter != null) {
            adapter.loadMoreComplete();
            if (statusManager == null) {
                return;
            }
            if (page == FIRST_PAGE) {
                //                if (!NetworkUtil.isConnected(App.getContext())) {
//                    //可自定义网络错误页面展示
//                    statusLayoutManager.showCustomLayout(R.layout.layout_status_layout_manager_error);
//                } else {
                statusManager.showCallback(ErrorStatusCallback.class);
//                }
                return;
            }
            //可根据不同错误展示不同错误布局
            statusManager.showCallback(ErrorStatusCallback.class);
        } else {
            ToastUtil.showFailed("适配器为空");
        }
    }
}
