package com.tourcoo.smartpark.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.Signature;
import android.util.Log;

import java.security.MessageDigest;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月04日11:35
 * @Email: 971613168@qq.com
 */
public class SignTool {
    private static final String TAG = "SignTool";

    //调用示例
    //SignTool.printSignatureMD5(CHAuthService.this,"com.sccngitv.dvb");


    public static String getSignatureMD5(Context mContext, String packageName) {
        try {
           return getMD5MessageDigest(mContext, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            return "未知";
        }
    }

    public static String getMD5MessageDigest(Context mContext, String str) {
        try {
            int i = 0;
            @SuppressLint("WrongConstant") Signature signature = mContext.getPackageManager().getPackageInfo(str, 64).signatures[0];
            MessageDigest instance = MessageDigest.getInstance("md5");
            instance.update(signature.toByteArray());
            byte[] digest = instance.digest();
            StringBuilder stringBuilder = new StringBuilder();
            int length = digest.length;
            while (i < length) {
                String toHexString = Integer.toHexString(digest[i] & 255);
                if (toHexString.length() == 1) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("0");
                    stringBuilder2.append(toHexString);
                    toHexString = stringBuilder2.toString();
                }
                stringBuilder.append(toHexString);
                i++;
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }
}
