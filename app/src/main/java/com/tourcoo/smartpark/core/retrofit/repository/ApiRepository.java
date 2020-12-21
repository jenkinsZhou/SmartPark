package com.tourcoo.smartpark.core.retrofit.repository;

import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.bean.AppUpdateBean;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.PageBean;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.TokenInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.bean.fee.ArrearsHistoryRecord;
import com.tourcoo.smartpark.bean.fee.ArrearsRecord;
import com.tourcoo.smartpark.bean.fee.DailyFeeRecord;
import com.tourcoo.smartpark.bean.fee.FeeCertificate;
import com.tourcoo.smartpark.bean.fee.FeeDetail;
import com.tourcoo.smartpark.bean.fee.PayCertificate;
import com.tourcoo.smartpark.bean.fee.PayResult;
import com.tourcoo.smartpark.bean.report.DailyReport;
import com.tourcoo.smartpark.bean.settle.SettleDetail;
import com.tourcoo.smartpark.bean.system.AppVersion;
import com.tourcoo.smartpark.core.retrofit.ApiService;
import com.tourcoo.smartpark.core.retrofit.RetrofitHelper;
import com.tourcoo.smartpark.core.retrofit.RetryWhen;
import com.tourcoo.smartpark.core.retrofit.ThreadTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:14
 * @Email: 971613168@qq.com
 */
public class ApiRepository extends BaseRepository {

    private static volatile ApiRepository instance;
    private ApiService mApiService;

    private ApiRepository() {
        mApiService = getApiService();
    }

    public static ApiRepository getInstance() {
        if (instance == null) {
            synchronized (ApiRepository.class) {
                if (instance == null) {
                    instance = new ApiRepository();
                }
            }
        }
        return instance;
    }


    public ApiService getApiService() {
        mApiService = RetrofitHelper.getInstance().createService(ApiService.class);
        return mApiService;
    }


    public Observable<BaseResult<AppUpdateBean>> getAppVersionInfo() {
        return ThreadTransformer.switchSchedulers(getApiService().getAppVersion().retryWhen(new RetryWhen()));
    }


    public Observable<BaseResult<TokenInfo>> requestLogin(String account, String pass, int parkingId) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("number", account);
        params.put("password", pass);
        params.put("parkingId", parkingId);
        return ThreadTransformer.switchSchedulers(getApiService().requestLogin(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<List<ParkingInfo>>> requestParkingList(String account) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("number", account);
        return ThreadTransformer.switchSchedulers(getApiService().requestParkingList(params).retryWhen(new RetryWhen()));
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public Observable<BaseResult<UserInfo>> requestUserInfo() {
        return ThreadTransformer.switchSchedulers(getApiService().requestUserInfo().retryWhen(new RetryWhen()));
    }

    /**
     * 获取车位列表
     *
     * @return
     */
    public Observable<BaseResult<List<ParkSpaceInfo>>> requestParkSpaceList() {
        return ThreadTransformer.switchSchedulers(getApiService().requestParkSpaceList().retryWhen(new RetryWhen()));
    }

    /**
     * 重置密码
     *
     * @param newPass
     * @return
     */
    public Observable<BaseResult<Object>> requestUpdatePass(String newPass) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("password", newPass);
        return ThreadTransformer.switchSchedulers(getApiService().requestUpdatePass(params).retryWhen(new RetryWhen()));
    }

    /**
     * 修改密码
     *
     * @param newPass
     * @return
     */
    public Observable<BaseResult<Object>> requestEditPass(String oldPass, String newPass, String rePass) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("originalPassword", oldPass);
        params.put("password", newPass);
        params.put("rePassword", rePass);
        return ThreadTransformer.switchSchedulers(getApiService().requestUpdatePass(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<Object>> requestAddParkingSpace(long parkingSpaceId, String plantNum, int carType, String[] photos) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("parkingSpaceId", parkingSpaceId);
        params.put("number", plantNum);
        params.put("type", carType);
        params.put("photos", photos);
        return ThreadTransformer.switchSchedulers(getApiService().requestAddParkingSpace(params).retryWhen(new RetryWhen()));
    }


    /**
     * 获取待收取停车费的车位列表
     *
     * @param plantNum 要搜索的车牌号
     * @return
     */
    public Observable<BaseResult<List<ParkSpaceInfo>>> requestSpaceSettleList(String plantNum) {
        Map<String, Object> params = new HashMap<>(1);
        if (!TextUtils.isEmpty(plantNum)) {
            params.put("number", plantNum);
        }
        return ThreadTransformer.switchSchedulers(getApiService().requestSpaceSettleList(params).retryWhen(new RetryWhen()));
    }

    /**
     * 获取某停车位的具体收费信息
     *
     * @param recordId ignore
     * @return
     */
    public Observable<BaseResult<SettleDetail>> requestSpaceSettleDetail(long recordId, boolean ignore, String arrearsIds) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", recordId);
        if (ignore) {
            params.put("ignore", 1);
        } else {
            params.put("ignore", 0);
        }
        if (!TextUtils.isEmpty(arrearsIds)) {
            params.put("arrearsId", arrearsIds);
        }
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestSpaceSettleDetail(params).retryWhen(new RetryWhen()));
    }

