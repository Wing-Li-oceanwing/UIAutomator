package com.oceanwing.ankertest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
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

import static java.lang.Thread.sleep;

/**
 * Created by MD01 on 2017/9/6.
 */
@RunWith(AndroidJUnit4.class)
public class F2OTATest {
    private UiDevice mDevice = null;
    private static final String BASIC_SAMPLE_PACKAGE = "com.chipsguide.app.roav.fmplayer";
    private int count = 0;
    private String filePath;
    private Context context;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getContext();

        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
        filePath = Env.S_ROOT_RESULT_FOLDER + df.format(new Date()) + ".xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("BluetoothTest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("蓝牙连接状态");
            row.createCell(2).setCellValue("Setting页面");
            row.createCell(3).setCellValue("About页面");
            row.createCell(4).setCellValue("Firmware upgrade页面");
            row.createCell(5).setCellValue("Update now页面");
            row.createCell(6).setCellValue("success?");
            row.createCell(7).setCellValue("time");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }

    }

    @Test
    public void otaUpdateTest() throws Exception {
        XLog.d( "Start update test");

        while (true) {
            count ++;

            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(count);
            row.createCell(0).setCellValue(count);
            XLog.d (String.format ("第%d次OTA升级", count));

            boolean connnect = connectBle();

            if (connnect) {
                XLog.d ("蓝牙连接正常");
                row.createCell (1).setCellValue ("pass");
                boolean setting = pressSetting ();
                if (setting){
                    XLog.d ("Setting 页面正常");
                    row.createCell (2).setCellValue ("pass");

                    boolean about = pressAbout ();
                    if (about){
                        XLog.d ("About 页面正常");
                        row.createCell (3).setCellValue ("pass");

                        boolean pressUpgrade = pressUpgrade();
                        if (pressUpgrade){
                            XLog.d ("Upgrade 页面正常");
                            row.createCell (4).setCellValue ("pass");

                            boolean updateNow = pressUpdateNow();
                            if (updateNow){
                                XLog.d ("Upgrade now 页面正常");
                                row.createCell (5).setCellValue ("pass");

                                pressOK ();

                                boolean result = waitForUiObjectText ("Update Complete");

                                row.createCell(7).setCellValue(DisplayUtils.getSaveDate());

                                sleep(500);
                                if (result) {
                                    XLog.d ("Upgrade success");
                                    row.createCell (6).setCellValue ("pass");
                                    sleep (3000);
                                    pressOK();
                                }else {
                                    XLog.d ("Upgrade fail");
                                    row.createCell (6).setCellValue ("fail");
                                    sleep (2000);
                                    mDevice.pressBack ();
                                }
                            }else {
                                XLog.d ("Upgrade now 页面异常");
                                row.createCell (5).setCellValue ("fail");
                            }
                        }else {
                            XLog.d ("Upgrade 页面异常");
                            row.createCell (4).setCellValue ("fail");
                        }

                    }else {
                        XLog.d ("About 页面异常");
                        row.createCell (3).setCellValue ("fail");
                    }
                }else {
                    XLog.d ("Setting 页面异常");
                    row.createCell (2).setCellValue ("fail");
                }

            }else {
                XLog.d ("蓝牙连接异常");
                row.createCell (1).setCellValue ("fail");
            }

//                Runtime.getRuntime().exec("am force-stop com.chipsguide.app.roav.fmplayer").waitFor();
            mDevice.executeShellCommand("am force-stop com.chipsguide.app.roav.fmplayer");
            sleep(5000);
//            Runtime.getRuntime().exec("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity").waitFor();
            mDevice.executeShellCommand("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity");
            sleep(3000);

            XLog.e ("测试完成，杀死APP，继续测试");
            XLog.e ("----------------------------------------");

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();

            sleep (50000);

        }
    }

    private void pressOK () throws UiObjectNotFoundException {
        UiObject tv_btn = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_btn"));
        tv_btn.clickAndWaitForNewWindow();
    }

    private boolean pressUpdateNow () throws UiObjectNotFoundException {
        UiObject tv_update_now = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_update_now"));
        tv_update_now.clickAndWaitForNewWindow();
        return mDevice.wait (Until.hasObject (By.res ("com.chipsguide.app.roav.fmplayer:id/tv_btn")), 1000);
    }

    private boolean pressSetting() throws UiObjectNotFoundException {
        UiObject setting = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tab_setting_rb"));
        setting.clickAndWaitForNewWindow();
        return mDevice.wait (Until.hasObject (By.res ("com.chipsguide.app.roav.fmplayer:id/rl_about")), 1000);
    }

    private boolean pressUpgrade() throws UiObjectNotFoundException {
        UiObject setting = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/rl_hardware_version"));
        setting.clickAndWaitForNewWindow();
        return mDevice.wait (Until.hasObject (By.res ("com.chipsguide.app.roav.fmplayer:id/tv_update_now")), 1000);
    }

    private boolean pressAbout() throws UiObjectNotFoundException {
        UiObject rl_about = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/rl_about"));
        rl_about.clickAndWaitForNewWindow();
        return mDevice.wait (Until.hasObject (By.res ("com.chipsguide.app.roav.fmplayer:id/rl_hardware_version")), 1000);
    }

    private boolean connectBle() throws InterruptedException, UiObjectNotFoundException {
        int connectCount = 0;
        if (mDevice.wait(Until.hasObject(By.res ("com.chipsguide.app.roav.fmplayer:id/tv_ok")),1000)){
            UiObject tv_ok = new UiObject (new UiSelector().resourceId ("com.chipsguide.app.roav.fmplayer:id/tv_ok"));
            tv_ok.click();
        }
        sleep(1000);

        if (!mDevice.wait(Until.hasObject(By.res ("com.chipsguide.app.roav.fmplayer:id/tv_has_connected")),1000)){
            if (mDevice.wait(Until.hasObject(By.res ("com.chipsguide.app.roav.fmplayer:id/tab_equipment_rb")),1000)){
                UiObject equipment = new UiObject (new UiSelector().resourceId ("com.chipsguide.app.roav.fmplayer:id/tab_equipment_rb"));
                equipment.clickAndWaitForNewWindow ();
            }else if (mDevice.wait(Until.hasObject(By.res ("com.chipsguide.app.roav.fmplayer:id/pb_connect_device")),1000)){
                UiObject pb_connect_device = new UiObject (new UiSelector().resourceId ("com.chipsguide.app.roav.fmplayer:id/pb_connect_device"));
                pb_connect_device.clickAndWaitForNewWindow ();
                sleep (5000);
                XLog.d ("点击重连蓝牙按钮");
            }
            else {
                try {
                    mDevice.executeShellCommand("am force-stop com.chipsguide.app.roav.fmplayer");
                    sleep(5000);
                    mDevice.executeShellCommand("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity");
                    sleep(3000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sleep(5000);
//            Runtime.getRuntime().exec("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity").waitFor();
                sleep(3000);
                XLog.e ("界面异常，杀死APP，继续测试");
                XLog.e ("----------------------------------------");
                return false;
            }
        }

        UiObject connect = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/tv_has_connected"));
        //Button com.chipsguide.app.roav.fmplayer:id/pb_connect_device
        String text = null;
        try {
            text = connect.getText().trim();
        } catch (UiObjectNotFoundException e) {
            XLog.e ("蓝牙未连接成功");
            XLog.e (e.toString ());
            return false;
        }
        if (text.equals("Connected")) {
            return true;
        } else {
            XLog.d ("蓝牙未连接成功");
            connectBle();
            connectCount++;
            if (connectCount > 3) {
                XLog.e ("连接三次蓝牙都无法连接成功");
                return false;
            }
        }
        return true;
    }


    public boolean waitForUiObjectText(String text) throws UiObjectNotFoundException {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            XLog.i("waiting for ota result...");
            UiObject it = new UiObject(new UiSelector().text(text));

            UiObject fail = new UiObject(new UiSelector().resourceId ("com.chipsguide.app.roav.fmplayer:id/tv_try_later").text("TRY LATER"));
            if (fail.exists ()){
                fail.clickAndWaitForNewWindow ();
                return false;
            }
            if (it.exists()) {
                return true;
            }
            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > 600000 ) {
                return false;
            }
        }
    }
}
