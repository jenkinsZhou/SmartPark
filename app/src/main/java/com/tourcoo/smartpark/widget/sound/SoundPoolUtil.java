package com.tourcoo.smartpark.widget.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.tourcoo.smartpark.R;

import java.util.HashMap;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年01月07日9:58
 * @Email: 971613168@qq.com
 */
public class SoundPoolUtil {
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 添加的声音资源参数
     */
    private HashMap<Integer, Integer> soundPoolMap;
    /**
     * 声音池
     */
    private SoundPool mSoundPool;
    /**
     * 单例
     */
    private static SoundPoolUtil instance;

    private SoundPoolUtil(Context context) {
        mContext = context;
    }

    public static SoundPoolUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SoundPoolUtil(context);
            instance.init();
        }
        return instance;
    }

    /**
     * 初始化声音
     */
    public void init() {
        //sdk版本21(Android 5.0)是SoundPool 的一个分水岭
        if (Build.VERSION.SDK_INT >= 21) {
            mSoundPool = new SoundPool.Builder().build();
        } else {
            /**
             * 第一个参数：int maxStreams：SoundPool对象的最大并发流数
             * 第二个参数：int streamType：AudioManager中描述的音频流类型
             * 第三个参数：int srcQuality：采样率转换器的质量。 目前没有效果。 使用0作为默认值。
             */
            mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }

        soundPoolMap = new HashMap<>();
        //打卡成功音频文件
        putSound(1, R.raw.ring_di);
      /*  //打卡失败音频文件
        putSound(2, R.raw.kq_fail);*/

    }


    private void putSound(int order, int soundRes) {
        // 上下文，声音资源id，优先级
        soundPoolMap.put(order, mSoundPool.load(mContext, soundRes, 0));
    }

    /**
     * 根据序号播放声音
     *
     * @param order
     */
    public void playSound(int order) {
        mSoundPool.play(
                soundPoolMap.get(order),
                1f,       //左耳道音量【0~1】
                1f,       //右耳道音量【0~1】
                1,        //播放优先级【0表示最低优先级】
                0,        //循环模式【0表示循环一次，-1表示一直循环，
                //其他表示数字+1表示当前数字对应的循环次数】
                1         //播放速度【1是正常，范围从0~2】
        );
    }

    /**
     * 释放内存
     */
    public void release() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
        instance = null;
    }

}