    /**
     * 标为欠费
     *
     * @param parkId
     * @return
     */
    public Observable<BaseResult<Object>> requestFlagArrears(long parkId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", parkId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestFlagArrears(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<List<ArrearsHistoryRecord>>> requestArrearsHistoryList(long carId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("carId", carId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestArrearsHistoryList(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<PayResult>> requestPay(long recordId, int type, String code, Integer[] arrearsId) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("id", recordId);
        params.put("type", type);
        if (!TextUtils.isEmpty(code)) {
            params.put("code", code);
        }
        params.put("arrearsId", arrearsId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestPay(params).retryWhen(new RetryWhen()));
    }

    /**
     * 用户日报
     *
     * @return
     */
    public Observable<BaseResult<DailyReport>> requestDailyReport() {
        return ThreadTransformer.switchSchedulers(getApiService().requestDailyReport().retryWhen(new RetryWhen()));
    }

    /**
     * 签到签出
     *
     * @return
     */
    public Observable<BaseResult<Object>> requestSign() {
        return ThreadTransformer.switchSchedulers(getApiService().requestSign().retryWhen(new RetryWhen()));
    }

    /**
     * 收费记录
     *
     * @param page
     * @return
     */
    public Observable<BaseResult<PageBean<DailyFeeRecord>>> requestDailyRecordList(int page) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("page", page);
        params.put("perPage", 10);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestDailyRecordList(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<AppVersion>> requestAppVersion(String versionName) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("versionName", versionName);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestAppVersion(params).retryWhen(new RetryWhen()));
    }


    /**
     * 欠费记录列表
     *
     * @param page
     * @return
     */
    public Observable<BaseResult<PageBean<ArrearsRecord>>> requestArrearsRecordList(int page) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("page", page);
        params.put("perPage", 10);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestArrearsRecordList(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<FeeCertificate>> requestFeeCertificate(long recordId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", recordId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestFeeCertificate(params).retryWhen(new RetryWhen()));
    }


    public Observable<BaseResult<PayCertificate>> requestPayCertificate(long recordId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", recordId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestPayCertificate(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<FeeDetail>> requestFeeDetail(long recordId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", recordId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestFeeDetail(params).retryWhen(new RetryWhen()));
    }

    public Observable<BaseResult<FeeDetail>> requestArrearsDetail(long recordId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", recordId);
        LogUtils.tag("提交到后台的参数").i(params);
        return ThreadTransformer.switchSchedulers(getApiService().requestArrearsDetail(params).retryWhen(new RetryWhen()));
    }

}
