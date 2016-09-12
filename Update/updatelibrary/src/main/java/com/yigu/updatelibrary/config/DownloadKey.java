package com.yigu.updatelibrary.config;

import android.content.Context;


/**
 * Created by Administrator on 2016/8/10.
 */
public class DownloadKey {
    public final static int showUpdateView = 0;
    public final static int showDownloadView = 2;
    public final static int closeDownloadView = 1;
    public static Context fromActivity = null;
    public static int ToShowDownloadView = showUpdateView;//显示更新弹框
    public static String saveFileName = "newversion.apk";

    public static String apkUrl;
    public static String changeLog;
    public static Integer version = 0;

    public static boolean intercetFlag;
    public static Boolean ISManual = false;
    public static Boolean LoadManual = false;
}