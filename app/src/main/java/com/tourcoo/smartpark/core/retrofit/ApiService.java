package com.tourcoo.smartpark.core.retrofit;

import com.tourcoo.smartpark.bean.AppUpdateBean;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.TokenInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.bean.settle.SettleDetail;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @PUT("/handheld/member/updatepassword")
    Observable<BaseResult<Object>> requestUpdatePass(@Body Map<String, Object> map);

    /**
     * 车辆登记车位
     * @return
     */
    @POST("/handheld/parking/addparking")
    Observable<BaseResult<Object>> requestAddParkingSpace(@Body Map<String, Object> map);

    /**
     * 获取待收取停车费的车位列表
     * @param map
     * @return
     */
    @GET("/handheld/parking/settlementlist")
    Observable<BaseResult<List<ParkSpaceInfo>>> requestSpaceSettleList(@QueryMap Map<String, Object> map);


    /**
     * 获取某停车位的具体收费信息
     * @param map
     * @return
     */
    @GET("/handheld/parking/settlementinfo")
    Observable<BaseResult<SettleDetail>> requestSpaceSettleDetail(@QueryMap Map<String, Object> map);

    /**
     * 标记欠费接口
     * @param map
     * @return
     */
    @POST("/handheld/parking/signarrears")
    Observable<BaseResult<Object>> requestFlagArrears(@QueryMap Map<String, Object> map);


}
