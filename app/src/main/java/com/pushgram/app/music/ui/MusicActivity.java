package com.pushgram.app.music.ui;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.pushgram.app.databinding.ActivityMusicBinding;
import com.pushgram.app.music.model.*;
import com.pushgram.app.music.service.MusicService;

import java.util.*;
import java.util.concurrent.*;

public class MusicActivity extends AppCompatActivity implements MusicService.StateListener {

    private ActivityMusicBinding binding;
    private PlaylistStore store;
    private PlaylistAdapter playlistAdapter;
    private MusicService musicService;
    private boolean bound = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = this::updateProgress;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MusicBinder) service).getService();
            musicService.setStateListener(MusicActivity.this);
            bound = true; refreshNowPlaying();
        }
        @Override public void onServiceDisconnected(ComponentName name) { bound=false; musicService=null; }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        store = PlaylistStore.getInstance(this);
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnNewPlaylist.setOnClickListener(v -> showCreateDialog());
        binding.btnImport.setOnClickListener(v -> showImportDialog());
        setupList();
        setupNowPlaying();
        Intent svc = new Intent(this, MusicService.class);
        startService(svc); bindService(svc, connection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onResume() { super.onResume(); refreshPlaylists(); }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (bound) { if(musicService!=null)musicService.setStateListener(null); unbindService(connection); }
        mainHandler.removeCallbacks(progressUpdater);
    }

    private void setupList() {
        playlistAdapter = new PlaylistAdapter(this, new ArrayList<>(),
                p -> { Intent i=new Intent(this, PlaylistDetailActivity.class); i.putExtra("playlist_id",p.getId()); startActivity(i); },
                p -> { new AlertDialog.Builder(this).setTitle(p.getName()).setItems(new String[]{"Delete"},(d,w)->{store.deletePlaylist(p.getId());refreshPlaylists();}).show(); });
        binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlaylists.setAdapter(playlistAdapter);
    }

    private void setupNowPlaying() {
        binding.btnPlayPause.setOnClickListener(v -> { if(bound&&musicService!=null) musicService.playPause(); });
        binding.btnNext.setOnClickListener(v -> { if(bound&&musicService!=null) musicService.next(); });
        binding.btnPrev.setOnClickListener(v -> { if(bound&&musicService!=null) musicService.prev(); });
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar sb,int p,boolean u){if(u&&bound&&musicService!=null)musicService.seekTo(p);}
            @Override public void onStartTrackingTouch(SeekBar sb){}
            @Override public void onStopTrackingTouch(SeekBar sb){}
        });
    }

    private void refreshPlaylists() {
        List<Playlist> all = store.getAll();
        playlistAdapter.updateData(all);
        binding.emptyState.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvPlaylists.setVisibility(all.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvTotalStats.setText(store.getTotalSongCount()+" songs across "+all.size()+" playlists");
    }

    private void showCreateDialog() {
        EditText input = new EditText(this); input.setHint("Playlist name"); input.setPadding(48,32,48,8);
        new AlertDialog.Builder(this).setTitle("New Playlist").setView(input)
                .setPositiveButton("Create",(d,w)->{String n=input.getText().toString().trim();if(!n.isEmpty()){store.createPlaylist(new Playlist(n));refreshPlaylists();}})
                .setNegativeButton("Cancel",null).show();
    }

    private void showImportDialog() {
        List<Playlist> playlists = store.getAll();
        if (playlists.isEmpty()) { Toast.makeText(this,"Create a playlist first!",Toast.LENGTH_SHORT).show(); return; }
        View view = LayoutInflater.from(this).inflate(com.pushgram.app.R.layout.dialog_import, null);
        EditText etUrl = view.findViewById(com.pushgram.app.R.id.etImportUrl);
        Spinner spinner = view.findViewById(com.pushgram.app.R.id.spinnerPlaylist);
        RadioGroup rg = view.findViewById(com.pushgram.app.R.id.rgSource);
        String[] names = playlists.stream().map(Playlist::getName).toArray(String[]::new);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        new AlertDialog.Builder(this).setTitle("Import Playlist / Song").setView(view)
                .setPositiveButton("Import",(d,w)->{
                    String url = etUrl.getText().toString().trim();
                    boolean isSpotify = rg.getCheckedRadioButtonId() == com.pushgram.app.R.id.rbSpotify;
                    String pid = playlists.get(spinner.getSelectedItemPosition()).getId();
                    if(!url.isEmpty()) startImport(url, isSpotify, pid);
                    else Toast.makeText(this,"Enter a URL",Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancel",null).show();
    }

    private void startImport(String url, boolean isSpotify, String targetId) {
        binding.progressImport.setVisibility(View.VISIBLE);
        binding.tvImportStatus.setVisibility(View.VISIBLE);
        binding.tvImportStatus.setText("Importing...");
        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            try {
                List<Song> songs;
                if (isSpotify) {
                    com.pushgram.app.music.api.SpotifyApiClient sp = new com.pushgram.app.music.api.SpotifyApiClient(
                            getString(com.pushgram.app.R.string.spotify_client_id),
                            getString(com.pushgram.app.R.string.spotify_client_secret));
                    try { songs=sp.importPlaylist(url,targetId); } catch(Exception e) { songs=new ArrayList<>(); songs.add(sp.importTrack(url,targetId)); }
                } else {
                    com.pushgram.app.music.api.YouTubeApiClient yt = new com.pushgram.app.music.api.YouTubeApiClient(getString(com.pushgram.app.R.string.youtube_api_key));
                    try { songs=yt.importPlaylist(url,targetId); } catch(Exception e) { songs=new ArrayList<>(); songs.add(yt.importSingleVideo(url,targetId)); }
                }
                final int count = songs.size();
                for (Song s : songs) store.addSongToPlaylist(targetId, s);
                mainHandler.post(() -> {
                    binding.progressImport.setVisibility(View.GONE);
                    binding.tvImportStatus.setVisibility(View.GONE);
                    refreshPlaylists();
                    Toast.makeText(this,"✅ Imported "+count+" song"+(count==1?"":"s")+"!",Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.progressImport.setVisibility(View.GONE);
                    binding.tvImportStatus.setVisibility(View.GONE);
                    Toast.makeText(this,"Import failed: "+e.getMessage(),Toast.LENGTH_LONG).show();
                });
            } finally {
                ex.shutdown();
            }
        });
    }

    private void refreshNowPlaying() {
        if (!bound || musicService == null) return;
        Song song = musicService.getCurrentSong();
        if (song != null) showNowPlayingBar(song, musicService.isPlaying());
    }

    private void showNowPlayingBar(Song song, boolean playing) {
        binding.nowPlayingBar.setVisibility(View.VISIBLE);
        binding.tvNowPlayingTitle.setText(song.getTitle());
        binding.tvNowPlayingArtist.setText(song.getDisplaySubtitle());
        binding.btnPlayPause.setText(playing ? "⏸" : "▶");
        if (song.getAlbumArtUrl() != null && !song.getAlbumArtUrl().isEmpty())
            Glide.with(this).load(song.getAlbumArtUrl()).placeholder(android.R.drawable.ic_media_play).into(binding.ivNowPlayingArt);
        int dur = musicService.getDuration();
        if (dur > 0) binding.seekBar.setMax(dur);
        mainHandler.removeCallbacks(progressUpdater);
        if (playing) mainHandler.post(progressUpdater);
    }

    private void updateProgress() {
        if (bound && musicService != null && musicService.isPlaying()) {
            binding.seekBar.setProgress(musicService.getCurrentPosition());
            mainHandler.postDelayed(progressUpdater, 500);
        }
    }

    @Override public void onSongChanged(Song s) { if(s!=null) showNowPlayingBar(s,true); }
    @Override public void onPlayStateChanged(boolean p) {
        binding.btnPlayPause.setText(p?"⏸":"▶");
        mainHandler.removeCallbacks(progressUpdater);
        if(p) mainHandler.post(progressUpdater);
    }
    @Override public void onLoading(Song s) { binding.tvNowPlayingTitle.setText("Loading: "+s.getTitle()); binding.nowPlayingBar.setVisibility(View.VISIBLE); }
    @Override public void onError(String msg) { Toast.makeText(this,"⚠️ "+msg,Toast.LENGTH_LONG).show(); }
}
