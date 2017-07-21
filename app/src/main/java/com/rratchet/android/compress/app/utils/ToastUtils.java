package com.rratchet.android.compress.app.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.rratchet.android.compress.app.AndroidApplication;

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

public class ToastUtils {

    private static Handler mHandler;
    private static Toast mToast;

    public static void show(CharSequence msg) {
        show(msg, false);
    }

    public static void show(final CharSequence msg, final boolean timeLong) {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        if (mHandler != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mToast == null) {
                        mToast = Toast.makeText(AndroidApplication.getInstance(), msg, timeLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                    } else {
                        mToast.setDuration(timeLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                        mToast.setText(msg);
                    }
                    mToast.show();
                }
            });
    }
}
