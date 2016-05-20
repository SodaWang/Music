package com.music.lichao.feicui.component;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.music.lichao.feicui.R;
import com.music.lichao.feicui.adapter.MusicAdapter;
import com.music.lichao.feicui.until.MusicEntity;
import com.music.lichao.feicui.until.MusicManager;

import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, FreshUIListener {

    //音乐管理器
    MusicManager mm;

    //音乐列表
    RecyclerView recyclerView;

    //音乐信息表
    List<MusicEntity> musicList;

    //音乐列表适配器
    MusicAdapter musicAdapter;

    //音乐控制器按钮
    Button play, pause, next, last, refresh;

    //封面
    ImageView album;

    //歌手、歌名
    TextView art, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //自定义标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
    }

    /**
     * 装载资源
     */
    private void init() {
        //初始化数据
        mm = MusicManager.getInstance(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        play = (Button) findViewById(R.id.bt_main_play);
        pause = (Button) findViewById(R.id.bt_main_pause);
        next = (Button) findViewById(R.id.bt_main_next);
        last = (Button) findViewById(R.id.bt_main_last);
        refresh = (Button) findViewById(R.id.bt_refresh);
        album = (ImageView) findViewById(R.id.iv_main_album);
        art = (TextView) findViewById(R.id.tv_main_art);
        name = (TextView) findViewById(R.id.tv_main_name);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
        refresh.setOnClickListener(this);
        mm.setFreshUIListener(this);

        musicList = mm.scanMusic(getContentResolver());
        //装载数据列表
        initRecyclerView();

        //设置自定义通知
        initNotify();

        //启动音乐服务
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);

        //刷新UI
        upDateMainUi();
    }

    /**
     * 装载数据列表
     */
    private void initRecyclerView() {
        //初始化适配器
        musicAdapter = new MusicAdapter(this, musicList);
        //列表点击事件
        musicAdapter.setOnItemClickLitener(new MusicAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                //播放音乐
                mm.setCurrentIndex(position);
                mm.play(position);
            }
        });
        //给列表设置适配器
        recyclerView.setAdapter(musicAdapter);
        //设置列表布局样式
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置歌手和歌名
        art.setText(mm.getMusicList().get(mm.getCurrentIndex()).getArt());
        name.setText(mm.getMusicList().get(mm.getCurrentIndex()).getTitle());
    }

    /**
     * 装载通知
     */
    private void initNotify() {
        // 在Android进行通知处理，首先需要重系统哪里获得通知管理器NotificationManager，它是一个系统Service。
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //设置远程view，由于通知中的布局并不是在app中展示的，需要采取这种方式来设置
        RemoteViews remoteViews = new RemoteViews(
                getPackageName(),
                R.layout.notify_music);

        // 设置通知栏中的play、pause、next、last 监听
        Intent intentPlay = new Intent("com.muisic.lichao.feicui.ORDER_NOTIFYCATION_PLAY");
        // 使用getBroadcast方法，得到一个PendingIntent对象，当该对象执行时，会发送一个广播
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this,
                0, intentPlay, 0);
        remoteViews.setOnClickPendingIntent(R.id.bt_notify_play,
                pendingIntentPlay);

        Intent intentPause = new Intent("com.muisic.lichao.feicui.ORDER_NOTIFYCATION_PAUSE");
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this,
                0, intentPause, 0);
        remoteViews.setOnClickPendingIntent(R.id.bt_notify_pause,
                pendingIntentPause);

        Intent intentNext = new Intent("com.muisic.lichao.feicui.ORDER_NOTIFYCATION_NEXT");
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this,
                0, intentNext, 0);
        remoteViews.setOnClickPendingIntent(R.id.bt_notify_next,
                pendingIntentNext);

        Intent intentLast = new Intent("com.muisic.lichao.feicui.ORDER_NOTIFYCATION_LAST");
        PendingIntent pendingIntentLast = PendingIntent.getBroadcast(this,
                0, intentLast, 0);
        remoteViews.setOnClickPendingIntent(R.id.bt_notify_last,
                pendingIntentLast);


        //装载自定义通知
        PendingIntent pendingIntentNotify = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);//通知点击后的跳转逻辑
        Notification notification = new Notification.Builder(this)
                .setContentTitle("音乐控件")
                .setContentText("下拉展开")
                .setContentIntent(pendingIntentNotify)
                .setSmallIcon(R.drawable.music)
                .build();
        notification.bigContentView = remoteViews;//设置大视图布局
        notification.flags |= Notification.FLAG_NO_CLEAR;//设置通知参数
        //给音乐管理器设置远程视图
        mm.setmRemoteViews(remoteViews);
        //把通知配置装载到音乐管理器中
        mm.setNotifycationManager(notificationManager);
        mm.setNotifycation(notification);
        //发送通知
        notificationManager.notify(0, notification);


        //手表端通知


        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        // 1.设置显示内容
        builder.setContentTitle(mm.getMusicList().get(mm.getCurrentIndex()).getTitle());
