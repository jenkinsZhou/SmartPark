package com.tourcoo.smartpark.core.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.utils.SizeUtil;

import java.security.MessageDigest;


/**
 * Function: Glide 工具类支持加载常规、圆角、圆形图片
 * Description:
 */
public class GlideManager {

    private static int sCommonPlaceholder = -1;
    private static int sCirclePlaceholder = -1;
    private static int sRoundPlaceholder = -1;

    private static Drawable sCommonPlaceholderDrawable;
    private static Drawable sCirclePlaceholderDrawable;
    private static Drawable sRoundPlaceholderDrawable;
    @ColorInt
    private static int mPlaceholderColor = Color.LTGRAY;
    private static float mPlaceholderRoundRadius = SizeUtil.dp2px(5);

    private static void setDrawable(GradientDrawable gd, float radius) {
        gd.setColor(mPlaceholderColor);
        gd.setCornerRadius(radius);
    }

    /**
     * 设置默认颜色
     *
     * @param placeholderColor
     */
    public static void setPlaceholderColor(@ColorInt int placeholderColor) {
        mPlaceholderColor = placeholderColor;
        sCommonPlaceholderDrawable = new GradientDrawable();
        sCirclePlaceholderDrawable = new GradientDrawable();
        sRoundPlaceholderDrawable = new GradientDrawable();
        setDrawable((GradientDrawable) sCommonPlaceholderDrawable, 0);
        setDrawable((GradientDrawable) sCirclePlaceholderDrawable, 10000);
        setDrawable((GradientDrawable) sRoundPlaceholderDrawable, mPlaceholderRoundRadius);
    }

    /**
     * 设置圆角图片占位背景图圆角幅度
     *
     * @param placeholderRoundRadius
     */
    public static void setPlaceholderRoundRadius(float placeholderRoundRadius) {
        mPlaceholderRoundRadius = placeholderRoundRadius;
        setPlaceholderColor(mPlaceholderColor);
    }

    /**
     * 设置圆形图片的占位图
     *
     * @param circlePlaceholder
     */
    public static void setCirclePlaceholder(int circlePlaceholder) {
        sCirclePlaceholder = circlePlaceholder;
    }

    public static void setCirclePlaceholder(Drawable circlePlaceholder) {
        sCirclePlaceholderDrawable = circlePlaceholder;
    }

    /**
     * 设置正常图片的占位符
     *
     * @param commonPlaceholder
     */
    public static void setCommonPlaceholder(int commonPlaceholder) {
        sCommonPlaceholder = commonPlaceholder;
    }

    public static void setCommonPlaceholder(Drawable commonPlaceholder) {
        sCommonPlaceholderDrawable = commonPlaceholder;
    }

    /**
     * 设置圆角图片的占位符
     *
     * @param roundPlaceholder
     */
    public static void setRoundPlaceholder(int roundPlaceholder) {
        sRoundPlaceholder = roundPlaceholder;
    }

    public static void setRoundPlaceholder(Drawable roundPlaceholder) {
        sRoundPlaceholderDrawable = roundPlaceholder;
    }

    /**
     * 普通加载图片
     *
     * @param obj
     * @param iv
     * @param placeholder
     */
    public static void loadImgCenterCrop(Object obj, ImageView iv, Drawable placeholder) {
        Glide.with(iv.getContext()).load(obj).apply(getRequestOptionsCenterCrop()
                .error(placeholder)
                .placeholder(placeholder)
                .fallback(placeholder)
                .dontAnimate()).into(iv);
    }

    public static void loadImgAuto(Object obj, ImageView iv, Drawable placeholder) {
        Glide.with(iv.getContext()).load(obj).apply(getRequestOptionsAuto()
                .error(placeholder)
                .placeholder(placeholder)
                .fallback(placeholder)
                .dontAnimate()).into(iv);
    }

    public static void loadImgAuto(Object obj, ImageView iv) {
        loadImgAuto(obj, iv, sCommonPlaceholder);
    }

    public static void loadImgAuto(Object obj, ImageView iv, int placeholderResource) {
        Drawable drawable = getDrawable(iv.getContext(), placeholderResource);
        loadImgAuto(obj, iv, drawable != null ? drawable : sCommonPlaceholderDrawable);
    }

    public static void loadImgCenterCrop(Object obj, ImageView iv, int placeholderResource) {
        Drawable drawable = getDrawable(iv.getContext(), placeholderResource);
        loadImgCenterCrop(obj, iv, drawable != null ? drawable : sCommonPlaceholderDrawable);
    }

    public static void loadImgCenterCrop(Object obj, ImageView iv) {
        loadImgCenterCrop(obj, iv, sCommonPlaceholder);
    }

    /**
     * 加载圆形图片
     *
     * @param obj
     * @param iv
     * @param placeholder 占位图
     */
    public static void loadCircleImgCenterCrop(Object obj, ImageView iv, Drawable placeholder) {
        Glide.with(iv.getContext()).load(obj).apply(getRequestOptionsCenterCrop()
                .error(placeholder)
                .placeholder(placeholder)
                .fallback(placeholder)
                .dontAnimate()
                .transform(new CircleCrop())).into(iv);
    }

