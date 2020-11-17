package com.tourcoo.smartpark.core.retrofit.repository;

import com.tourcoo.smartpark.bean.AppUpdateBean;
import com.tourcoo.smartpark.bean.BaseResult;
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


 /*   public Observable<BaseResult<List<String>>> upload() {
        return ThreadTransformer.switchSchedulers(getApiService().uploadFiles().retryWhen(new RetryWhen()));
    }*/
}