//        builder.setContentText(musicInfo.artist);
        // 若只设置了SmallIcon,而没设置LargeIcon,则在通知栏左侧会显示SmallIcon设置的图标;若同时设置了LargeIcon,则左侧显示LargeIcon,右侧显示SmallIcon
        builder.setSmallIcon(R.mipmap.ic_launcher);

        // 2.设置跳转属性
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        // 设置了ContentIntent后,通知栏有了点击效果,而wear滑动到最右侧时,多了一个Open on phone的页面
        builder.setContentIntent(pendingIntent);

        // 3.设置通知属性
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);

        // 4.设置手表特有属性
        builder.extend(extendWear(builder));
        notificationManager.notify(1, builder.build());

        builder.setAutoCancel(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //播放
            case R.id.bt_main_play:
                mm.play(0);
                break;
            //暂停
            case R.id.bt_main_pause:
                mm.pause();
                break;
            //下一曲
            case R.id.bt_main_next:
                mm.next();
                break;
            //上一曲
            case R.id.bt_main_last:
                mm.last();
                break;
            //刷新
            case R.id.bt_refresh:
                musicList = mm.scanMusic(getContentResolver());
                musicAdapter.notifyDataSetChanged();
                break;
        }

    }


    /**
     * 刷新主页面UI
     */
    private void upDateMainUi() {
        //刷新歌曲选中状态
        musicAdapter.setSelection(mm.getCurrentIndex());
        musicAdapter.notifyDataSetChanged();
        //刷新封面
        album.setBackground(mm.getMusicList().get(mm.getCurrentIndex()).getAlbum_img());
        //刷新歌手和歌名
        art.setText(mm.getMusicList().get(mm.getCurrentIndex()).getArt());
        name.setText(mm.getMusicList().get(mm.getCurrentIndex()).getTitle());
    }

    //手表端通知
    private NotificationCompat.WearableExtender extendWear(NotificationCompat.Builder builder) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

        BitmapFactory.Options options = new BitmapFactory.Options();

        // 可滚动,背景则为640x400,否则为400x400.
        // 若设置了背景,则LargeIcon在Wear端失效
        options.outWidth = 400;
        options.outHeight = 400;
        Bitmap wearBgBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.bg, options);
        // 设置了Bg后,LargeIcon便失效
        wearableExtender.setBackground(wearBgBitmap);

        Intent pauseIntent = new Intent(MusicService.ORDER_NOTIFYCATION_PAUSE);
        PendingIntent pausePIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
        NotificationCompat.Action paAction = new NotificationCompat.Action(
                R.drawable.pause, "暂停", pausePIntent);
        wearableExtender.addAction(paAction);

        Intent playIntent = new Intent(MusicService.ORDER_NOTIFYCATION_PLAY);
        PendingIntent playPIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
        NotificationCompat.Action pauseAction = new NotificationCompat.Action(
                R.drawable.play, "播放", playPIntent);
        wearableExtender.addAction(pauseAction);

        Intent nextIntent = new Intent(MusicService.ORDER_NOTIFYCATION_NEXT);
        PendingIntent nextPIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        NotificationCompat.Action nextAction = new NotificationCompat.Action(
                R.drawable.right, "下一曲", nextPIntent);
        wearableExtender.addAction(nextAction);

        Intent lastIntent = new Intent(MusicService.ORDER_NOTIFYCATION_LAST);
        PendingIntent lastPIntent = PendingIntent.getBroadcast(this, 0, lastIntent, 0);
        NotificationCompat.Action lastAction = new NotificationCompat.Action(
                R.drawable.left, "上一曲", lastPIntent);
        wearableExtender.addAction(lastAction);

        // 隐藏默认应用图标
        wearableExtender.setHintHideIcon(true);
        // 设置跟ContentText跟随的图标
        wearableExtender.setContentIcon(R.mipmap.ic_launcher);
        // 只支持Start和End标签,默认是End
        wearableExtender.setContentIconGravity(Gravity.END);

        return wearableExtender;
    }

    @Override
    public void freshUI() {
        upDateMainUi();
    }
}
