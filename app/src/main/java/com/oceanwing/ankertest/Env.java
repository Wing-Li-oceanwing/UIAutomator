
package com.oceanwing.ankertest;

import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 环境参数及方法工具类。
 */
public class Env {
    public static String S_ROOT_DISPLAY_FOLDER =
            SDCardPathHelper.getAbsoluteSdcardPath() + "/TestTool/";

    public static final String S_ROOT_LOG_FOLDER = S_ROOT_DISPLAY_FOLDER + "Log/";
    public static final String S_ROOT_RESULT_FOLDER = S_ROOT_DISPLAY_FOLDER + "Result/";
    public static final String S_ROOT_CONFIG_FOLDER = S_ROOT_DISPLAY_FOLDER + "Config/";


    public static File ROOT_M1_FOLDER;
    public static File ROOT_LOG_FOLDER;
    public static File ROOT_RESULT_FOLDER;
    public static File ROOT_CONFIG_FOLDER;


    public static void init() {

        FileUtil.createDir(S_ROOT_DISPLAY_FOLDER);
        FileUtil.createDir(S_ROOT_LOG_FOLDER);
        FileUtil.createDir(S_ROOT_RESULT_FOLDER);
        FileUtil.createDir(S_ROOT_CONFIG_FOLDER);


        ROOT_M1_FOLDER = new File(S_ROOT_DISPLAY_FOLDER);
        ROOT_LOG_FOLDER = new File(S_ROOT_LOG_FOLDER);
        ROOT_RESULT_FOLDER = new File(S_ROOT_RESULT_FOLDER);
        ROOT_CONFIG_FOLDER = new File(S_ROOT_CONFIG_FOLDER);


    }

    /**
     * 是否存在SD卡
     */
    public static boolean isSDCardExist() {
        if (!Environment.getExternalStorageState(
        ).equals(Environment.MEDIA_MOUNTED)) {
            // 对用户只提示一次，以免干扰
            if (!Env.hasSDCardNotExistWarned) {
                Toast.makeText(TestToolApp.getContext(), "sdcard required!", Toast.LENGTH_SHORT).show();
                Env.hasSDCardNotExistWarned = true;
            }
            return false;
        }
        return true;
    }

    private static boolean hasSDCardNotExistWarned = false;

    public static class SDCardPathHelper {

        public static final String CT_S_Sdcard_Sign_Storage_emulated = "storage/emulated/";
        public static final String CT_S_Sdcard_Sign_Storage_sdcard = "storage/sdcard";
        // 根据Nexus5 Android6.01适配
        public static final String CT_S_Sdcard_Sign_Storage_emulated_0 = "storage/emulated/0";
        public static final String CT_S_Sdcard_Sign_sdcard = "sdcard";

        private static String CD_S_SdcardPath = "";
        private static String CD_S_SdcardPathAbsolute = "";

        public static String getSdcardPath() {
            if (TextUtils.isEmpty(CD_S_SdcardPath))
                CD_S_SdcardPath = Environment.getExternalStorageDirectory().getPath();

            CD_S_SdcardPath = checkAndReplaceEmulatedPath(CD_S_SdcardPath);

            return CD_S_SdcardPath;
        }

        public static String getAbsoluteSdcardPath() {
            if (TextUtils.isEmpty(CD_S_SdcardPathAbsolute)) {
                CD_S_SdcardPathAbsolute = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            // 先试试默认的目录，如果创建目录失败再试其他方案
            String testFileName = DisplayUtils.getSaveDateMs();
            File testF = new File(CD_S_SdcardPathAbsolute + "/TestTool/" + testFileName + "/");
            if (testF.mkdirs()) {
                FileUtil.deleteFile(testF);
                return CD_S_SdcardPathAbsolute;
            }

            // 默认路径不可用，尝试其他方案
            CD_S_SdcardPathAbsolute = checkAndReplaceEmulatedPath(CD_S_SdcardPathAbsolute);

            return CD_S_SdcardPathAbsolute;
        }

        public static File getSdcardPathFile() {
            return new File(getSdcardPath());
        }

        public static String checkAndReplaceEmulatedPath(String strSrc) {
            String result = strSrc;
            Pattern p = Pattern.compile("/?storage/emulated/\\d{1,2}");
            Matcher m = p.matcher(strSrc);
            if (m.find()) {
                result = strSrc.replace(CT_S_Sdcard_Sign_Storage_emulated, CT_S_Sdcard_Sign_Storage_sdcard);
                // 如果目录建立失败，最后尝试Nexus5 Android6.01适配
                String testFileName = DisplayUtils.getSaveDateMs();
                File testFile = new File(CD_S_SdcardPathAbsolute + "/TestTool/" + testFileName + "/");
                if (testFile.mkdirs()) {
                    FileUtil.deleteFile(testFile);
                } else {
                    result = strSrc.replace(CT_S_Sdcard_Sign_Storage_emulated_0, CT_S_Sdcard_Sign_sdcard);
                    // test
                    File testF = new File(result + "/TestTool/" + testFileName + "/");
                    if (testF.mkdirs()) {
                        FileUtil.deleteFile(testF);
                    }
                }
            }
            return result;
        }
    }
}
