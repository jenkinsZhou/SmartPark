package com.tourcoo.smartpark.core.retrofit;

import com.tourcoo.smartpark.bean.AppUpdateBean;
import com.tourcoo.smartpark.bean.BaseResult;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:15
 * @Email: 971613168@qq.com
 */
public interface ApiService {

    /**
     * 获取版本更新数据
     *
     * @return
     */
    @GET("handheld/version")
    Observable<BaseResult<AppUpdateBean>> getAppVersion();

    /**
     * 多个文件上传
     *
     * @param files
     * @return
     */
    @POST("file/upload")
    Call<BaseResult<List<String>>> uploadFiles(@Body RequestBody files);
}
