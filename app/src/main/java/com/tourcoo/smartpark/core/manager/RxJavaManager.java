package com.tourcoo.smartpark.core.manager;

import com.tourcoo.smartpark.core.retrofit.CommonTransformer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月27日17:23
 * @Email: 971613168@qq.com
 */
public class RxJavaManager {
    public interface TimerListener {
        /**
         * 倒计时结束回调
         */
        void timeEnd();
    }

    private static volatile RxJavaManager instance;

    private RxJavaManager() {
    }

    public static RxJavaManager getInstance() {
        if (instance == null) {
            synchronized (RxJavaManager.class) {
                if (instance == null) {
                    instance = new RxJavaManager();
                }
            }
        }
        return instance;
    }

    /**
     * 创建Observable
     *
     * @param value
     * @param delay
     * @param unit
     * @param <T>
     * @return
     */
    public <T> Observable<T> getDelayObservable(T value, long delay, TimeUnit unit) {
        return Observable.just(value)
                .delay(delay, unit)
                .compose(CommonTransformer.<T>switchSchedulers());
    }

    /**
     * 创建 时延单位秒的Observable
     *
     * @param value
     * @param delay
     * @param <T>
     * @return
     */
    public <T> Observable<T> getDelayObservable(T value, long delay) {
        return getDelayObservable(value, delay, TimeUnit.SECONDS);
    }

    /**
     * 设置时延为毫秒的定时器
     *
     * @param delayTime
     * @return
     */
    public Observable<Long> setTimer(long delayTime) {
        return getDelayObservable(delayTime, delayTime, TimeUnit.MILLISECONDS);
    }


    /**
     * 每隔milliseconds毫秒 执行一次命令
     *
     * @param milliseconds
     * @param observable
     */
    public void doEventByInterval(long milliseconds, Observer<Long> observable) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observable);
    }
}
