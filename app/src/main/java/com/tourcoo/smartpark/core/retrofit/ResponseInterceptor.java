package com.tourcoo.smartpark.core.retrofit;


import com.google.gson.Gson;
import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.ui.account.AccountHelper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * @author :JenkinsZhou
 * @description : 拦截器
 * @company :途酷科技
 * @date 2020年11月25日9:40
 * @Email: 971613168@qq.com
 */
public class ResponseInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(final Chain chain) throws IOException {
        // 原始请求
        Request request = chain.request();
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return response;
        }
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        String respString = source.getBuffer().clone().readString(Charset.defaultCharset());
        BaseResult result = new Gson().fromJson(respString, BaseResult.class);
        if (result != null && result.getCode() == 401) {
            ToastUtil.showNormal(result.getErrMsg());
            AccountHelper.getInstance().logout();
        }
        return response;

    }
}
