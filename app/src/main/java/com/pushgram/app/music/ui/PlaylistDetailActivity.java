package com.pushgram.app.music.ui;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.pushgram.app.databinding.ActivityPlaylistDetailBinding;
import com.pushgram.app.music.model.*;
import com.pushgram.app.music.service.MusicService;

public class PlaylistDetailActivity extends AppCompatActivity implements MusicService.StateListener {
    private ActivityPlaylistDetailBinding binding;
    private PlaylistStore store;
    private Playlist playlist;
    private SongAdapter songAdapter;
    private MusicService musicService;
    private boolean bound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName n, IBinder s) { musicService=((MusicService.MusicBinder)s).getService(); musicService.setStateListener(PlaylistDetailActivity.this); bound=true; }
        @Override public void onServiceDisconnected(ComponentName n) { bound=false; musicService=null; }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        store = PlaylistStore.getInstance(this);
        playlist = store.getById(getIntent().getStringExtra("playlist_id"));
        if (playlist == null) { finish(); return; }
        setupUI();
        bindService(new Intent(this, MusicService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if(bound){if(musicService!=null)musicService.setStateListener(null);unbindService(connection);}
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v->finish());
        binding.tvPlaylistName.setText(playlist.getName());
        binding.tvPlaylistInfo.setText(playlist.getSongCount()+" songs · "+playlist.getFormattedDuration());
        binding.btnPlayAll.setOnClickListener(v->{ if(!playlist.getSongs().isEmpty())playFrom(0); else Toast.makeText(this,"No songs",Toast.LENGTH_SHORT).show();});
        binding.btnShuffle.setOnClickListener(v->{ if(!playlist.getSongs().isEmpty()){if(bound&&musicService!=null)musicService.setShuffle(true);playFrom((int)(Math.random()*playlist.getSongs().size()));}});
        songAdapter = new SongAdapter(this, playlist.getSongs(),
                (s,i)->playFrom(i),
                (s,i)->new AlertDialog.Builder(this).setTitle(s.getTitle()).setItems(new String[]{"Remove"},(d,w)->{store.removeSongFromPlaylist(playlist.getId(),i);playlist=store.getById(playlist.getId());songAdapter.updateData(playlist.getSongs());binding.tvPlaylistInfo.setText(playlist.getSongCount()+" songs · "+playlist.getFormattedDuration());}).show());
        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.setAdapter(songAdapter);
    }

    private void playFrom(int i) {
        if(!bound||musicService==null){Toast.makeText(this,"Service not ready",Toast.LENGTH_SHORT).show();return;}
        musicService.setQueue(playlist.getSongs(),i);
        songAdapter.setCurrentPlayingIndex(i);
    }

    @Override public void onSongChanged(Song s) { if(s==null)return; for(int i=0;i<playlist.getSongs().size();i++){if(playlist.getSongs().get(i).getId().equals(s.getId())){songAdapter.setCurrentPlayingIndex(i);return;}} }
    @Override public void onPlayStateChanged(boolean p) {}
    @Override public void onLoading(Song s) {}
    @Override public void onError(String m) { Toast.makeText(this,m,Toast.LENGTH_SHORT).show(); }
}
