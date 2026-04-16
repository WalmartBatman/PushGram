package com.pushgram.app.rank.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pushgram.app.R;
import com.pushgram.app.rank.model.LeaderboardEntry;
import com.pushgram.app.rank.model.RankTier;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {

    private final Context ctx;
    private List<LeaderboardEntry> data;

    public LeaderboardAdapter(Context ctx, List<LeaderboardEntry> data) {
        this.ctx = ctx;
        this.data = new ArrayList<>(data);
    }

    public void updateData(List<LeaderboardEntry> newData) {
        this.data = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_leaderboard, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LeaderboardEntry e = data.get(position);
        RankTier tier = e.getTier();

        h.tvRankNum.setText(e.getRankMedal());
        h.tvName.setText(e.getDisplayName());
        h.tvTier.setText(tier.getDisplayName());
        h.tvXp.setText(e.getTotalXp() + " XP");
        h.tvReps.setText(e.getTotalReps() + " reps");
        h.tvWins.setText(e.getBattlesWon() + "W");

        try {
            h.tvTier.setTextColor(Color.parseColor(tier.colorHex));
        } catch (Exception ex) {
            h.tvTier.setTextColor(0xFFFFFFFF);
        }

        // Highlight current user
        if (e.isCurrentUser()) {
            h.itemView.setBackgroundColor(0x22FF6B35);
            h.tvName.setText("▶ " + e.getDisplayName() + " (You)");
        } else {
            h.itemView.setBackgroundColor(0x00000000);
        }

        // Top 3 special styling
        if (e.getRank() == 1) h.tvRankNum.setTextColor(0xFFFFD700);
        else if (e.getRank() == 2) h.tvRankNum.setTextColor(0xFFC0C0C0);
        else if (e.getRank() == 3) h.tvRankNum.setTextColor(0xFFCD7F32);
        else h.tvRankNum.setTextColor(0xFFAAAAAA);
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRankNum, tvName, tvTier, tvXp, tvReps, tvWins;

        VH(View v) {
            super(v);
            tvRankNum = v.findViewById(R.id.tvLeaderRank);
            tvName    = v.findViewById(R.id.tvLeaderName);
            tvTier    = v.findViewById(R.id.tvLeaderTier);
            tvXp      = v.findViewById(R.id.tvLeaderXp);
            tvReps    = v.findViewById(R.id.tvLeaderReps);
            tvWins    = v.findViewById(R.id.tvLeaderWins);
        }
    }
}
