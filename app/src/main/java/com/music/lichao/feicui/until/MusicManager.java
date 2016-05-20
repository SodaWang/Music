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
import com.music.lichao.feicui.component.FreshUIListener;

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
    //音乐信息列表
    private List<MusicEntity> list_music;
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


    //刷新监听接口
    FreshUIListener mFreshUIListener;

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
    public void setFreshUIListener(FreshUIListener freshUIListener) {
        this.mFreshUIListener = freshUIListener;
    }

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
    public List<MusicEntity> scanMusic(ContentResolver contentResolver) {
        //初始化音乐信息列表
        if (list_music == null) {
            list_music = new ArrayList<MusicEntity>();
        } else {
            list_music.clear();
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
                MusicEntity entity = new MusicEntity();
                //得到歌曲文件的路径
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //保存到列表中
                entity.setPath(url);
                //得到歌曲名字
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //保存到列表中
                entity.setTitle(name);
                //查询歌曲id
                int album_id = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                //根据歌曲id查询封面路径
                String albumArt = getAlbumArt(album_id, contentResolver);
                Bitmap bm = null;
                if (albumArt == null) {
                    bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);//默认封面图片
                    BitmapDrawable bmDrawable = new BitmapDrawable(mContext.getResources(), bm);
                    //保存专辑封面
                    entity.setAlbum_img(bmDrawable);
                } else {
                    bm = BitmapFactory.decodeFile(albumArt);
                    BitmapDrawable bmDrawable = new BitmapDrawable(mContext.getResources(), bm);//查询到的专辑封面
                    //保存专辑封面
                    entity.setAlbum_img(bmDrawable);
                }
                //查询歌曲歌手名字
                String art = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //保存歌曲歌手名字
                entity.setArt(art);
                list_music.add(entity);//将获得到的单曲信息加到列表中
            } while (cursor.moveToNext());
        }
        return list_music;
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
                if (list_music.get(id).getPath() != null && !list_music.get(id).getPath().isEmpty()) {
                    mp.reset();//把各项参数恢复到初始状态
                    mp.setDataSource(list_music.get(id).getPath());
                    mp.prepare();  //进行缓冲
                    mp.start();//播放
                    //更改通知栏音乐名字
                    if (mRemoteViews != null)
                        mRemoteViews.setTextViewText(R.id.tv_notify_name, list_music.get(id).getTitle());
                    if (notifycationManager != null && notifycation != null) {
                        notifycationManager.notify(0, notifycation);
                    }
                } else {
                    Log.e("lichao", "音乐列表读取失败！");
                }
                //刷新UI控件
                freshUI(mFreshUIListener);
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
        //计算正确的歌曲id
        if (currentIndex + 1 != list_music.size()) {
            currentIndex = currentIndex + 1;
        } else {
            currentIndex = 0;
        }
        //播放
        isPause = false;
        play(currentIndex);
    }

    /**
     * 上一曲
     */
    public void last() {
        //计算正确的歌曲id
        if (currentIndex - 1 != -1) {
            currentIndex = currentIndex - 1;
        } else {
            currentIndex = list_music.size() - 1;
        }
        //播放
        isPause = false;
        play(currentIndex);
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
        if (!list_music.isEmpty()) {
            return list_music.get(currentIndex).getTitle();
        }
        return null;
    }

    /**
     * 获得音乐列表
     */
    public List<MusicEntity> getMusicList() {
        return list_music;
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
        return list_music.get(currentIndex).getAlbum_img();
    }

    /**
     * 刷新控件UI
     */
    private void freshUI(FreshUIListener freshUIListener) {
        freshUIListener.freshUI();
    }


}


