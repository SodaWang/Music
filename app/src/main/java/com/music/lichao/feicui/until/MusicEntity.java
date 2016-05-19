package com.music.lichao.feicui.until;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2016/5/19.
 * MP3信息实体类
 */
public class MusicEntity {
    private String title;//歌名
    private String path;//文件路径
    private BitmapDrawable album_img;//专辑封面
    private String art;//歌手

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public BitmapDrawable getAlbum_img() {
        return album_img;
    }

    public void setAlbum_img(BitmapDrawable album_img) {
        this.album_img = album_img;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }
}
