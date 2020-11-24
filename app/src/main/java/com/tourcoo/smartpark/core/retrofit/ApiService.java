package com.tourcoo.smartpark.core.retrofit;

import com.tourcoo.smartpark.bean.AppUpdateBean;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.TokenInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
    @GET("/handheld/version")
    Observable<BaseResult<AppUpdateBean>> getAppVersion();

    /**
     * 多个文件上传
     *
     * @param file
     * @return
     */
    @POST("/file/upload")
    Call<BaseResult<List<String>>> uploadFiles(@Body RequestBody file);


    /**
     * 登录
     *
     * @param map
     * @return
     */
    @POST("/handheld/login/login")
    Observable<BaseResult<TokenInfo>> requestLogin(@Body Map<String, Object> map);

    /**
     * 获取当前收费员管辖的停车场列表
     * @param map
     * @return
     */
    @GET("/handheld/parking/memberlist")
    Observable<BaseResult<List<ParkingInfo>>> requestParkingList(@QueryMap Map<String, Object> map);


    @GET("/handheld/member/info")
    Observable<BaseResult<UserInfo>> requestUserInfo();

    /**
     * 获取当前登录停车场车位列表
     * @return
     */
    @GET("/handheld/parking/spacelist")
    Observable<BaseResult<List<ParkSpaceInfo>>> requestParkSpaceList();


}
