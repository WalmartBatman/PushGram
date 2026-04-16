package com.pushgram.app.music.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Playlist implements Serializable {
    private String id, name, coverUrl;
    private List<Song> songs;
    private long createdAt, updatedAt;

    public Playlist() { songs = new ArrayList<>(); }
    public Playlist(String name) {
        this.id = UUID.randomUUID().toString(); this.name = name;
        this.songs = new ArrayList<>(); this.createdAt = this.updatedAt = System.currentTimeMillis();
    }

    public void addSong(Song s) {
        if (songs == null) songs = new ArrayList<>();
        songs.add(s); updatedAt = System.currentTimeMillis();
        if (coverUrl == null && s.getAlbumArtUrl() != null) coverUrl = s.getAlbumArtUrl();
    }
    public void removeSong(int i) { if (songs != null && i >= 0 && i < songs.size()) { songs.remove(i); updatedAt = System.currentTimeMillis(); } }
    public int getSongCount() { return songs == null ? 0 : songs.size(); }
    public long getTotalDurationMs() { if (songs == null) return 0; long t = 0; for (Song s : songs) t += s.getDurationMs(); return t; }
    public String getFormattedDuration() { return getTotalDurationMs() / 60000 + " min"; }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public List<Song> getSongs() { return songs; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String u) { coverUrl = u; }
}