    public static void loadCircleImgCenterCrop(Object obj, ImageView iv, int placeholderResource) {
        Drawable drawable = getDrawable(iv.getContext(), placeholderResource);
        loadCircleImgCenterCrop(obj, iv, drawable != null ? drawable : sCirclePlaceholderDrawable);
    }

    public static void loadCircleImg(Object obj, ImageView iv) {
        loadCircleImgCenterCrop(obj, iv, sCirclePlaceholder);
    }

    public static void loadRoundImageAuto(Object obj, ImageView iv) {
        loadRoundImgAuto(obj, iv, mPlaceholderRoundRadius);
    }

    /**
     * 加载圆角图片
     *
     * @param obj                 加载的图片资源
     * @param iv
     * @param dp                  圆角尺寸-dp
     * @param placeholder         -占位图
     * @param isOfficial-是否官方模式圆角
     */
    @SuppressWarnings("unchecked")
    public static void loadRoundImgCenterCrop(Object obj, ImageView iv, float dp, Drawable placeholder, boolean isOfficial) {
        Glide.with(iv.getContext()).load(obj).apply(getRequestOptionsCenterCrop())
                .error(placeholder)
                .placeholder(placeholder)
                .fallback(placeholder)
                .dontAnimate()
                .transform(new MultiTransformation(new CenterCrop(), isOfficial ? new RoundedCorners(dp2px(dp)) : new GlideRoundTransform(dp2px(dp)))).into(iv);

    }

    public static void loadRoundImgAuto(Object obj, ImageView iv, float dp, Drawable placeholder, boolean isOfficial) {
        Glide.with(iv.getContext()).load(obj).apply(getRequestOptionsAuto()
                .error(placeholder)
                .placeholder(placeholder)
                .fallback(placeholder)
                .dontAnimate()
                .transform(isOfficial ? new RoundedCorners(dp2px(dp)) : new GlideRoundTransform(dp2px(dp)))).into(iv);
    }

    public static void loadRoundImgCenterCrop(Object obj, ImageView iv, float dp, int placeholderResource, boolean isOfficial) {
        Drawable drawable = getDrawable(iv.getContext(), placeholderResource);
        loadRoundImgCenterCrop(obj, iv, dp, drawable != null ? drawable : sRoundPlaceholderDrawable, isOfficial);
    }

    public static void loadRoundImgAuto(Object obj, ImageView iv, float dp, int placeholderResource, boolean isOfficial) {
        Drawable drawable = getDrawable(iv.getContext(), placeholderResource);
        loadRoundImgAuto(obj, iv, dp, drawable != null ? drawable : sRoundPlaceholderDrawable, isOfficial);
    }

    public static void loadRoundImgAuto(Object obj, ImageView iv, float dp, boolean isOfficial) {
        loadRoundImgAuto(obj, iv, dp, sRoundPlaceholder, isOfficial);
    }


    public static void loadRoundImg(Object obj, ImageView iv, float dp, boolean isOfficial) {
        loadRoundImgCenterCrop(obj, iv, dp, sRoundPlaceholder, isOfficial);


    }

    public static void loadRoundImg(Object obj, ImageView iv, float dp) {
        loadRoundImg(obj, iv, dp, true);
    }

    public static void loadRoundImgAuto(Object obj, ImageView iv, float dp) {
        loadRoundImgAuto(obj, iv, dp, true);
    }

    public static void loadRoundImgAuto(Object obj, ImageView iv, boolean isOfficial) {
        loadRoundImg(obj, iv, mPlaceholderRoundRadius, isOfficial);
    }

    public static void loadRoundImg(Object obj, ImageView iv, boolean isOfficial) {
        loadRoundImg(obj, iv, mPlaceholderRoundRadius, isOfficial);
    }

    public static void loadRoundImg(Object obj, ImageView iv) {
        loadRoundImg(obj, iv, true);
    }

    public static void loadRoundImgAuto(Object obj, ImageView iv) {
        loadRoundImg(obj, iv, true);
    }

    private static RequestOptions getRequestOptionsCenterCrop() {
        RequestOptions requestOptions = new RequestOptions()
                // 填充方式
                .centerCrop()
                //优先级
                .priority(Priority.HIGH)
                //缓存策略
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        return requestOptions;
    }

    private static RequestOptions getRequestOptionsAuto() {
        RequestOptions requestOptions = new RequestOptions()
                //优先级
                .priority(Priority.HIGH)
                //缓存策略
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        return requestOptions;
    }

    private static int dp2px(float dipValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private static Drawable getDrawable(Context context, @DrawableRes int res) {
        Drawable drawable = null;
        try {
            drawable = context.getResources().getDrawable(res);
        } catch (Exception e) {

        }
        return drawable;
    }

    private static class GlideRoundTransform extends BitmapTransformation {
        int radius;

        public GlideRoundTransform(int dp) {
            super();
            this.radius = dp;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return roundCrop(pool, toTransform);
        }

        private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
            Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
            canvas.drawRoundRect(rectF, radius, radius, paint);
            return result;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {

        }
    }
}