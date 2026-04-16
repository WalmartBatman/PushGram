package com.pushgram.app.music.model;

import java.io.Serializable;

public class Song implements Serializable {
    public enum Source { YOUTUBE, SPOTIFY }
    private String id, title, artist, albumArtUrl, streamUrl, previewUrl, playlistId;
    private long durationMs, addedAt;
    private Source source;

    public Song() {}
    public Song(String id, Source source, String title, String artist,
                String albumArtUrl, long durationMs, String playlistId) {
        this.id = id; this.source = source; this.title = title; this.artist = artist;
        this.albumArtUrl = albumArtUrl; this.durationMs = durationMs;
        this.playlistId = playlistId; this.addedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public Source getSource() { return source; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbumArtUrl() { return albumArtUrl; }
    public long getDurationMs() { return durationMs; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String u) { streamUrl = u; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String u) { previewUrl = u; }
    public String getPlaylistId() { return playlistId; }
    public long getAddedAt() { return addedAt; }
    public String getDisplaySubtitle() { return artist != null && !artist.isEmpty() ? artist : (source == Source.YOUTUBE ? "YouTube" : "Spotify"); }
    public String getFormattedDuration() {
        if (durationMs <= 0) return "";
        long s = durationMs / 1000;
        return String.format("%d:%02d", s/60, s%60);
    }
}
