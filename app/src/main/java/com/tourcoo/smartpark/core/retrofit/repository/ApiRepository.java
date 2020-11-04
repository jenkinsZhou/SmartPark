package com.tourcoo.smartpark.core.retrofit.repository;

import com.tourcoo.smartpark.core.retrofit.ApiService;
import com.tourcoo.smartpark.core.retrofit.RetrofitHelper;

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

    private ApiService getApiService() {
        mApiService = RetrofitHelper.getInstance().createService(ApiService.class);
        return mApiService;
    }
}
