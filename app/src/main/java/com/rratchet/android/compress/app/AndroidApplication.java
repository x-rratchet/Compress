package com.rratchet.android.compress.app;

import android.app.Application;

/**
 * <pre>
 *
 * 作者:      ASLai(gdcpljh@126.com).
 * 日期:      17-7-21
 * 版本:      V1.0
 * 描述:      description
 *
 * </pre>
 */

public class AndroidApplication extends Application {


    private static volatile AndroidApplication mInstance;

    public static AndroidApplication getInstance() {
        if (mInstance == null) {
            synchronized (AndroidApplication.class) {
                if (mInstance == null) {
                    mInstance = new AndroidApplication();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }
}
