package com.music.lichao.feicui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.lichao.feicui.R;

import java.util.List;


/**
 * 音乐列表适配器
 * Created by z on 2016/5/10.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {


    //列表数据源
    private List<String> mMusicList;
    //上下文
    private Context mContext;
    //点击事件监听器
    private OnItemClickLitener mOnItemClickLitener;

    public List<String> getmMusicList() {
        return mMusicList;
    }

    public void setmMusicList(List<String> mMusicList) {
        this.mMusicList = mMusicList;
    }

    //点击事件接口
    public interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    //设置点击接口
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    /**
     * init 初始化适配器，载入数据源
     *
     * @param context   上下文
     * @param musicList 数据源
     */
    public MusicAdapter(Context context, List<String> musicList) {
        mMusicList = musicList;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //加载单项内容布局
        MyViewHolder holder = new MyViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.recycleritem_music, parent, false));
        return holder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        //设置列表项文本
        holder.tv_music.setText(mMusicList.get(position));

        //设置点击监听
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    class MyViewHolder extends ViewHolder {

        /**
         * 音乐名称
         */
        TextView tv_music;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_music = (TextView) itemView.findViewById(R.id.tv_music);

        }
    }
}