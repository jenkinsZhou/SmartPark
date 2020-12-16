package com.tourcoo.smartpark.core.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.SmartParkApplication;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.UiManager;
import com.tourcoo.smartpark.util.StringUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author :JenkinsZhou
 * @description :
 * @company :途酷科技
 * @date 2020年10月29日11:26
 * @Email: 971613168@qq.com
 */
public class FileUtil {

    /**
     * 获取系统缓存路径
     *
     * @return
     */
    public static String getCacheDir() {
        Context context = UiManager.getInstance().getApplication().getApplicationContext();
        //storage/emulated/0/Android/data/<package-name>/cache/xxx/
        File cacheDir = context.getExternalCacheDir();
        String fileDir = context.getString(R.string.external_cache_dir);
        if (cacheDir == null) {
            //data/data/<package-name>/cache/xxx/
            cacheDir = context.getCacheDir();
            fileDir = context.getString(R.string.cache_dir);
        }
        //该路径需和fast_file_path配置一致
        cacheDir = new File(cacheDir, fileDir);
        return cacheDir.getAbsolutePath();
    }

    /**
     * 获取Environment.getExternalStorageDirectory()目录--注意读写SD卡权限{android.permission.WRITE_EXTERNAL_STORAGE}
     * 即:/storage/emulated/0/xxx/
     *
     * @return
     */
    public static String getExternalStorageDirectory() {
        Context context = UiManager.getInstance().getApplication().getApplicationContext();
        File file = new File(Environment.getExternalStorageDirectory().toString());
        String fileDir = context.getString(R.string.external_storage_directory);
        //该路径需和fast_file_path配置一致
        file = new File(file, fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    /**
     * 获取context.getFilesDir()
     * /data/data/<package-name>/files/xxx/
     *
     * @return
     */
    public static String getFilesDir() {
        Context context = UiManager.getInstance().getApplication().getApplicationContext();
        File file = context.getFilesDir();
        String fileDir = context.getString(R.string.files_dir);
        //该路径需和fast_file_path配置一致
        file = new File(file, fileDir);
        return file.getAbsolutePath();
    }

    /**
     * 获取 context.getExternalFilesDir
     * /storage/emulated/0/Android/data/<package_name>/files/xxx/
     *
     * @return
     */
    public static String getExternalFilesDir() {
        Context context = UiManager.getInstance().getApplication().getApplicationContext();
        File file = context.getExternalFilesDir(null);
        String fileDir = context.getString(R.string.external_files_dir);
        //该路径需和fast_file_path配置一致
        file = new File(file, fileDir);
        return file.getAbsolutePath();
    }

    /**
     * 安装apk 包路径在FastFileProvider 配置路径下
     *
     * @param apkPath apk 文件对象
     */
    public static void installApk(File apkPath) {
        Context context = UiManager.getInstance().getApplication().getApplicationContext();
        if (context == null || apkPath == null) {
            return;
        }
        installApk(apkPath, context.getPackageName() + ".SmartParkFileProvider");
    }

    /**
     * 安装App 使用lib FileProvider
     * 使用{@link #getCacheDir()} ()}创建文件包
     *
     * @param apkPath apk 文件对象
     */
    public static void installApk(File apkPath, @NonNull String authority) {
        if (apkPath == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // context 使用startActivity需增加 FLAG_ACTIVITY_NEW_TASK TAG 否则低版本上(目前发现在7.0以下版本)会提示以下错误
        //android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri apkUri;
        //判断版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //加入provider
            apkUri = FileProvider.getUriForFile(SmartParkApplication.getContext(), authority, apkPath);
            //授予一个URI的临时权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            LogUtils.i("apkUri=" + apkUri);
        } else {
            apkUri = Uri.fromFile(apkPath);
            LogUtils.i("apkUri=" + apkUri);
        }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        SmartParkApplication.getContext().startActivity(intent);
    }


    public static Uri pathToUri(String path) {
        Uri uri;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            uri = Uri.fromFile(new File(path));
        } else {
            /**
             * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
             * 并且这样可以解决MIUI系统上拍照返回size为0的情况
             */
            uri = FileProvider.getUriForFile(SmartParkApplication.getContext(), getAuthority(), new File(path));
        }

        return uri;
    }

    /**
     * 使用第三方qq文件管理器打开
     *
     * @param uri
     * @return
     */
    public static boolean isQQMediaDocument(Uri uri) {
        return "com.tencent.mtt.fileprovider".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getAuthority() {
        return SmartParkApplication.getContext().getPackageName() + ".SmartParkFileProvider";
    }

    public static boolean isFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        return new File(filePath).isFile();
    }


    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }


    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }


    /**
     * 获取此Uri的数据列的值。这对于MediaStore uri和其他基于文件的内容提供程序非常有用。
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException e) {
            //do nothing
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    /**
     * 全平台处理方法
     */
    public static String getPath(final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        final boolean isN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        if (isN) {
            return getFilePathForN(SmartParkApplication.getContext(), uri);
        }
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(SmartParkApplication.getContext(), uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), StringUtil.parseToLong(id));
                return getDataColumn(SmartParkApplication.getContext(), contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(SmartParkApplication.getContext(), contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(SmartParkApplication.getContext(), uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * android7.0以上处理方法
     */
    private static String getFilePathForN(Context context, Uri uri) {
        try {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = (returnCursor.getString(nameIndex));
            File file = new File(context.getFilesDir(), name);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            returnCursor.close();
            inputStream.close();
            outputStream.close();
            return file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
