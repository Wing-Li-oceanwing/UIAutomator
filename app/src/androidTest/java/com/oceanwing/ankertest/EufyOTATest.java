package com.oceanwing.ankertest;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;

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

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EufyOTATest {

    private UiDevice mDevice = null;
    private int testCount = 0;
    private String filePath;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        XLog.d("Start ota test");
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
        filePath = Env.S_ROOT_RESULT_FOLDER + df.format(new Date()) + ".xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook ();
            HSSFSheet sheet = wb.createSheet ("OTA");
            HSSFRow row = sheet.createRow (0);
            row.createCell (0).setCellValue ("测试次数");
            row.createCell (1).setCellValue ("update按键");
            row.createCell (2).setCellValue ("测试结果");
            FileOutputStream os = new FileOutputStream (filePath);
            wb.write (os);
            os.close ();
            wb.close ();
        }
    }

    @Test
    public void otaTest() throws UiObjectNotFoundException, IOException, InterruptedException {
        //判断是否处于蓝牙连接页面，做些处理
        while (true) {
            testCount++;
            XLog.d ("-------------------------------------------------------------------------");
            XLog.d ("-------------------------------------------------------------------------");
            XLog.d (String.format (Locale.CHINA, "第%d次升级....", testCount));
            HSSFWorkbook wb = new HSSFWorkbook (new FileInputStream (filePath));
            HSSFSheet sheet = wb.getSheetAt (0);
            HSSFRow row = sheet.createRow (testCount);
            row.createCell (0).setCellValue (testCount);

            XLog.i("检测是否Update出现");
            boolean isUpdate = waitForUiObjectText("com.eufylife.smarthome:id/updateStatus", "Update", "Failed");
            if (isUpdate){

                UiObject update = new UiObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateStatus"));
                row.createCell (1).setCellValue ("点击update");
                XLog.i("点击Update按键");
                update.clickAndWaitForNewWindow();
                boolean result = waitForUiObjectText("Later");
                if (result){
                    row.createCell (2).setCellValue ("升级成功");
                    XLog.i("弹出了Later的对话框");
                }else {
                    row.createCell (2).setCellValue ("升级异常");
                    XLog.i("没有弹出了Later的对话框");
                }
            }else {
                row.createCell (1).setCellValue ("Update异常");
                XLog.i("Update按键未发现,等待100秒");
                sleep(100000);
            }
            FileOutputStream os = new FileOutputStream (filePath);
            wb.write (os);
            os.close ();
            wb.close ();
        }
    }

    public boolean waitForUiObjectText(String rsid, String text, String text2) {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            UiObject it = new UiObject(new UiSelector().resourceId(rsid).text(text));
            UiObject it2 = new UiObject(new UiSelector().resourceId(rsid).text(text2));

            if (it.exists()) {
                return true;
            }else if (it2.exists()){
                return true;
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > 60000 ) {
                return false;
            }
        }
    }

    //com.eufylife.smarthome:id/updateStatus
    //Update

    //com.eufylife.smarthome:id/ok
    //com.eufylife.smarthome:id/cancel
    //Try Again

    public boolean waitForUiObjectText( String text) throws UiObjectNotFoundException {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UiObject it = new UiObject(new UiSelector().resourceId("com.eufylife.smarthome:id/cancel").text(text));
            UiObject fail = new UiObject(new UiSelector().resourceId("com.eufylife.smarthome:id/updateStatus").text("Failed"));
            if (it.exists ()){
                XLog.i("已经检测到升级完成的对话框，等待10秒点击Later");
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                it.clickAndWaitForNewWindow ();
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
