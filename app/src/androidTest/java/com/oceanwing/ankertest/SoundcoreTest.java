package com.oceanwing.ankertest;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import com.elvishew.xlog.XLog;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.lang.Thread.sleep;

@RunWith(AndroidJUnit4.class)
public class SoundcoreTest {

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
        filePath = Env.S_ROOT_RESULT_FOLDER + df.format(new Date()) + "_Soundcore.xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("DashCamTest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("是否检测到新版本");
            row.createCell(2).setCellValue("下载ota结果");
            row.createCell(3).setCellValue("升级ota结果");
            row.createCell(4).setCellValue("结束时间");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    @Test
    public void otaTest() throws IOException, InterruptedException {

        for (count =1; count <10000; count++) {
            XLog.i( String.format(Locale.CHINA, "------第%d次执行OTA测试------", count));

            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(count);
            row.createCell(0).setCellValue(count);
            mDevice.executeShellCommand("am force-stop com.oceanwing.soundcore");
            XLog.i( "kill the app..." );
            sleep(5000);

            mDevice.executeShellCommand("am start -n com.oceanwing.soundcore/.activity.WelcomeActivity");
            XLog.i( "am start the app" );
            sleep(5000);

            boolean isWaitUpdate = waitForUiObjectRes("com.oceanwing.soundcore:id/st_positive", 60);
            if (isWaitUpdate){
                //执行点击更多
                UiObject more = new UiObject(new UiSelector().resourceId("com.oceanwing.soundcore:id/st_positive"));
                try {
                    more.clickAndWaitForNewWindow();
                    XLog.i( "点击more" );
                    row.createCell( 1 ).setCellValue( "pass" );
                    boolean isDownload = waitForUiObjectRes("com.oceanwing.soundcore:id/sb_download", 10, "下载");
                    if (isDownload) {
                        sleep( 1000 );
                        UiObject download = new UiObject( new UiSelector().resourceId( "com.oceanwing.soundcore:id/sb_download" ).text( "下载" ) );
                        download.clickAndWaitForNewWindow();
                        XLog.i( "点击下载" );
                    }else {
                        XLog.i( "未发现新版本" );
                    }
                    boolean isUpdate = waitForUiObjectRes("com.oceanwing.soundcore:id/sb_download", 120, "安装");
                    if (isUpdate){
                        sleep( 1000 );
                        row.createCell(2 ).setCellValue("pass");
                        UiObject update = new UiObject( new UiSelector().resourceId(  "com.oceanwing.soundcore:id/sb_download" ).text( "安装" ) );
                        update.clickAndWaitForNewWindow();
                        XLog.i( "点击安装" );

                        boolean isFinish = waitForUiObjectRes("com.oceanwing.soundcore:id/sb_bluetooth2", 240);
                        if (isFinish){
                            row.createCell(3 ).setCellValue("pass");
                            row.createCell(4 ).setCellValue(DisplayUtils.getSaveTime());
                        }
                    }else {
                        row.createCell(2 ).setCellValue("fail");
                        XLog.i( "下载失败" );
                        row.createCell(3 ).setCellValue("fail");
                        row.createCell(4 ).setCellValue(DisplayUtils.getSaveTime());
                    }

                    XLog.e ("测试完成，杀死APP，继续测试");
                    mDevice.pressHome();
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                    XLog.e( "点击按键出现异常" );
                }
            }else {
                row.createCell( 1 ).setCellValue( "fail" );
            }

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }


    public boolean waitForUiObjectRes(String res, int time_t) {//等待对象出现
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
            if (time > time_t*1000) {
                return false;
            }
        }
    }


    public boolean waitForUiObjectRes(String res, int time_t, String text) {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UiObject it = new UiObject(new UiSelector().resourceId(res).text(text));
            if (it.exists()) {
                return true;
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > time_t*1000) {
                return false;
            }
        }
    }

    public boolean waitForUiObject() {//等待对象出现
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
