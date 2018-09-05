package com.oceanwing.ankertest;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.util.Log;

import com.elvishew.xlog.XLog;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.Thread.sleep;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DashCamTest {

    private static final String BASIC_SAMPLE_PACKAGE =  "com.zhixin.roav.cam";
    private UiDevice mDevice = null;
    private Context context = null;
    private int count = 0;
    private String filePath;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getTargetContext();
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");//设置日期格式
        filePath = Env.S_ROOT_RESULT_FOLDER + df.format(new Date()) + ".xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("DashCamTest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("WIFI连接");
            row.createCell(2).setCellValue("视频列表刷新");
            row.createCell(3).setCellValue("刷新耗时(ms)");
            row.createCell(4).setCellValue("WIFI连接");
            row.createCell(5).setCellValue("点击setting");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    @Test
    public void videoTest() throws InterruptedException, IOException {
        count ++;
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(new FileInputStream(filePath));
        } catch (IOException e) {
            XLog.e("无法创建文件");
            e.printStackTrace();
        }
        assert wb != null;
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.createRow(count);
        row.createCell(0).setCellValue(count);
        XLog.i("TestCount--------->" + count);

        boolean connect = waitForUiObjectIsClickable();

        Date curDate = null;

        if (connect) {
            curDate = new Date(System.currentTimeMillis());
            UiObject2 videos = mDevice.findObject(By.res("com.zhixin.roav.cam:id/v_dashcam_containt"));
            row.createCell(1).setCellValue("pass");
            videos.clickAndWait(Until.newWindow(), 2000);
            XLog.i("WIFI是连上的，点击查看视频");
        }else {
            row.createCell(1).setCellValue("fail");
            XLog.i("WIFI 1分钟内未连上的");
        }

        boolean flag = waitForUiObjectRes("com.zhixin.roav.cam:id/img_video_thumb");
        if (flag) {
            XLog.i("刷新出了视频列表");
            row.createCell(2).setCellValue("pass");
            Date endDate = new Date(System.currentTimeMillis());
            long diff = endDate.getTime() - curDate.getTime();
            row.createCell(3).setCellValue(diff);

            mDevice.pressHome();

            sleep(60000);

            final Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);

            // Clear out any previous instances
            assert intent != null;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            XLog.i("拉起app，等待WIFI是否连接");

            connect = waitForUiObjectIsClickable();

            if (connect) {

                XLog.i("WIFI连接成功");
                row.createCell(4).setCellValue("pass");
                sleep(1000);
                pressSetting();
                XLog.i("点击setting等待1s");
                row.createCell(5).setCellValue("pass");
                sleep(1000);
                mDevice.pressHome();
                XLog.i("点击返回home，等待5s");
                sleep(60000);
                context.startActivity(intent);
                XLog.i("拉起app，等待1s");
                sleep(1000);
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                videoTest();

            } else {
                XLog.i("WIFI在1分钟内都未连上");
                row.createCell(4).setCellValue("fail");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
            }
        } else {
            XLog.i("50s未刷新出了视频列表");
            row.createCell(2).setCellValue("fail");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    private void pressSetting() throws InterruptedException {
        UiObject setting = new UiObject(new UiSelector().resourceId("com.zhixin.roav.cam:id/img_setting"));
        try {
            setting.clickAndWaitForNewWindow( 2000);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        sleep(1000);
    }

    private boolean isWifiConnected() throws UiObjectNotFoundException {
        UiObject setting = new UiObject(new UiSelector().resourceId("com.zhixin.roav.cam:id/img_setting"));
        XLog.i(String.format(Locale.CHINA, "wifi 连接状态:%b", setting.isCheckable()));
        return setting.isCheckable();
    }


    public boolean waitForUiObjectRes(String res) {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UiObject it = new UiObject(new UiSelector().resourceId(res));
            if (it.exists()) {
                return true;
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > 500000) {
                return false;
            }
        }
    }

    public boolean waitForUiObjectIsClickable() {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //com.zhixin.roav.cam:id/v_setting_container
            UiObject it = new UiObject(new UiSelector().resourceId("com.zhixin.roav.cam:id/img_setting"));

            try {
                if (it.isEnabled()) {
                    return true;
                }
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > 60000) {
                return false;
            }
        }
    }

}
