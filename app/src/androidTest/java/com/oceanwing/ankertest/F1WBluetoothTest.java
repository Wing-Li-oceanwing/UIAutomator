package com.oceanwing.ankertest;

import android.content.Context;
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
import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class F1WBluetoothTest {
    private UiDevice mDevice = null;
    private Context context = null;
    private String filePath;
    private int testCount = 0;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        context = InstrumentationRegistry.getTargetContext();

        assertEquals("com.oceanwing.ankertest", context.getPackageName());
    }

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
            row.createCell(1).setCellValue("当前蓝牙连接状态");
            row.createCell(2).setCellValue("更改状态结果");
            row.createCell(3).setCellValue("连接时长");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    @Test
    public void openVideoTest() throws InterruptedException, IOException, RemoteException, UiObjectNotFoundException {
        // Open DashCam Videos com.zhixin.roav.cam:id/v_cam_container
//        测试次数
//        WIFI连接
        while (true) {

            testCount++;
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(testCount);
            row.createCell(0).setCellValue(testCount);

//            UiObject2 status = mDevice.findObject(By.res("com.zhixin.roav.charger.spectrum:id/charge_connection_button"));
            UiObject status = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button"));
            String strStatus = status.getText();
            row.createCell(1).setCellValue(strStatus);

            XLog.i("测试次数：" + testCount);

            switch (strStatus) {

                case "Disconnect":
                    row.createCell(1).setCellValue("Connect");
                    boolean disconnect = disConnectBluetooth();
                    row.createCell(2).setCellValue(disconnect);

                    break;
                case "Connect":
                    row.createCell(1).setCellValue("DisConnect");
                    Date start = new Date();
                    boolean connect = connectBluetooth();
                    row.createCell(2).setCellValue(connect);

                    if (connect){
                        Date end = new Date();
                        long time = end.getTime() - start.getTime();
                        XLog.i(String.format(Locale.CHINA, "蓝牙连接时长%d ms", time));
                        row.createCell(3).setCellValue(time);
                    }else {
                        row.createCell(3).setCellValue("null");
                    }
                    break;
                case "Connecting":
                    XLog.i("Connecting .....");
                    sleep(1000);
                    break;
                default:
                    break;
            }

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            sleep(5000);
        }
    }

    //连接蓝牙
    private boolean connectBluetooth() {
        UiObject2 disconnect = mDevice.findObject(By.res("com.chipsguide.app.roav.fmplayer:id/charge_connection_button"));
        String connect = disconnect.getText();
        if (connect.equals("Connect")) {
            XLog.d("开始连接蓝牙");
            disconnect.clickAndWait(Until.newWindow(), 1000);
        }
        boolean status = waitForUiObjectText("Disconnect");

        if (status) {
            XLog.d("连接蓝牙成功");
            return true;
        } else {
            XLog.d("连接蓝牙失败");
            return false;
        }
    }

    //断开蓝牙
    private boolean disConnectBluetooth() {
        UiObject2 disconnect = mDevice.findObject(By.res("com.chipsguide.app.roav.fmplayer:id/charge_connection_button"));
        String connect = disconnect.getText();
        if (connect.equals("Disconnect")) {
            XLog.d("开始断开蓝牙");
            disconnect.clickAndWait(Until.newWindow(), 1000);
        }
        connect = disconnect.getText();
        if (connect.equals("Connect")) {
            XLog.d("断开蓝牙：成功");
            return true;
        } else {
            XLog.d("断开蓝牙：失败");
            return false;
        }
    }

    //判断蓝牙是否连接
    public boolean waitForUiObjectText(String text) {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UiObject close = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/btn_close"));
            if (close.exists()){

                try {
                    close.clickAndWaitForNewWindow();
                } catch (UiObjectNotFoundException e) {
                    e.printStackTrace();
                }
                return false;
            }
//          UiObject2 it = mDevice.findObject(By.res("com.zhixin.roav.cam:id/tv_cam_video_desc").text(text));
            UiObject it = new UiObject(new UiSelector().resourceId("com.chipsguide.app.roav.fmplayer:id/charge_connection_button").text(text));
            if (it.exists()) {
                return true;
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > 35000) {
                return false;
            }
        }
    }

    private boolean isBluetoothConnected() throws UiObjectNotFoundException {
        UiObject connectButton = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/charge_connection_button")
                .className("android.widget.Button"));
        String text = connectButton.getText().trim();
        if (text.equals("Connect")){
            connectButton.clickAndWaitForNewWindow();
            return false;
        }else {
            return true;
        }
    }

}
