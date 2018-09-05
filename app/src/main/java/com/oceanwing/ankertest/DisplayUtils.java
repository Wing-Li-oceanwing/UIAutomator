

package com.oceanwing.ankertest;

import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DisplayUtils {
    /**
     * 获得系统时间
     *
     * @return
     */
    private static SimpleDateFormat simpleTimeFormat =
            new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static String getSystemTime() {
        Date date = new Date();
        return simpleTimeFormat.format(date);
    }

    public static String getSystemTime(long date) {
        return simpleTimeFormat.format(new Date(date));
    }

    // 获取系统短日期时间
    private static SimpleDateFormat simpleDateTimeFormat =
            new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    public static String getSystemDateTime() {
        Date date = new Date();
        return simpleDateTimeFormat.format(date);
    }

    public static String getSystemDateTime(long date) {
        return simpleDateTimeFormat.format(new Date(date));
    }

    // GPS使用的日期格式
    private static SimpleDateFormat gpsDataFormatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static String getGpsSaveTime() {
        Date date = new Date();
        return gpsDataFormatter.format(date);
    }

    public static String getGpsSaveTime(long data) {
        return gpsDataFormatter.format(new Date(data));
    }

    public static String getGpsSaveTime(Date date) {
        return gpsDataFormatter.format(date);
    }

    // 供外部模块做保存操作时引用的日期格式转换器
    private static SimpleDateFormat saveFormatter =
            new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    public static String getSaveTime() {
        Date date = new Date();
        return saveFormatter.format(date);
    }

    public static String getSaveTime(long data) {
        return saveFormatter.format(new Date(data));
    }

    // 日期，到ms
    private static SimpleDateFormat saveDateMsFormatter =
            new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);

    public static String getSaveDateMs() {
        Date date = new Date();
        return saveDateMsFormatter.format(date);
    }

    public static String getSaveDateMs(long data) {
        return saveDateMsFormatter.format(new Date(data));
    }

    // 日期，到s
    private static SimpleDateFormat saveDateFormatter =
            new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);

    public static String getSaveDate() {
        Date date = new Date();
        return saveDateFormatter.format(date);
    }

    public static String getSaveDate(long data) {
        return saveDateFormatter.format(new Date(data));
    }

    // 日期，到日
    private static SimpleDateFormat dateFormatter =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public static String getDate() {
        Date date = new Date();
        return dateFormatter.format(date);
    }

    public static String getDate(long data) {
        return dateFormatter.format(new Date(data));
    }

    /**
     * 是否存在SD卡
     */
    public static boolean isSDCardExist() {
        if (!android.os.Environment.getExternalStorageState(
        ).equals(android.os.Environment.MEDIA_MOUNTED)) {
            // 对用户只提示一次，以免干扰
            if (!hasSDCardNotExistWarned) {
                openToast("保存内容请先插入sdcard!!!");
                hasSDCardNotExistWarned = true;
            }
            return false;
        }
        return true;
    }

    private static boolean hasSDCardNotExistWarned = false;

    /**
     * toast提示
     * 该方法在OET尚未完成初始化时调用会有异常：Caused by: java.lang.RuntimeException:
     * Can't create handler inside thread that has not called Looper.prepare()，所以
     * 需要try..catch保护
     *
     * @param message
     */
    private static void openToast(String message) {
        try {
            Toast toast = Toast.makeText(TestToolApp.getContext(), message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (Exception e) {
            Log.e("NetWorkUtils.openToast", "Toast when OET App not inited.");
        }
    }
}
