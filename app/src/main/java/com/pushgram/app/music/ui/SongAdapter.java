package com.pushgram.app.music.ui;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.pushgram.app.R;
import com.pushgram.app.music.model.Song;
import java.util.*;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.VH> {
    public interface OnClick { void onClick(Song s, int i); }
    public interface OnLongClick { void onLongClick(Song s, int i); }
    private final Context ctx;
    private List<Song> data;
    private int currentIdx = -1;
    private final OnClick click; private final OnLongClick longClick;

    public SongAdapter(Context ctx, List<Song> data, OnClick c, OnLongClick lc) { this.ctx=ctx; this.data=new ArrayList<>(data); this.click=c; this.longClick=lc; }
    public void updateData(List<Song> d) { this.data=new ArrayList<>(d); notifyDataSetChanged(); }
    public void setCurrentPlayingIndex(int i) { int old=currentIdx; currentIdx=i; if(old>=0&&old<data.size())notifyItemChanged(old); if(i>=0&&i<data.size())notifyItemChanged(i); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) { return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_song, p, false)); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Song s = data.get(pos);
        h.tvTitle.setText(s.getTitle()); h.tvArtist.setText(s.getDisplaySubtitle()); h.tvDuration.setText(s.getFormattedDuration());
        h.tvSource.setText(s.getSource()==Song.Source.YOUTUBE?"▶ YT":"♫ SP");
        h.tvSource.setTextColor(s.getSource()==Song.Source.YOUTUBE?0xFFFF0000:0xFF1DB954);
        if(s.getAlbumArtUrl()!=null&&!s.getAlbumArtUrl().isEmpty()) Glide.with(ctx).load(s.getAlbumArtUrl()).centerCrop().placeholder(android.R.drawable.ic_media_play).into(h.ivArt);
        else h.ivArt.setImageResource(android.R.drawable.ic_media_play);
        h.tvTitle.setTextColor(pos==currentIdx?0xFFFF6B35:0xFFFFFFFF);
        h.itemView.setAlpha(pos==currentIdx?1f:0.85f);
        h.itemView.setOnClickListener(v->click.onClick(s,pos));
        h.itemView.setOnLongClickListener(v->{longClick.onLongClick(s,pos);return true;});
    }
    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        ImageView ivArt; TextView tvTitle, tvArtist, tvDuration, tvSource;
        VH(View v) { super(v); ivArt=v.findViewById(R.id.ivSongArt); tvTitle=v.findViewById(R.id.tvSongTitle); tvArtist=v.findViewById(R.id.tvSongArtist); tvDuration=v.findViewById(R.id.tvSongDuration); tvSource=v.findViewById(R.id.tvSongSource); }
    }
}
