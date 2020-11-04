package com.tourcoo.smartpark.core.retrofit.repository;

import android.accounts.NetworkErrorException;

import com.tourcoo.smartpark.core.base.entity.BaseResult;
import com.tourcoo.smartpark.core.retrofit.DataNullException;
import com.tourcoo.smartpark.core.retrofit.RetryWhen;
import com.tourcoo.smartpark.core.retrofit.ThreadTransformer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

import static com.tourcoo.smartpark.core.control.RequestConstant.REQUEST_SUCCESS;

/**
 * @author :JenkinsZhou
 * @description :retrofit封装
 * @company :途酷科技
 * @date 2020年10月29日10:54
 * @Email: 971613168@qq.com
 */
public class BaseRepository {

    /**
     * @param observable 用于解析 统一返回实体统一做相应的错误码--如登录失效
     * @param <T>
     * @return
     */
    protected <T> Observable<T> transform(Observable<BaseResult<T>> observable) {
        return ThreadTransformer.switchSchedulers(
                observable.retryWhen(new RetryWhen())
                        .flatMap((Function<BaseResult<T>, ObservableSource<T>>) result -> {
                            if (result.code == REQUEST_SUCCESS) {
                                return result.data != null ? Observable.just(result.data)
                                        : Observable.error(new DataNullException());
                            } else {
                                return Observable.error(new NetworkErrorException());
                            }
                        }));
    }
}
