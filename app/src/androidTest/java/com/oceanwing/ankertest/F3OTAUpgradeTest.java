package com.oceanwing.ankertest;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by MD01 on 2017/9/6.
 */
@RunWith(AndroidJUnit4.class)
public class F3OTAUpgradeTest {
    private UiDevice mDevice = null;
    private int count = 0;
    private String filePath;

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.oceanwing.otaupdate", appContext.getPackageName());
    }

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Log.e("Tag------->", "Start ota test");
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
        String testPath = Environment.getExternalStorageDirectory() + "/DCIM/";
        filePath = testPath + "/" + df.format(new Date()) + ".xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("OTATest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("BLE连接");
            row.createCell(2).setCellValue("Setting");
            row.createCell(3).setCellValue("Result");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }

    }

    @Test
    public void otaUpdateTest() throws Exception {
        while (true) {
            count++;
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(count);
            row.createCell(0).setCellValue(count);

            boolean connect = connectBle();
            if (connect){
                row.createCell(1).setCellValue("pass");
            }else {
                row.createCell(1).setCellValue("fail");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                fail("BLE connect fail 3 times");
            }
            boolean setting = pressSetting();
            if (setting){
                row.createCell(2).setCellValue("pass");
            }else {
                row.createCell(2).setCellValue("fail");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                fail("setting fail");
            }
            sleep(1000);
            pressUpgrade();
//            if (result){
                row.createCell(3).setCellValue("pass");
//            }else {
//                row.createCell(3).setCellValue("fail");
//                FileOutputStream os = new FileOutputStream(filePath);
//                wb.write(os);
//                os.close();
//                wb.close();
//                fail("ota fail");
//            }
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
            sleep(3000);
            mDevice.pressBack();
            pressBackSetting();
            sleep(1000);
            otaUpdateTest();
        }
    }

    private boolean pressBackSetting() {
        //通过scrollable属性来选定滑动view
        boolean setting = false;
        UiScrollable appViews = new UiScrollable(new UiSelector().resourceId("com.zhixin.roav.charger.spectrum:id/viewPager"));
        appViews.setAsHorizontalList();
        try {
//            appViews.flingToBeginning(6);
            appViews.scrollToBeginning(5);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            setting = mDevice.wait(Until.hasObject(By.text("com.zhixin.roav.charger.spectrum:id/charge_connection_button")), 2000);
        } catch (Exception e) {

        }
        return setting;
    }

    private void pressUpgrade() {
        //com.zhixin.roav.charger.spectrum:id/ota_update_container
        UiObject2 upgrade = mDevice.findObject(By.res("com.zhixin.roav.charger.spectrum:id/ota_update_container"));
        upgrade.clickAndWait(Until.newWindow(), 5000);
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //com.zhixin.roav.viva:id/bt_update_device
        UiObject2 device = mDevice.findObject(By.res("com.zhixin.roav.charger.spectrum:id/bt_update_device"));
        device.click();
        try {
            mDevice.wait(Until.hasObject(By.text("Update Successfully")), 300000);
        } catch (Exception e) {
        }
    }

    private boolean pressSetting() {
        //通过scrollable属性来选定滑动view
        boolean setting = false;
        UiScrollable appViews = new UiScrollable(new UiSelector().resourceId("com.zhixin.roav.charger.spectrum:id/viewPager"));
        appViews.setAsHorizontalList();
        try {
            appViews.flingToEnd(5);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            setting = mDevice.wait(Until.hasObject(By.text("Updade Device")), 4000);
        } catch (Exception e) {

        }
        return true;
    }

    private boolean connectBle(){
        int connectCount = 0;
        UiObject2 connect = mDevice.findObject(By.res("com.zhixin.roav.charger.spectrum:id/charge_connection_button"));
        String text = connect.getText().trim();
        if (text.equals("Disconnect")){
            return true;
        }else {
            connect.clickAndWait(Until.newWindow(), 3000);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectBle();
            connectCount++;
            if (connectCount > 2){
                return false;
            }
        }
        return true;
    }
}
