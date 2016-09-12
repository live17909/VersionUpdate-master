package com.yigu.updatelibrary.module;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.yigu.updatelibrary.activity.DownloadingActivity;
import com.yigu.updatelibrary.config.DownloadKey;
import com.yigu.updatelibrary.config.UpdateKey;
import com.yigu.updatelibrary.utils.FileUtil;
import com.yigu.updatelibrary.utils.GetAppInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2016/8/10.
 */
public class Download extends Thread {
    private final static int DOWN_UPDATE = 1;
    private final static int DOWN_OVER = 2;
    private Context mContext;
    private static int length;
    private static int count;
    private static int progress;
    private static File apkFile;
    private DownHandler mDownHandler;

    public Download(Context context) {
        mContext = context;
        mDownHandler = new DownHandler(context);
    }

    public Download(Notification.Builder builder, Context context) {
        mContext = context;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mDownHandler = new DownHandler(context, builder, notificationManager);
    }

    private static class DownHandler extends Handler {
        WeakReference<Context> mContextWeakReference;
        Notification.Builder mBuilder;
        NotificationManager mNotificationManager;

        DownHandler(Context context) {
            mContextWeakReference = new WeakReference<>(context);
        }

        DownHandler(Context context, Notification.Builder builder, NotificationManager notificationManager) {
            mContextWeakReference = new WeakReference<>(context);
            mBuilder = builder;
            mNotificationManager = notificationManager;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            Context context = mContextWeakReference.get();
            switch (msg.what) {
                case DOWN_UPDATE:
                    if (UpdateKey.DialogOrNotification == UpdateKey.WITH_DIALOG) {
                        ((DownloadingActivity) context).mProgress.setProgress(progress);
                        ((DownloadingActivity) context).count.setText(progress + "%");
                    } else if (UpdateKey.DialogOrNotification == UpdateKey.WITH_NOTIFICATION) {
                        mBuilder.setProgress(length, count, false)
                                .setContentText("下载进度:" + progress + "%");
                        mNotificationManager.notify(1115, mBuilder.build());
                    }
                    break;
                case DOWN_OVER:
                    if (UpdateKey.DialogOrNotification == UpdateKey.WITH_DIALOG) {
                        ((DownloadingActivity) context).finish();
                    } else if (UpdateKey.DialogOrNotification == UpdateKey.WITH_NOTIFICATION) {
                        mBuilder.setTicker("下载完成");
                        mNotificationManager.notify(1115, mBuilder.build());
                        mNotificationManager.cancel(1115);
                    }
                    length = 0;
                    count = 0;
                    DownloadKey.ToShowDownloadView = DownloadKey.closeDownloadView;
                    if (checkApk(context)) {
                        installApk(context);
                    }
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void run() {
        URL url = null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            url = new URL(DownloadKey.apkUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            length = connection.getContentLength();
            inputStream = connection.getInputStream();
        } catch (FileNotFoundException e) {
            try {
                connection.disconnect();
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                String location = new String(connection.getHeaderField("Location").getBytes("ISO-8859-1"), "UTF-8").replace(" ", "");
                url = new URL(location);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                length = connection.getContentLength();
                inputStream = connection.getInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File appDir = FileUtil.getCacheDirectory(mContext);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            apkFile = new File(appDir, DownloadKey.saveFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(apkFile);
            long temFileLength = appDir.length();
            byte buf[] = new byte[1024];
            int times = 0;//这很重要
            int numread;
            do {
                numread = inputStream.read(buf);
                count += numread;
                progress = (int) (((float) count / length) * 100);
                if ((times == 512) || (temFileLength == length)) {
                    mDownHandler.sendEmptyMessage(DOWN_UPDATE);
                    times = 0;
                }
                times++;
                if (numread <= 0) {
                    mDownHandler.sendEmptyMessage(DOWN_OVER);
                    break;
                }
                fileOutputStream.write(buf, 0, numread);
            } while (!DownloadKey.intercetFlag);//点击取消就停止下载

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            connection.disconnect();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkApk(Context context) {
        String apkName = GetAppInfo.getAPKPackageName(context, apkFile.toString());
        String appName = GetAppInfo.getAppPackageName(context);
        if (apkName != null && apkName.equals(appName)) {
            Log.i("UpdateFun TAG", "包名相同,安装apk");
            return true;
        } else {
            Log.i("UpdateFun TAG",
                    String.format("apk检验:包名不同。该app包名:%s，apk包名:%s", appName, apkName));
            Toast.makeText(context, "apk检验:包名不同,不进行安装,原因可能是运营商劫持", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private static void installApk(Context context) {
        if (!apkFile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkFile.toString()),
                "application/vnd.android.package-archive");
        context.startActivity(i);
    }
}
