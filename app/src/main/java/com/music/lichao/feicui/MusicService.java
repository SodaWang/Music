package com.music.lichao.feicui;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.music.lichao.feicui.until.MusicManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;

/**
 * 音乐后台服务
 * Created by z on 2016/5/11.
 */
public class MusicService extends Service {
    //桌面组件管理器
    AppWidgetManager appWidgetManager;
    //桌面组件名
    public ComponentName componentName;
    //远程桌面控件
    private RemoteViews remoteViews;
    //自定义控制命令的action
    public static final String ORDER_NOTIFYCATION_PLAY = "com.muisic.lichao.feicui.ORDER_NOTIFYCATION_PLAY";
    public static final String ORDER_NOTIFYCATION_PAUSE = "com.muisic.lichao.feicui.ORDER_NOTIFYCATION_PAUSE";
    public static final String ORDER_NOTIFYCATION_NEXT = "com.muisic.lichao.feicui.ORDER_NOTIFYCATION_NEXT";
    public static final String ORDER_NOTIFYCATION_LAST = "com.muisic.lichao.feicui.ORDER_NOTIFYCATION_LAST";
    //音乐管理器
    MusicManager mm;

    public MusicService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //装载远程视图
        remoteViews = new RemoteViews(getPackageName(), R.layout.appwidget_layout);
        //装载组件名
        componentName = new ComponentName(this, MyAppWidgetProvider.class);
        //载入桌面组件管理器
        appWidgetManager = AppWidgetManager.getInstance(this);
        //载入音乐列表
        mm = MusicManager.getInstance();
        mm.scanMusic(getContentResolver());
        //注册命令控制接收器
        MusicReceiver receiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ORDER_NOTIFYCATION_PLAY);
        intentFilter.addAction(ORDER_NOTIFYCATION_PAUSE);
        intentFilter.addAction(ORDER_NOTIFYCATION_NEXT);
        intentFilter.addAction(ORDER_NOTIFYCATION_LAST);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MusicReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            //获得接收到的广播的action
            String action = intent.getAction();
            switch (action) {
                //播放
                case ORDER_NOTIFYCATION_PLAY:
                    mm.play(mm.getCurrentIndex());
                    break;
                //暂停
                case ORDER_NOTIFYCATION_PAUSE:
                    mm.pause();
                    break;
                //上一曲
                case ORDER_NOTIFYCATION_LAST:
                    mm.last();
                    break;
                //下一曲
                case ORDER_NOTIFYCATION_NEXT:
                    mm.next();
                    break;
            }
            //更新桌面组件
            updateWidgetUi();
        }
    }

    //更新桌面组件
    private void updateWidgetUi() {
        //通知桌面组件更新UI
        Intent intent = new Intent(MyAppWidgetProvider.WIDGET_UPDATE_UI);
        sendBroadcast(intent);
    }

}
