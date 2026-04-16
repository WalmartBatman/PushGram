package com.pushgram.app.music.ui;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.pushgram.app.R;
import com.pushgram.app.music.model.Playlist;
import java.util.*;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.VH> {
    public interface OnClick { void onClick(Playlist p); }
    public interface OnLongClick { void onLongClick(Playlist p); }
    private final Context ctx;
    private List<Playlist> data;
    private final OnClick click; private final OnLongClick longClick;
    public PlaylistAdapter(Context ctx, List<Playlist> data, OnClick c, OnLongClick lc) { this.ctx=ctx; this.data=new ArrayList<>(data); this.click=c; this.longClick=lc; }
    public void updateData(List<Playlist> d) { this.data=new ArrayList<>(d); notifyDataSetChanged(); }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) { return new VH(LayoutInflater.from(ctx).inflate(R.layout.item_playlist, p, false)); }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Playlist p = data.get(pos);
        h.tvName.setText(p.getName());
        h.tvCount.setText(p.getSongCount()+" songs · "+p.getFormattedDuration());
        if (p.getCoverUrl()!=null&&!p.getCoverUrl().isEmpty()) Glide.with(ctx).load(p.getCoverUrl()).centerCrop().placeholder(android.R.drawable.ic_media_play).into(h.ivCover);
        else h.ivCover.setImageResource(android.R.drawable.ic_media_play);
        h.itemView.setOnClickListener(v->click.onClick(p));
        h.itemView.setOnLongClickListener(v->{longClick.onLongClick(p);return true;});
    }
    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCover; TextView tvName, tvCount;
        VH(View v) { super(v); ivCover=v.findViewById(R.id.ivPlaylistCover); tvName=v.findViewById(R.id.tvPlaylistName); tvCount=v.findViewById(R.id.tvPlaylistCount); }
    }
}
