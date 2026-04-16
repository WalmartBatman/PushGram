package com.pushgram.app.music.api;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pushgram.app.music.model.Song;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class SpotifyApiClient {
    private static final String TAG = "SpotifyApiClient";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String API_BASE  = "https://api.spotify.com/v1";
    private final OkHttpClient http;
    private final String clientId, clientSecret;
    private String accessToken;
    private long tokenExpiresAt = 0;

    public SpotifyApiClient(String clientId, String clientSecret) {
        this.clientId = clientId; this.clientSecret = clientSecret;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
    }

    private synchronized void ensureToken() throws IOException {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiresAt - 60000) return;
        String creds = Base64.getEncoder().encodeToString((clientId+":"+clientSecret).getBytes(StandardCharsets.UTF_8));
        RequestBody body = new FormBody.Builder().add("grant_type","client_credentials").build();
        Request req = new Request.Builder().url(TOKEN_URL).post(body)
                .addHeader("Authorization","Basic "+creds).build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("Token error: "+resp.code());
            JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
            accessToken = json.get("access_token").getAsString();
            tokenExpiresAt = System.currentTimeMillis() + json.get("expires_in").getAsInt() * 1000L;
        }
    }

    public List<Song> importPlaylist(String urlOrId, String targetId) throws IOException {
        ensureToken();
        String pid = extractPlaylistId(urlOrId);
        if (pid == null) throw new IOException("Invalid Spotify playlist URL/ID");
        List<Song> songs = new ArrayList<>();
        int offset = 0;
        while (true) {
            String url = API_BASE+"/playlists/"+pid+"/tracks?limit=50&offset="+offset+
                    "&fields=next,items(track(id,name,preview_url,duration_ms,artists(name),album(images)))";
            JsonObject root = JsonParser.parseString(getAuthed(url)).getAsJsonObject();
            JsonArray items = root.getAsJsonArray("items");
            if (items == null || items.size() == 0) break;
            for (JsonElement item : items) {
                try {
                    JsonObject track = item.getAsJsonObject().getAsJsonObject("track");
                    if (track == null || track.get("id").isJsonNull()) continue;
                    String tid = track.get("id").getAsString();
                    String title = track.get("name").getAsString();
                    long dur = track.get("duration_ms").getAsLong();
                    JsonArray artists = track.getAsJsonArray("artists");
                    StringBuilder ab = new StringBuilder();
                    for (int i=0;i<artists.size();i++) { if(i>0) ab.append(", "); ab.append(artists.get(i).getAsJsonObject().get("name").getAsString()); }
                    String thumb = "";
                    JsonArray imgs = track.getAsJsonObject("album").getAsJsonArray("images");
                    if (imgs != null && imgs.size()>0) thumb = imgs.get(Math.max(0,imgs.size()-2)).getAsJsonObject().get("url").getAsString();
                    String preview = null;
                    if (!track.get("preview_url").isJsonNull()) preview = track.get("preview_url").getAsString();
                    Song s = new Song(tid, Song.Source.SPOTIFY, title, ab.toString(), thumb, dur, targetId);
                    if (preview != null) s.setPreviewUrl(preview);
                    songs.add(s);
                } catch (Exception e) { Log.w(TAG, "Skip: "+e.getMessage()); }
            }
            if (root.get("next").isJsonNull()) break;
            offset += 50;
        }
        return songs;
    }

    public Song importTrack(String urlOrId, String targetId) throws IOException {
        ensureToken();
        String tid = extractTrackId(urlOrId);
        if (tid == null) throw new IOException("Invalid Spotify track URL/ID");
        JsonObject track = JsonParser.parseString(getAuthed(API_BASE+"/tracks/"+tid)).getAsJsonObject();
        String title = track.get("name").getAsString();
        long dur = track.get("duration_ms").getAsLong();
        JsonArray artists = track.getAsJsonArray("artists");
        String artist = artists.size()>0 ? artists.get(0).getAsJsonObject().get("name").getAsString() : "";
        String thumb = "";
        JsonArray imgs = track.getAsJsonObject("album").getAsJsonArray("images");
        if (imgs != null && imgs.size()>0) thumb = imgs.get(Math.max(0,imgs.size()-2)).getAsJsonObject().get("url").getAsString();
        String preview = null;
        if (!track.get("preview_url").isJsonNull()) preview = track.get("preview_url").getAsString();
        Song s = new Song(tid, Song.Source.SPOTIFY, title, artist, thumb, dur, targetId);
        if (preview != null) s.setPreviewUrl(preview);
        return s;
    }

    public static String buildSpotifyDeepLink(String trackId) { return "spotify:track:" + trackId; }

    private String getAuthed(String url) throws IOException {
        Request req = new Request.Builder().url(url)
                .addHeader("Authorization","Bearer "+accessToken)
                .addHeader("Accept-Encoding","gzip").build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("API error "+resp.code());
            return resp.body().string();
        }
    }

    public static String extractPlaylistId(String in) {
        if (in==null) return null; in=in.trim();
        if (in.matches("^[A-Za-z0-9]{22}$")) return in;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("playlist[:/]([A-Za-z0-9]{22})").matcher(in);
        return m.find() ? m.group(1) : null;
    }

    public static String extractTrackId(String in) {
        if (in==null) return null; in=in.trim();
        if (in.matches("^[A-Za-z0-9]{22}$")) return in;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("track[:/]([A-Za-z0-9]{22})").matcher(in);
        return m.find() ? m.group(1) : null;
    }
}
