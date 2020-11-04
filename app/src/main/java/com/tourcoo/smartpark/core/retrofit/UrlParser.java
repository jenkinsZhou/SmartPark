package com.tourcoo.smartpark.core.retrofit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.HttpUrl;
import okhttp3.Request;


/**
 * @author :JenkinsZhou
 * @description :多BaseUrl解析器
 * @company :途酷科技
 * @date 2020年10月29日11:18
 * @Email: 971613168@qq.com
 */
public interface UrlParser {

    /**
     * 将 {@link CommonMultiUrl#mBaseUrlMap} 中映射的 Url 解析成完整的{@link HttpUrl}
     * 用来替换 @{@link Request#url} 里的BaseUrl以达到动态切换 Url的目的
     *
     * @param domainUrl 目标请求(base url)
     * @param url       需要替换的请求(原始url)
     * @return
     */
    HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url);
}
