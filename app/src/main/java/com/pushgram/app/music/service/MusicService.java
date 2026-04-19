package com.pushgram.app.music.service;

import android.app.*;
import android.content.Intent;
import android.media.*;
import android.os.*;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.pushgram.app.music.model.Song;
import com.pushgram.app.music.api.SpotifyApiClient;
import com.pushgram.app.music.api.YouTubeApiClient;
import com.pushgram.app.music.ui.MusicActivity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    public static final String CHANNEL_ID = "pushgram_music";
    public static final int NOTIF_ID = 42;
    public static final String ACTION_PLAY_PAUSE = "com.pushgram.PLAY_PAUSE";
    public static final String ACTION_NEXT       = "com.pushgram.NEXT";
    public static final String ACTION_PREV       = "com.pushgram.PREV";
    public static final String ACTION_STOP       = "com.pushgram.STOP";

    private MediaPlayer mediaPlayer;
    private List<Song> queue = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isPlaying = false, shuffle = false, repeat = false;
    private final IBinder binder = new MusicBinder();
    private StateListener stateListener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private YouTubeApiClient ytClient;
    private SpotifyApiClient spClient;
    // Cached on main thread in onCreate() so background threads never call getString()
    private String cachedYtApiKey;
    private String cachedSpClientId;
    private String cachedSpClientSecret;

    public interface StateListener {
        void onSongChanged(Song song);
        void onPlayStateChanged(boolean playing);
        void onLoading(Song song);
        void onError(String message);
    }

    @Override public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        createNotificationChannel();
        mediaSession = new MediaSessionCompat(this, "PushGramMusic");
        mediaSession.setActive(true);
        // Cache API keys and initialize clients on the main thread (getString is not thread-safe
        // in all contexts and ytClient/spClient had a check-then-act race on the executor thread)
        try {
            cachedYtApiKey       = getString(com.pushgram.app.R.string.youtube_api_key);
            cachedSpClientId     = getString(com.pushgram.app.R.string.spotify_client_id);
            cachedSpClientSecret = getString(com.pushgram.app.R.string.spotify_client_secret);
            ytClient = new YouTubeApiClient(cachedYtApiKey);
            spClient = new SpotifyApiClient(cachedSpClientId, cachedSpClientSecret);
        } catch (Exception e) {
            android.util.Log.w(TAG, "API keys not configured: " + e.getMessage());
        }
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) handleAction(intent.getAction());
        return START_STICKY;
    }

    @Override public IBinder onBind(Intent intent) { return binder; }

    @Override public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (mediaSession != null) mediaSession.release();
        executor.shutdown();
    }

    public void setQueue(List<Song> songs, int startIndex) {
        queue = new ArrayList<>(songs); currentIndex = startIndex; playCurrentSong();
    }

    public void playPause() {
        if (mediaPlayer == null) { playCurrentSong(); return; }
        if (mediaPlayer.isPlaying()) { mediaPlayer.pause(); isPlaying = false; }
        else { mediaPlayer.start(); isPlaying = true; }
        updateNotification(); notifyStateChanged();
    }

    public void next() {
        if (queue.isEmpty()) return;
        currentIndex = shuffle ? (int)(Math.random()*queue.size()) : (currentIndex+1)%queue.size();
        playCurrentSong();
    }

    public void prev() {
        if (queue.isEmpty()) return;
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) mediaPlayer.seekTo(0);
        else { currentIndex = (currentIndex-1+queue.size())%queue.size(); playCurrentSong(); }
    }

    public void seekTo(int ms) { if (mediaPlayer != null) mediaPlayer.seekTo(ms); }
    public void setShuffle(boolean on) { shuffle = on; }
    public void setRepeat(boolean on) { repeat = on; }
    public boolean isPlaying() { return mediaPlayer != null && mediaPlayer.isPlaying(); }
    public int getCurrentPosition() { return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0; }
    public int getDuration() { return mediaPlayer != null ? mediaPlayer.getDuration() : 0; }
    public Song getCurrentSong() { return (currentIndex>=0 && currentIndex<queue.size()) ? queue.get(currentIndex) : null; }
    public List<Song> getQueue() { return Collections.unmodifiableList(queue); }
    public int getCurrentIndex() { return currentIndex; }
    public void setStateListener(StateListener l) { stateListener = l; }

    private void playCurrentSong() {
        if (queue.isEmpty() || currentIndex < 0) return;
        Song song = queue.get(currentIndex);
        notifyLoading(song);
        executor.execute(() -> {
            try {
                String url = resolveStreamUrl(song);
                if (url == null) return;
                mainHandler.post(() -> startPlayback(url, song));
            } catch (Exception e) {
                Log.e(TAG, "Stream resolve failed", e);
                mainHandler.post(() -> notifyError("Could not play: " + song.getTitle()));
            }
        });
    }

    private String resolveStreamUrl(Song song) throws IOException {
        switch (song.getSource()) {
            case YOUTUBE: {
                // ytClient initialized in onCreate() on main thread — safe to use here
                if (ytClient == null) throw new IOException("YouTube client not initialized — check youtube_api_key in strings.xml");
                String url = ytClient.resolveAudioStreamUrl(song.getId());
                song.setStreamUrl(url); return url;
            }
            case SPOTIFY:
                if (song.getPreviewUrl() != null) return song.getPreviewUrl();
                mainHandler.post(() -> {
                    Intent i = new Intent(Intent.ACTION_VIEW)
                            .setData(android.net.Uri.parse(SpotifyApiClient.buildSpotifyDeepLink(song.getId())))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try { startActivity(i); } catch (Exception e) { notifyError("Spotify app not installed"); }
                });
                return null;
            default: return null;
        }
    }

    private void startPlayback(String url, Song song) {
        releasePlayer();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(mp -> { mp.start(); isPlaying=true; notifyStateChanged(); updateNotification(); });
            mediaPlayer.setOnCompletionListener(mp -> { if(repeat){mp.seekTo(0);mp.start();}else next(); });
            mediaPlayer.setOnErrorListener((mp,w,e) -> { notifyError("Playback error"); return true; });
            mediaPlayer.prepareAsync();
            startForeground(NOTIF_ID, buildNotification(song));
        } catch (IOException e) { notifyError("Playback failed: "+e.getMessage()); }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) { try { mediaPlayer.stop(); mediaPlayer.release(); } catch (Exception ignored) {} mediaPlayer=null; isPlaying=false; }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,"PushGram Music",NotificationManager.IMPORTANCE_LOW);
            ch.setSound(null,null);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(Song song) {
        Intent open = new Intent(this, MusicActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent opi = PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(song!=null?song.getTitle():"No track")
                .setContentText(song!=null?song.getDisplaySubtitle():"")
                .setContentIntent(opi)
                .addAction(android.R.drawable.ic_media_previous,"Prev",actionIntent(ACTION_PREV,1))
                .addAction(isPlaying?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,isPlaying?"Pause":"Play",actionIntent(ACTION_PLAY_PAUSE,2))
                .addAction(android.R.drawable.ic_media_next,"Next",actionIntent(ACTION_NEXT,3))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2).setMediaSession(mediaSession.getSessionToken()))
                .setOngoing(isPlaying).setPriority(NotificationCompat.PRIORITY_LOW).setSilent(true).build();
    }

    private void updateNotification() { ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIF_ID,buildNotification(getCurrentSong())); }

    private PendingIntent actionIntent(String action, int rc) {
        Intent i = new Intent(this,MusicService.class).setAction(action);
        return PendingIntent.getService(this,rc,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }

    private void handleAction(String action) {
        switch(action){
            case ACTION_PLAY_PAUSE: playPause(); break;
            case ACTION_NEXT: next(); break;
            case ACTION_PREV: prev(); break;
            case ACTION_STOP: releasePlayer(); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { stopForeground(STOP_FOREGROUND_REMOVE); } else { stopForeground(true); } stopSelf(); break;
        }
    }

    private void notifyStateChanged() { if(stateListener!=null){stateListener.onPlayStateChanged(isPlaying);stateListener.onSongChanged(getCurrentSong());} }
    private void notifyLoading(Song song) { if(stateListener!=null) stateListener.onLoading(song); }
    private void notifyError(String msg) { if(stateListener!=null) stateListener.onError(msg); }

    public class MusicBinder extends Binder { public MusicService getService(){return MusicService.this;} }
}
