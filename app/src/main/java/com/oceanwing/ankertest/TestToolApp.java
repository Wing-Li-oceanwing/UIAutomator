package com.oceanwing.ankertest;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.interceptor.BlacklistTagsFilterInterceptor;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;

/**
 * @author wing.li
 * @date on 2017/11/28.
 **/

public class TestToolApp extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();



        setContext(getApplicationContext());

        Env.init();

        initXlog();

    }

    private void initXlog() {
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(BuildConfig.DEBUG ? LogLevel.ALL
                        : LogLevel.NONE)
                .tag("Test")
//                .t()
                .nst()
//                .b()
                .addInterceptor(new BlacklistTagsFilterInterceptor(
                        "blacklist1", "blacklist2", "blacklist3"))
                .build();

        Printer androidPrinter = new AndroidPrinter();
        Printer filePrinter = new FilePrinter
                .Builder(Env.S_ROOT_LOG_FOLDER)
                .fileNameGenerator(new DateFileNameGenerator())
                .logFlattener(new ClassicFlattener())
                .build();

        XLog.init(
                config,
                androidPrinter,
                filePrinter);
    }

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context) {
        mContext = context;
    }
}
