package com.music.lichao.feicui.component;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.music.lichao.feicui.R;
import com.music.lichao.feicui.until.MusicManager;

public class LockScreenActivity extends AppCompatActivity implements View.OnClickListener {
    //四个控制按键
    Button play, pause, next, last;
    //专辑封面
    ImageView img_album;
    //音乐管理器
    MusicManager mm = MusicManager.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //去掉锁屏界面
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED); //屏幕锁定时也能显示

        init();
    }

    /**
     * 加载组件
     */
    private void init() {
        play = (Button) findViewById(R.id.lock_play);
        pause = (Button) findViewById(R.id.lock_pause);
        next = (Button) findViewById(R.id.lock_right);
        last = (Button) findViewById(R.id.lock_left);
        img_album = (ImageView) findViewById(R.id.img_album);
        img_album.setBackground(mm.getCurrentImg());//设置默认封面

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //播放
            case R.id.lock_play:
                mm.play(mm.getCurrentIndex());
                break;
            //暂停
            case R.id.lock_pause:
                mm.pause();
                break;
            //下一曲
            case R.id.lock_right:
                mm.next();
                img_album.setBackground(mm.getCurrentImg());//刷新封面
                break;
            //上一曲
            case R.id.lock_left:
                mm.last();
                img_album.setBackground(mm.getCurrentImg());//刷新封面
                break;
        }
    }
}
