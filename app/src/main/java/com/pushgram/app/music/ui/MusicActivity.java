package com.pushgram.app.music.ui;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.pushgram.app.databinding.ActivityMusicBinding;
import com.pushgram.app.music.model.*;
import com.pushgram.app.music.service.MusicService;

import java.util.*;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Change #6 — Music import now uses account-based flow:
 *   Source selection → Username/password login → Playlist picker → Import
 *   (replaces previous URL-paste dialog)
 */
public class MusicActivity extends AppCompatActivity implements MusicService.StateListener {

    private ActivityMusicBinding binding;
    private PlaylistStore store;
    private PlaylistAdapter playlistAdapter;
    private MusicService musicService;
    private boolean bound = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressUpdater = this::updateProgress;

    // ── Service connection ──────────────────────────────────────────────────
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
        binding.btnImport.setOnClickListener(v -> showSourceSelector());   // Change #6
        setupList();
        setupNowPlaying();
        Intent svc = new Intent(this, MusicService.class);
        startService(svc); bindService(svc, connection, Context.BIND_AUTO_CREATE);
    }

    @Override protected void onResume()  { super.onResume();  refreshPlaylists(); }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (bound) { if(musicService!=null)musicService.setStateListener(null); unbindService(connection); }
        mainHandler.removeCallbacks(progressUpdater);
    }

    // ── List setup ──────────────────────────────────────────────────────────
    private void setupList() {
        playlistAdapter = new PlaylistAdapter(this, new ArrayList<>(),
                p -> { Intent i=new Intent(this,PlaylistDetailActivity.class); i.putExtra("playlist_id",p.getId()); startActivity(i); },
                p -> new AlertDialog.Builder(this).setTitle(p.getName())
                        .setItems(new String[]{"Delete"},(d,w)->{store.deletePlaylist(p.getId());refreshPlaylists();})
                        .show());
        binding.rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlaylists.setAdapter(playlistAdapter);
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

    // ── Change #6: Multi-step import flow ───────────────────────────────────

    /** Step 1 — source selection */
    private void showSourceSelector() {
        String[] sources = {"YouTube Music", "Spotify", "Apple Music"};
        new AlertDialog.Builder(this)
                .setTitle("Import from")
                .setItems(sources, (d, which) -> showLoginPrompt(sources[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Step 2 — account login */
    private void showLoginPrompt(String sourceName) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);

        EditText etUser = new EditText(this);
        etUser.setHint("Username or email");
        etUser.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etUser);

        EditText etPass = new EditText(this);
        etPass.setHint("Password");
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPass);

        new AlertDialog.Builder(this)
                .setTitle("Connect " + sourceName)
                .setMessage("Enter your " + sourceName + " credentials")
                .setView(layout)
                .setPositiveButton("Connect", (d, w) -> {
                    String user = etUser.getText().toString().trim();
                    String pass = etPass.getText().toString();
                    if (user.isEmpty() || pass.isEmpty()) {
                        Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    validateAndShowPlaylists(sourceName, user, pass);
                })
                .setNegativeButton("Back", (d, w) -> showSourceSelector())
                .show();
    }

    /** Step 3 — validate credentials (simulated) + show playlists */
    private void validateAndShowPlaylists(String sourceName, String user, String pass) {
        // Show progress
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Validating your " + sourceName + " account...");
        progress.setCancelable(false);
        progress.show();

        // Simulate network validation (replace with real OAuth in production)
        mainHandler.postDelayed(() -> {
            progress.dismiss();
            // Simulated playlists fetched from the account
            String[] mockPlaylists = getSimulatedPlaylists(sourceName);
            showPlaylistPicker(sourceName, mockPlaylists);
        }, 1500);
    }

    /** Step 4 — pick which playlist to import */
    private void showPlaylistPicker(String sourceName, String[] playlists) {
        List<Playlist> localPlaylists = store.getAll();
        if (localPlaylists.isEmpty()) {
            Toast.makeText(this, "Create a local playlist first!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Your " + sourceName + " playlists")
                .setItems(playlists, (d, which) ->
                        showLocalPlaylistPicker(sourceName, playlists[which], localPlaylists))
                .setNegativeButton("Back", (d, w) -> showSourceSelector())
                .show();
    }

    /** Step 5 — pick the local destination playlist */
    private void showLocalPlaylistPicker(String sourceName, String remoteName, List<Playlist> local) {
        String[] names = local.stream().map(Playlist::getName).toArray(String[]::new);
        new AlertDialog.Builder(this)
                .setTitle("Import \"" + remoteName + "\" into")
                .setItems(names, (d, which) ->
                        startAccountImport(sourceName, remoteName, local.get(which).getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Step 6 — import with auto-duplicate filtering */
    private void startAccountImport(String source, String remoteName, String targetId) {
        binding.progressImport.setVisibility(View.VISIBLE);
        binding.tvImportStatus.setVisibility(View.VISIBLE);
        binding.tvImportStatus.setText("Importing from " + source + "…");

        ExecutorService ex = Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            try {
                // Simulate fetching songs from remote playlist
                List<Song> remoteSongs = buildSimulatedSongs(source, remoteName, targetId);

                // Auto-filter duplicates (Change #6 — only import new songs)
                Playlist target = store.getById(targetId);
                List<Song> existing  = (target != null && target.getSongs() != null) ? target.getSongs() : new ArrayList<>();
                Set<String> existingTitles = new HashSet<>();
                for (Song s : existing) existingTitles.add(s.getTitle().toLowerCase());

                List<Song> newSongs = new ArrayList<>();
                for (Song s : remoteSongs) {
                    if (!existingTitles.contains(s.getTitle().toLowerCase())) {
                        newSongs.add(s);
                    }
                }

                int skipped = remoteSongs.size() - newSongs.size();
                for (Song s : newSongs) store.addSongToPlaylist(targetId, s);
                final int added = newSongs.size();
                final int sk    = skipped;

                mainHandler.post(() -> {
                    binding.progressImport.setVisibility(View.GONE);
                    binding.tvImportStatus.setVisibility(View.GONE);
                    refreshPlaylists();
                    String msg = "✅ Imported " + added + " song" + (added == 1 ? "" : "s")
                                 + (sk > 0 ? " · " + sk + " duplicate" + (sk==1?"":"s") + " skipped" : "");
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    binding.progressImport.setVisibility(View.GONE);
                    binding.tvImportStatus.setVisibility(View.GONE);
                    Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } finally { ex.shutdown(); }
        });
    }

    // ── Simulation helpers ──────────────────────────────────────────────────

    private String[] getSimulatedPlaylists(String source) {
        switch (source) {
            case "Spotify":
                return new String[]{"My Workout Mix", "Running Beats", "Pump Up 2024", "Liked Songs"};
            case "YouTube Music":
                return new String[]{"Gym Session", "Top Hits", "High Energy", "Auto Mix"};
            case "Apple Music":
                return new String[]{"Fitness Mix", "Power Tracks", "BPM 140+", "Favourites"};
            default:
                return new String[]{"Playlist 1", "Playlist 2"};
        }
    }

    private List<Song> buildSimulatedSongs(String source, String playlist, String targetId) throws InterruptedException {
        Thread.sleep(800); // simulate network
        List<Song> songs = new ArrayList<>();
        String[][] tracks = {
            {"Eye of the Tiger", "Survivor"},
            {"Lose Yourself", "Eminem"},
            {"Can't Hold Us", "Macklemore"},
            {"Stronger", "Kanye West"},
            {"Till I Collapse", "Eminem"},
            {"Power", "Kanye West"},
            {"Bangarang", "Skrillex"},
            {"Smells Like Teen Spirit", "Nirvana"},
        };
        // Map source string to Song.Source enum (Apple Music falls back to SPOTIFY)
        Song.Source src = source.equals("YouTube Music") ? Song.Source.YOUTUBE : Song.Source.SPOTIFY;
        for (String[] t : tracks) {
            Song s = new Song(UUID.randomUUID().toString(), src, t[0], t[1], null, 0L, targetId);
            songs.add(s);
        }
        return songs;
    }

    // ── Now playing ─────────────────────────────────────────────────────────

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
            Glide.with(this).load(song.getAlbumArtUrl())
                 .placeholder(android.R.drawable.ic_media_play)
                 .into(binding.ivNowPlayingArt);
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
