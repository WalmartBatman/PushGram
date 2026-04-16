package com.pushgram.app.music.api;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pushgram.app.music.model.Song;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YouTubeApiClient {
    private static final String TAG = "YouTubeApiClient";
    private static final String BASE = "https://www.googleapis.com/youtube/v3";
    private static final String OEMBED = "https://www.youtube.com/oembed?url=";
    private final OkHttpClient http;
    private final String apiKey;

    public YouTubeApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
    }

    public List<Song> importPlaylist(String urlOrId, String targetPlaylistId) throws IOException {
        String pid = extractPlaylistId(urlOrId);
        if (pid == null) throw new IOException("Invalid playlist URL/ID");
        List<Song> songs = new ArrayList<>();
        String pageToken = null;
        do {
            String url = BASE + "/playlistItems?part=snippet&maxResults=50&playlistId=" + pid +
                    "&key=" + apiKey + (pageToken != null ? "&pageToken=" + pageToken : "");
            JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
            JsonArray items = root.getAsJsonArray("items");
            if (items == null) break;
            for (JsonElement item : items) {
                try {
                    JsonObject sn = item.getAsJsonObject().getAsJsonObject("snippet");
                    String vid = sn.getAsJsonObject("resourceId").get("videoId").getAsString();
                    String title = sn.get("title").getAsString();
                    if (title.equals("Deleted video") || title.equals("Private video")) continue;
                    String channel = sn.has("videoOwnerChannelTitle") ? sn.get("videoOwnerChannelTitle").getAsString() : "";
                    String thumb = "";
                    JsonObject thumbs = sn.getAsJsonObject("thumbnails");
                    if (thumbs != null) { JsonObject m = thumbs.getAsJsonObject("medium"); if (m != null) thumb = m.get("url").getAsString(); }
                    songs.add(new Song(vid, Song.Source.YOUTUBE, title, channel, thumb, 0, targetPlaylistId));
                } catch (Exception e) { Log.w(TAG, "Skip: " + e.getMessage()); }
            }
            pageToken = root.has("nextPageToken") ? root.get("nextPageToken").getAsString() : null;
        } while (pageToken != null);
        return songs;
    }

    public Song importSingleVideo(String urlOrId, String targetPlaylistId) throws IOException {
        String vid = extractVideoId(urlOrId);
        if (vid == null) throw new IOException("Invalid video URL/ID");
        String url = OEMBED + URLEncoder.encode("https://www.youtube.com/watch?v=" + vid, "UTF-8") + "&format=json";
        JsonObject root = JsonParser.parseString(get(url)).getAsJsonObject();
        String title = root.has("title") ? root.get("title").getAsString() : "YouTube Video";
        String author = root.has("author_name") ? root.get("author_name").getAsString() : "";
        String thumb = root.has("thumbnail_url") ? root.get("thumbnail_url").getAsString() : "";
        return new Song(vid, Song.Source.YOUTUBE, title, author, thumb, 0, targetPlaylistId);
    }

    public String resolveAudioStreamUrl(String videoId) throws IOException {
        String url = "https://www.youtube.com/youtubei/v1/player?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
        String body = "{\"context\":{\"client\":{\"clientName\":\"ANDROID_MUSIC\",\"clientVersion\":\"5.16.51\",\"androidSdkVersion\":30}},\"videoId\":\"" + videoId + "\"}";
        okhttp3.RequestBody rb = okhttp3.RequestBody.create(body, okhttp3.MediaType.parse("application/json"));
        Request req = new Request.Builder().url(url).post(rb)
                .addHeader("User-Agent", "com.google.android.apps.youtube.music/5.16.51").build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("Player API error: " + resp.code());
            JsonObject root = JsonParser.parseString(resp.body().string()).getAsJsonObject();
            JsonObject sd = root.getAsJsonObject("streamingData");
            if (sd == null) throw new IOException("No streaming data");
            JsonArray af = sd.getAsJsonArray("adaptiveFormats");
            if (af == null) throw new IOException("No adaptive formats");
            String fallback = null;
            for (JsonElement fmt : af) {
                JsonObject f = fmt.getAsJsonObject();
                if (!f.has("url")) continue;
                int itag = f.get("itag").getAsInt();
                String mime = f.has("mimeType") ? f.get("mimeType").getAsString() : "";
                if (itag == 140) return f.get("url").getAsString();
                if (mime.contains("audio") && fallback == null) fallback = f.get("url").getAsString();
            }
            if (fallback != null) return fallback;
            throw new IOException("No audio stream found");
        }
    }

    private String get(String url) throws IOException {
        Request req = new Request.Builder().url(url).addHeader("Accept-Encoding","gzip").build();
        try (Response resp = http.newCall(req).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
            return resp.body().string();
        }
    }

    public static String extractPlaylistId(String in) {
        if (in == null) return null;
        in = in.trim();
        if (in.matches("^[A-Za-z0-9_-]{13,}$") && !in.contains("/")) return in;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[?&]list=([A-Za-z0-9_-]+)").matcher(in);
        return m.find() ? m.group(1) : null;
    }

    public static String extractVideoId(String in) {
        if (in == null) return null;
        in = in.trim();
        if (in.matches("^[A-Za-z0-9_-]{11}$")) return in;
        java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{11})").matcher(in);
        if (m1.find()) return m1.group(1);
        java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("[?&]v=([A-Za-z0-9_-]{11})").matcher(in);
        return m2.find() ? m2.group(1) : null;
    }
}
