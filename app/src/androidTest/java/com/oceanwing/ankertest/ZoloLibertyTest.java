package com.oceanwing.ankertest;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
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
public class ZoloLibertyTest {
    private UiDevice mDevice = null;
    private static final String BASIC_SAMPLE_PACKAGE = "com.oceanwing.zolohome";
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
            HSSFSheet sheet = wb.createSheet("nameTest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("测试次数");
            row.createCell(1).setCellValue("EQ");
            row.createCell(2).setCellValue("Transparency");
            row.createCell(3).setCellValue("MyZolo");
            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }

    }

    @Test
    public void otaUpdateTest() throws Exception {
        Log.e("Tag------->", "Start update test");
        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        sleep(8000);
        while (true) {
            count++;
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(count);
            row.createCell(0).setCellValue(count);

            pressEQ();
            sleep(2000);
            try {
                boolean eq = clickListViewItem();
                if (eq) {
                    mDevice.pressBack();
                    row.createCell(1).setCellValue("pass");
                } else {
                    row.createCell(1).setCellValue("fail");
                    FileOutputStream os = new FileOutputStream(filePath);
                    wb.write(os);
                    os.close();
                    wb.close();
                    fail("press EQ fail");
                }
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }

            boolean trans = pressTransparency();
            if (trans) {
                row.createCell(2).setCellValue("pass");
                Thread.sleep(1000);
                mDevice.pressBack();
            } else {
                row.createCell(2).setCellValue("fail");
                FileOutputStream os = new FileOutputStream(filePath);
                wb.write(os);
                os.close();
                wb.close();
                fail();
            }
            pressMyZolo();
            row.createCell(3).setCellValue("pass");
            sleep(3000);

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();

        }
    }

    private void pressEQ() {
        UiObject2 eq = mDevice.findObject(By.res("com.oceanwing.zolohome:id/tv_eq"));
        eq.clickAndWait(Until.newWindow(), 5000);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean pressTransparency() throws UiObjectNotFoundException {
        UiObject eq = new UiObject(new UiSelector().resourceId("com.oceanwing.zolohome:id/tv_transparency"));
        eq.clickAndWaitForNewWindow();
        UiObject wifiSwitchObj = new UiObject(new UiSelector().resourceId("com.oceanwing.zolohome:id/switchview_transparency"));

        boolean switchTrans = wifiSwitchObj.clickAndWaitForNewWindow();
        if (!switchTrans) {
            return false;
        }
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void pressMyZolo() throws InterruptedException {
        UiObject2 more = mDevice.findObject(By.res("com.oceanwing.zolohome:id/tv_more"));
        more.clickAndWait(Until.newWindow(), 2000);

        UiObject2 myZolo = mDevice.findObject(By.res("com.oceanwing.zolohome:id/tv_my_zolo"));
        myZolo.clickAndWait(Until.newWindow(), 2000);

        try {
            mDevice.wait(Until.hasObject(By.text("Firmware")), 5000);
        } catch (Exception e) {

        }

        mDevice.pressBack();
        Thread.sleep(1000);
        mDevice.pressBack();
    }

    private boolean clickListViewItem() throws UiObjectNotFoundException {
        boolean haslist = mDevice.hasObject(By.res("com.oceanwing.zolohome:id/listview_equalizer"));
        if (!haslist) {
            return false;
        }
        UiObject listview = new UiObject(new UiSelector().className("android.widget.ListView"));
        for (int i = 0; i < listview.getChildCount(); i++) {
            boolean result = listview.getChild(new UiSelector().clickable(true).index(i)).clickAndWaitForNewWindow();
            if (!result) {
                return false;
            }
        }
        return true;
    }
}
