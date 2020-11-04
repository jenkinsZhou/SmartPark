package com.tourcoo.smartpark.core.delegate;

import android.app.Activity;

import com.apkfuns.logutils.LogUtils;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author :JenkinsZhou
 * @description :代理相关管理类
 * @company :途酷科技
 * @date 2020年10月29日14:25
 * @Email: 971613168@qq.com
 */
public class DelegateManager {
    private static volatile DelegateManager sInstance;

    private DelegateManager() {
    }

    public static DelegateManager getInstance() {
        if (sInstance == null) {
            synchronized (DelegateManager.class) {
                if (sInstance == null) {
                    sInstance = new DelegateManager();
                }
            }
        }
        return sInstance;
    }

    public RefreshDelegate getRefreshDelegate(Class cls) {
        RefreshDelegate delegate = null;
        if (cls != null && mRefreshDelegateMap.containsKey(cls)) {
            delegate = mRefreshDelegateMap.get(cls);
        }
        return delegate;
    }

    /**
     * 装载RefreshDelegate Map对象
     */
    private WeakHashMap<Class, RefreshDelegate> mRefreshDelegateMap = new WeakHashMap<>();
    private WeakHashMap<Class, TitleDelegate> mTitleDelegateMap = new WeakHashMap<>();

    public void putTitleDelegate(Class cls, TitleDelegate titleDelegate) {
        if (cls != null && !mTitleDelegateMap.containsKey(cls)) {
            mTitleDelegateMap.put(cls, titleDelegate);
        }
    }

    public void putRefreshDelegate(Class cls, RefreshDelegate refreshDelegate){
        if (cls != null && !mRefreshDelegateMap.containsKey(cls)) {
            mRefreshDelegateMap.put(cls, refreshDelegate);
        }
    }
    public void removeRefreshDelegate(Class cls) {
        if(mRefreshDelegateMap != null){
            RefreshDelegate delegate = getRefreshDelegate(cls);
            LogUtils.i("removeFastRefreshDelegate_class:" + cls + ";delegate:" + delegate);
            if (delegate != null) {
                delegate.onDestroy();
                mRefreshDelegateMap.remove(cls);
            }
        }
    }

    public void removeTitleDelegate(Class cls){
        if(mTitleDelegateMap != null){
            TitleDelegate delegate = getTitleDelegate(cls);
            LogUtils.i("removeTitleDelegate_class:" + cls + ";delegate:" + delegate);
            if (delegate != null) {
                delegate.onDestroy();
                mTitleDelegateMap.remove(cls);
            }
        }
    }


    public TitleDelegate getTitleDelegate(Class cls) {
        TitleDelegate delegate = null;
        if (cls != null && mTitleDelegateMap.containsKey(cls)) {
            delegate = mTitleDelegateMap.get(cls);
        }
        return delegate;
    }
}
