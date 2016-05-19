package com.music.lichao.feicui.component;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.music.lichao.feicui.R;
import com.music.lichao.feicui.until.MusicManager;

/**
 * 音乐后台服务
 * Created by z on 2016/5/11.
 */
public class MusicService extends Service {
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
        //载入音乐列表
        mm = MusicManager.getInstance(this);
        mm.scanMusic(getContentResolver());
        //注册命令控制接收器
        MusicReceiver receiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ORDER_NOTIFYCATION_PLAY);
        intentFilter.addAction(ORDER_NOTIFYCATION_PAUSE);
        intentFilter.addAction(ORDER_NOTIFYCATION_NEXT);
        intentFilter.addAction(ORDER_NOTIFYCATION_LAST);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
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
                //屏幕熄灭
                case Intent.ACTION_SCREEN_OFF:
                    Intent lockIntent = new Intent(MusicService.this, LockScreenActivity.class);
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(lockIntent);
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
