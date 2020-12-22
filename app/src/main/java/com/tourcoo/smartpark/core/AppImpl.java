package com.tourcoo.smartpark.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.kingja.loadsir.core.LoadService;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.base.WebAppActivity;
import com.tourcoo.smartpark.core.control.IRefreshLoadView;
import com.tourcoo.smartpark.core.control.IRefreshView;
import com.tourcoo.smartpark.core.control.LoadMoreFoot;
import com.tourcoo.smartpark.core.control.LoadingDialog;
import com.tourcoo.smartpark.core.control.MultiStatusView;
import com.tourcoo.smartpark.core.control.ObserverControl;
import com.tourcoo.smartpark.core.control.QuitAppControl;
import com.tourcoo.smartpark.core.control.RecyclerViewControl;
import com.tourcoo.smartpark.core.control.TitleBarViewControl;
import com.tourcoo.smartpark.core.control.ToastControl;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.utils.DrawableUtil;
import com.tourcoo.smartpark.core.utils.SizeUtil;
import com.tourcoo.smartpark.core.utils.StackUtil;
import com.tourcoo.smartpark.core.utils.StatusBarUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;
import com.tourcoo.smartpark.core.widget.CommonLoadMoreView;
import com.tourcoo.smartpark.core.widget.dialog.loading.IosLoadingDialog;
import com.tourcoo.smartpark.core.widget.dialog.loading.LoadingDialogWrapper;
import com.tourcoo.smartpark.core.widget.view.radius.RadiusTextView;
import com.tourcoo.smartpark.core.widget.view.titlebar.TitleBarView;

import io.reactivex.Observable;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日10:17
 * @Email: 971613168@qq.com
 */
public class AppImpl implements DefaultRefreshHeaderCreator, LoadMoreFoot, RecyclerViewControl, MultiStatusView, LoadingDialog, TitleBarViewControl, QuitAppControl, ToastControl, ObserverControl {

    private Context mContext;
    private String TAG = this.getClass().getSimpleName();

    public AppImpl(@Nullable Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
        layout.setEnableHeaderTranslationContent(true)
                .setEnableOverScrollDrag(true);
        return new ClassicsHeader(mContext);
    }

    @Nullable
    @Override
    public LoadMoreView createDefaultLoadMoreView(BaseQuickAdapter adapter) {
        if (adapter != null) {
            //设置动画是否一直开启
            adapter.isFirstOnly(false);
        }
        //方式一:设置FastLoadMoreView--可参考FastLoadMoreView.Builder相应set方法
        //默认配置请参考FastLoadMoreView.Builder(mContext)里初始化
        return new CommonLoadMoreView.Builder(mContext)
                .setLoadingTextFakeBold(true)
                .setLoadingSize(SizeUtil.dp2px(20))
//                                .setLoadTextColor(Color.MAGENTA)
//                                //设置Loading 颜色-5.0以上有效
//                                .setLoadingProgressColor(Color.MAGENTA)
//                                //设置Loading drawable--会使Loading颜色失效
//                                .setLoadingProgressDrawable(R.drawable.dialog_loading_wei_bo)
//                                //设置全局TextView颜色
//                                .setLoadTextColor(Color.MAGENTA)
//                                //设置全局TextView文字字号
//                                .setLoadTextSize(SizeUtil.dp2px(14))
//                                .setLoadingText("努力加载中...")
//                                .setLoadingTextColor(Color.GREEN)
//                                .setLoadingTextSize(SizeUtil.dp2px(14))
//                                .setLoadEndText("我是有底线的")
//                                .setLoadEndTextColor(Color.GREEN)
//                                .setLoadEndTextSize(SizeUtil.dp2px(14))
//                                .setLoadFailText("哇哦!出错了")
//                                .setLoadFailTextColor(Color.RED)
//                                .setLoadFailTextSize(SizeUtil.dp2px(14))
                .build();
        //方式二:使用adapter自带--默认设置的和这个基本一致只是提供了相应设置方法
//                        return new SimpleLoadMoreView();
        //方式三:参考SimpleLoadMoreView或CommonLoadMoreView完全自定义自己的LoadMoreView
//                        return MyLoadMoreView();
    }

    @Override
    public void setRecyclerView(RecyclerView recyclerView, Class<?> cls) {

    }



    @Nullable
    @Override
    public LoadingDialogWrapper createLoadingDialog(@Nullable Activity activity) {
        return new LoadingDialogWrapper(activity, new IosLoadingDialog(activity,""));
    }

    @Override
    public boolean createTitleBarViewControl(TitleBarView titleBar, Class<?> cls) {
        //默认的MD风格返回箭头icon如使用该风格可以不用设置
        Drawable mDrawable = DrawableUtil.setTintDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_back),
                ContextCompat.getColor(mContext, R.color.colorTitleText));
        //是否支持状态栏白色
        boolean isSupport = StatusBarUtil.isSupportStatusBarFontChange();
        boolean isActivity = Activity.class.isAssignableFrom(cls);
        Activity activity = StackUtil.getInstance().getActivity(cls);
        //设置TitleBarView 所有TextView颜色
   /*     titleBar.setStatusBarLightMode(isSupport)
                //不支持黑字的设置白透明
                .setStatusAlpha(isSupport ? 0 : 102)
                .setLeftTextDrawable(isActivity ? mDrawable : null)
                .setDividerHeight(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? SizeUtil.dp2px(0.5f) : 0);*/
        if (activity != null) {
            titleBar.setTitleMainText(activity.getTitle())
                    .setOnLeftTextClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.finish();
                        }
                    });
        }
        if (activity instanceof WebAppActivity) {
            return false;
        }
        //设置海拔效果
//        ViewCompat.setElevation(titleBar, mContext.getResources().getDimension(R.dimen.dp_elevation));
        return false;
    }

    @Override
    public long quipApp(boolean isFirst, Activity activity) {
        //默认配置
        if (isFirst) {
            ToastUtil.showNormal(R.string.quit_app);
        } else {
            StackUtil.getInstance().exit(false);
        }
        return 2000;
    }

    @Override
    public Toast getToast() {
        return null;
    }

    @Override
    public void setToast(Toast toast, RadiusTextView textView) {

    }

    /**
     * @param o {@link BaseObserver} 对象用于后续事件逻辑
     * @param e 原始错误
     * @return true 拦截操作不进行原始{@link BaseObserver#onError(Throwable)}后续逻辑
     * false 不拦截继续后续逻辑
     * {@link com.tourcoo.smartpark.core.retrofit.DataNullException} 已在{@link BaseObserver#onError} ｝处理如果为该类型Exception可不用管,参考
     * {@link BaseObserver#transform(Observable)} 处理逻辑
     */
    @Override
    public boolean onError(BaseObserver o, Throwable e) {
        return false;
    }

    @Override
    public void setMultiStatusView(LoadService loadService, IRefreshView iRefreshView) {

    }
}
