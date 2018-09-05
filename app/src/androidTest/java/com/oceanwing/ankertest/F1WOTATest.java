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
public class F1WOTATest {

    private UiDevice mDevice = null;
    private int count = 0;
    private String filePath;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        XLog.d("Start ota test");
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");//设置日期格式
        filePath = Env.S_ROOT_RESULT_FOLDER + df.format(new Date()) + ".xls";

        File f1 = new File(filePath);

        if (!f1.exists()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("F1WOTATest");
            HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue("蓝牙连接状态");
            row.createCell(1).setCellValue("setting点击Update Device");
            row.createCell(2).setCellValue("开始时间");
            row.createCell(3).setCellValue("Update");
            row.createCell(4).setCellValue("测试结果");
            row.createCell(5).setCellValue("结束时间");

            FileOutputStream os = new FileOutputStream(filePath);
            wb.write(os);
            os.close();
            wb.close();
        }
    }

    @Test
    public void otaTest() throws UiObjectNotFoundException, IOException, InterruptedException {
        //判断是否处于蓝牙连接页面，做些处理

        while (true) {
            count++;
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.createRow(count);
            row.createCell(0).setCellValue(count);
            XLog.i(String.format(Locale.CHINA, "第%d次ota测试", count));

            boolean isMain = isInMainActivity();

            if (isMain) {
                XLog.i("app页面在主页面，开始新一轮的ota测试");
                //判断蓝牙是否连接状态
                boolean connect = isBluetoothConnected();
                while (!connect){
                    sleep(10000);
                    XLog.i("蓝牙重新连接");
                    if(isBluetoothConnected()){
                        break;
                    }
                }
                boolean setting = pressSetting(5);
                while (!setting){
                    sleep(5000);
                    if (isInSettingUpdate()){
                        break;
                    }
                    XLog.i("再次滑动至setting页面");
                }
                UiObject fiveText = mDevice.findObject(new UiSelector()
                        .resourceId("com.zhixin.roav.charger.spectrum:id/ota_update_container"));
                fiveText.clickAndWaitForNewWindow();
                row.createCell(1).setCellValue("pass");
                boolean update = waitForUiObjectText("com.zhixin.roav.charger.spectrum:id/bt_update_device",
                        "Update Device");
                if (update){
                    UiObject it = new UiObject(new UiSelector().resourceId("com.zhixin.roav.charger.spectrum:id/bt_update_device"));

                    if (!it.exists()){
                        XLog.i("未找到新版本");
                        row.createCell(2).setCellValue(DisplayUtils.getSaveDate());
                        row.createCell(3).setCellValue("未找到新版本");
                    }else {

                        it.clickAndWaitForNewWindow();
                        row.createCell(2).setCellValue(DisplayUtils.getSaveDate());
                        row.createCell(3).setCellValue("pass");
                        boolean result = waitForUiObjectText("Update Successfully");
                        if (result) {
                            //测试成功
                            XLog.i("测试成功");
                            row.createCell(4).setCellValue("pass");
                        } else {
                            XLog.i("测试失败");
                            row.createCell(4).setCellValue("fail");
                        }
                        row.createCell(5).setCellValue(DisplayUtils.getSaveDate());
                        sleep(1000);
                    }

                    mDevice.pressBack();
                    XLog.i("sleep 20秒");
                    FileOutputStream os = new FileOutputStream(filePath);
                    wb.write(os);
                    os.close();
                    wb.close();
                    sleep(20000);

                    pressBackSetting(5);
                }else {
                    XLog.i("网络异常");
                    row.createCell (2).setCellValue ("fail");
                    row.createCell (3).setCellValue ("网络异常");

                    FileOutputStream os = new FileOutputStream(filePath);
                    wb.write(os);
                    os.close();
                    wb.close();
                }

            } else {
                killAppAndOpen();

                sleep(30000);
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

    private void killAppAndOpen() throws IOException, InterruptedException {
        Runtime.getRuntime().exec("am force-stop com.zhixin.roav.charger.spectrum").waitFor();
        sleep(1000);
        Runtime.getRuntime().exec("am start -n com.zhixin.roav.charger.spectrum/com.zhixin.roav.spectrum.ui.LoadingActivity").waitFor();
        sleep(1000);

//        mDevice.executeShellCommand("am force-stop com.zhixin.roav.charger.spectrum");
//        sleep(5000);
//        mDevice.executeShellCommand("am start -n com.chipsguide.app.roav.fmplayer/.activity.SplashActivity");
//        sleep(3000);
    }

    private boolean isInMainActivity() throws UiObjectNotFoundException {
        XLog.i("判断app处于哪个页面");

        UiObject connectButton = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/charge_connection_button")
                .className("android.widget.Button"));

        UiObject twoText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/park_icon"));

        UiObject threeText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/graph_view"));

        UiObject forthText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/color_picker_view"));

        UiObject fiveText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/ota_update_container"));

        if(connectButton.exists() && connectButton.isEnabled()) {
            XLog.i("判断app---->app页面在主页面");
            return true;
        } else if (twoText.exists()){
            XLog.i("判断app---->app页面在第二个页面，向左滑动");
            return pressBackSetting(2);
        }else if (threeText.exists()){
            XLog.i("判断app---->app页面在第三个页面，向左滑动");
            return pressBackSetting(3);
        }else if (forthText.exists()){
            XLog.i("判断app---->app页面在第四个页面，向左滑动");
            return pressBackSetting(4);
        }else if (fiveText.exists()){
            XLog.i("判断app---->app页面在第五个页面，向左滑动");
            return pressBackSetting(5);
        }
        return false;
    }

    private boolean isInSettingUpdate() throws UiObjectNotFoundException {
        UiObject connectButton = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/charge_connection_button"));

        UiObject twoText = mDevice.findObject(new UiSelector()//com.chipsguide.app.roav.fmplayer:id/park_icon
                .resourceId("com.zhixin.roav.charger.spectrum:id/park_icon"));

        UiObject threeText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/graph_view"));

        UiObject forthText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/color_picker_view"));

        UiObject fiveText = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/ota_update_container"));

        if(connectButton.exists()) {
            XLog.i("判断app---->app页面在主页面，向右滑动");
            return pressSetting(5);
        } else if (twoText.exists()){
            XLog.i("判断app---->app页面在第二个页面，向右滑动");
            return pressSetting(4);
        }else if (threeText.exists()){
            XLog.i("判断app---->app页面在第三个页面，向右滑动");
            return pressSetting(3);
        }else if (forthText.exists()){
            XLog.i("判断app---->app页面在第四个页面，向右滑动");
            return pressSetting(2);
        }else if (fiveText.exists()){
            XLog.i("判断app---->app页面在第五个页面");
            return true;
        }
        return false;
    }

    private boolean pressSetting(int count) throws UiObjectNotFoundException {
        //通过scrollable属性来选定滑动view
        UiScrollable appViews = new UiScrollable(new UiSelector().resourceId("com.zhixin.roav.charger.spectrum:id/viewPager"));
        appViews.setAsHorizontalList();
        appViews.flingToEnd(count);
        UiObject connectButton = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/ota_update_container"));
        return connectButton.exists() && connectButton.isEnabled();
    }

    private boolean pressBackSetting(int count) throws UiObjectNotFoundException {
        //通过scrollable属性来选定滑动view
        UiScrollable appViews = new UiScrollable(new UiSelector().resourceId("com.zhixin.roav.charger.spectrum:id/viewPager"));
        appViews.setAsHorizontalList();
        appViews.scrollToBeginning(count);

        UiObject connectButton = mDevice.findObject(new UiSelector()
                .resourceId("com.zhixin.roav.charger.spectrum:id/charge_connection_button"));
        return connectButton.exists() && connectButton.isEnabled();
    }

    public boolean waitForUiObjectText(String rsid, String text) {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            UiObject it = new UiObject(new UiSelector().resourceId(rsid).text(text));

            if (it.exists()) {
                return true;
            }

            Date end = new Date();
            long time = end.getTime() - start.getTime();
            if (time > 30000 ) {
                return false;
            }
        }
    }


    public boolean waitForUiObjectText( String text) throws UiObjectNotFoundException {//等待对象出现
        Date start = new Date();
        while (true) {
            try {
                sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            UiObject it = new UiObject(new UiSelector().text(text));

            UiObject fail = new UiObject(new UiSelector().resourceId("android:id/button2"));

            if (fail.exists ()){
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
