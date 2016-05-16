package com.music.lichao.feicui.until;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import com.music.lichao.feicui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐播放管理类
 * Created by z on 2016/5/10.
 */
public class MusicManager {

    //是否暂停中
    private boolean isPause = false;
    //音乐控制器
    private MediaPlayer mp;
    //音乐路径列表
    private List<String> list_path;
    //音乐封面列表
    private List<BitmapDrawable> list_img;
    //当前音乐编号
    private int currentIndex = 0;
    //通知管理器
    private NotificationManager notifycationManager;
    //通知
    private Notification notifycation;
    //远程视图
    private RemoteViews mRemoteViews;
    //单例
    private static MusicManager mMusicManager = null;
    //上下文
    private Context mContext;

    //构造器
    private MusicManager(Context context) {

        mContext = context;
        mp = new MediaPlayer();
    }

    //单例
    public static MusicManager getInstance(Context context) {
        if (mMusicManager == null) {
            mMusicManager = new MusicManager(context);
        }
        return mMusicManager;
    }

    //读写器
    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public RemoteViews getmRemoteViews() {
        return mRemoteViews;
    }

    public void setmRemoteViews(RemoteViews mRemoteViews) {
        this.mRemoteViews = mRemoteViews;
    }

    public NotificationManager getNotifycationManager() {
        return notifycationManager;
    }

    public void setNotifycationManager(NotificationManager notifycationManager) {
        this.notifycationManager = notifycationManager;
    }

    public Notification getNotifycation() {
        return notifycation;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setNotifycation(Notification notifycation) {
        this.notifycation = notifycation;
    }

    /**
     * 扫描音乐文件，返回文件路径列表
     *
     * @param contentResolver
     */
    public List<String> scanMusic(ContentResolver contentResolver) {
        //初始化音乐路径表
        if (list_path == null) {
            list_path = new ArrayList<String>();
        } else {
            list_path.clear();
        }
        //初始化封面信息列表
        if (list_img == null) {
            list_img = new ArrayList<BitmapDrawable>();
        } else {
            list_img.clear();
        }

        //查询设备中的音乐文件信息
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        //遍历歌曲信息
        if (cursor != null && cursor.moveToFirst()) {
            do {
                //得到歌曲文件的路径
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //保存到列表中
                list_path.add(url);
                //查询歌曲id
                int album_id = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                //根据歌曲id查询封面路径
                String albumArt = getAlbumArt(album_id, contentResolver);
                Bitmap bm = null;
                if (albumArt == null) {
                    bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);//默认封面图片
                    BitmapDrawable bmDrawable = new BitmapDrawable(mContext.getResources(), bm);
                    list_img.add(bmDrawable);
                } else {
                    bm = BitmapFactory.decodeFile(albumArt);
                    BitmapDrawable bmDrawable = new BitmapDrawable(mContext.getResources(), bm);//查询到的专辑封面
                    list_img.add(bmDrawable);
                }
            } while (cursor.moveToNext());
        }
        return list_path;
    }

    /**
     * 播放音乐
     *
     * @param id 音乐编号
     */
    public void play(int id) {
        try {
            if (isPause) {
                mp.start();
            } else {
                if (list_path != null && !list_path.isEmpty()) {
                    mp.reset();//把各项参数恢复到初始状态
                    mp.setDataSource(list_path.get(id));
                    mp.prepare();  //进行缓冲
                    mp.start();//播放
                    //更改通知栏音乐名字
                    if (mRemoteViews != null)
                        mRemoteViews.setTextViewText(R.id.tv_notify_name, list_path.get(id));
                    if (notifycationManager != null && notifycation != null) {
                        notifycationManager.notify(0, notifycation);
                    }
                } else {
                    Log.e("lichao", "音乐列表读取失败！");
                }

            }
            //取消暂停状态
            isPause = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     */
    public void pause() {
        if (mp != null && mp.isPlaying()) {
            mp.pause();
            //进入暂停状态
            isPause = true;
        }
    }

    /**
     * 停止音乐
     */
    public void stop() {
        if (mp != null) {
            mp.stop();
            try {
                mp.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下一曲
     */
    public void next() {
        //暂停状态下或者播放中才可下一曲
        if (isPause || mp.isPlaying()) {
            //计算正确的歌曲id
            if (currentIndex + 1 != list_path.size()) {
                currentIndex = currentIndex + 1;
            } else {
                currentIndex = 0;
            }
            //播放
            isPause = false;
            play(currentIndex);
        }
    }

    /**
     * 上一曲
     */
    public void last() {
        //暂停状态下或者播放中才可上一曲
        if (isPause || mp.isPlaying()) {
            //计算正确的歌曲id
            if (currentIndex - 1 != -1) {
                currentIndex = currentIndex - 1;
            } else {
                currentIndex = list_path.size() - 1;
            }
            //播放
            isPause = false;
            play(currentIndex);
        }
    }

    /**
     * 销毁资源
     */
    public void destory() {
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
        }
    }

    /**
     * 获得当前音乐名称
     */
    public String getCurrentName() {
        if (!list_path.isEmpty()) {
            return list_path.get(currentIndex);
        }
        return null;
    }

    /**
     * 获得音乐列表
     */
    public List<String> getMusicList() {
        return list_path;
    }

    /**
     * 功能 通过album_id查找 album_art 如果找不到返回null
     *
     * @param album_id
     * @return album_art
     */
    private String getAlbumArt(int album_id, ContentResolver contentResolver) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = contentResolver.query(
                Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

    /**
     * 获得当前封面drawable
     *
     * @return BitmapDrawable
     */
    public BitmapDrawable getCurrentImg() {
        return list_img.get(currentIndex);
    }
}


