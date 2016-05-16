package com.music.lichao.feicui.component;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.music.lichao.feicui.R;
import com.music.lichao.feicui.until.MusicManager;

/**
 * 桌面组件广播接收器
 * Created by z on 2016/5/11.
 */
public class MyAppWidgetProvider extends AppWidgetProvider {
    //更新桌面控件ui的Action
    public static final String WIDGET_UPDATE_UI = "com.music.lichao.feicui.WIDGET_UPDATE_UI";
    //音乐管理器
    MusicManager mm;

    public MyAppWidgetProvider() {
        super();
    }

    // 接收广播的回调函数
    @Override
    public void onReceive(Context context, Intent intent) {
        mm = MusicManager.getInstance(context);
        //获得命令action
        String action = intent.getAction();
        //来自桌面组件的点击命令
        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            //判断命令来源按钮
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            switch (buttonId) {
                //播放、暂停
                case R.id.tv_widget_play:
                    Intent intentPlay = new Intent(MusicService.ORDER_NOTIFYCATION_PLAY);
                    context.sendBroadcast(intentPlay);
                    break;
                case R.id.tv_widget_pause:
                    Intent intentPause = new Intent(MusicService.ORDER_NOTIFYCATION_PAUSE);
                    context.sendBroadcast(intentPause);
                    break;
                //上一曲
                case R.id.tv_widget_last:
                    Intent intentLast = new Intent(MusicService.ORDER_NOTIFYCATION_LAST);
                    context.sendBroadcast(intentLast);
                    break;
                //下一曲
                case R.id.tv_widget_next:
                    Intent intentNext = new Intent(MusicService.ORDER_NOTIFYCATION_NEXT);
                    context.sendBroadcast(intentNext);
                    break;
            }
        }
        //更新UI的action
        else if (WIDGET_UPDATE_UI.equals(action)) {
            pushUpdate(context, AppWidgetManager.getInstance(context), mm.getCurrentName(), mm.isPause());
        }
        super.onReceive(context, intent);
    }

    /*
     * 在3种情况下会调用OnUpdate()。onUpdate()是在main线程中进行，因此如果处理需要花费时间多于10秒，处理应在service中完成。
     * （1）在时间间隔到时调用，时间间隔在widget定义的android:updatePeriodMillis中设置；
     * （2）用户拖拽到主页，widget实例生成。无论有没有设置Configure activity，我们在Android4.4的测试中，当用户拖拽图
     * 片至主页时，widget实例生成，会触发onUpdate()，然后再显示activity（如果有）。这
     * 点和资料说的不一样，资料认为如果设置了Configure acitivity，就不会在一开始调用onUpdate()，而
     * 实验显示当实例生成（包括创建和重启时恢复），都会先调用onUpate()。在本例，由于此时在preference尚未有相关数据，创
     * 建实例时不能有效进行数据设置。
     * （3）机器重启，实例在主页上显示，会再次调用onUpdate()
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        pushUpdate(context, appWidgetManager, "", false);
    }

    //根据按钮获得一个pendingintent
    private PendingIntent getPendingIntent(Context context, int buttonId) {
        //创建显式的intent，启动本组件
        Intent intent = new Intent();
        intent.setClass(context, MyAppWidgetProvider.class);
        //设置意图类型
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        //设置按钮来源信息
        intent.setData(Uri.parse("feicui:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }

    // 更新所有的 widget
    private void pushUpdate(Context context, AppWidgetManager appWidgetManager, String songName, Boolean play_pause) {

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
        //将按钮与点击事件绑定
        remoteView.setOnClickPendingIntent(R.id.tv_widget_play, getPendingIntent(context, R.id.tv_widget_play));
        remoteView.setOnClickPendingIntent(R.id.tv_widget_pause, getPendingIntent(context, R.id.tv_widget_pause));
        remoteView.setOnClickPendingIntent(R.id.tv_widget_next, getPendingIntent(context, R.id.tv_widget_next));
        remoteView.setOnClickPendingIntent(R.id.tv_widget_last, getPendingIntent(context, R.id.tv_widget_last));

        //设置内容
        if (!songName.equals("")) {
            remoteView.setTextViewText(R.id.tv_widget_name, songName);
        }

        // 相当于获得所有本程序创建的appwidget
        ComponentName componentName = new ComponentName(context, MyAppWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteView);
    }
}
