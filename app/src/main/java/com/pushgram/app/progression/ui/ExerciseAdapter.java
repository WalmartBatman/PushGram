package com.pushgram.app.progression.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pushgram.app.R;
import com.pushgram.app.progression.model.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.VH> {

    public static class ExerciseItem {
        public final Exercise exercise;
        public final int currentLevel;
        public final int bestReps;

        public ExerciseItem(Exercise exercise, int currentLevel, int bestReps) {
            this.exercise = exercise;
            this.currentLevel = currentLevel;
            this.bestReps = bestReps;
        }
    }

    public interface OnClickListener { void onClick(Exercise e); }

    private final Context ctx;
    private List<ExerciseItem> data;
    private final OnClickListener listener;

    public ExerciseAdapter(Context ctx, List<ExerciseItem> data, OnClickListener listener) {
        this.ctx = ctx;
        this.data = new ArrayList<>(data);
        this.listener = listener;
    }

    public void updateData(List<ExerciseItem> newData) {
        this.data = new ArrayList<>(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_exercise, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExerciseItem item = data.get(position);
        Exercise e = item.exercise;

        h.tvEmoji.setText(e.getEmoji());
        h.tvName.setText(e.getName());
        h.tvCategory.setText(e.getCategory().label);
        h.tvDifficulty.setText(e.getDifficultyStars());
        h.tvLevel.setText("Level " + item.currentLevel + "/5");
        h.tvEquipment.setText(e.getEquipment());
        h.progressLevel.setProgress(item.currentLevel * 20);

        // Primary muscles
        StringBuilder muscles = new StringBuilder();
        for (Exercise.MuscleGroup mg : e.getPrimaryMuscles()) {
            if (muscles.length() > 0) muscles.append(" · ");
            muscles.append(mg.emoji).append(mg.name);
        }
        h.tvMuscles.setText(muscles.toString());

        if (item.bestReps > 0) {
            h.tvBest.setText("Best: " + item.bestReps + " reps");
            h.tvBest.setVisibility(View.VISIBLE);
        } else {
            h.tvBest.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(e));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvCategory, tvDifficulty, tvLevel, tvMuscles, tvEquipment, tvBest;
        ProgressBar progressLevel;

        VH(View v) {
            super(v);
            tvEmoji      = v.findViewById(R.id.tvExerciseEmoji);
            tvName       = v.findViewById(R.id.tvExerciseName);
            tvCategory   = v.findViewById(R.id.tvExerciseCategory);
            tvDifficulty = v.findViewById(R.id.tvExerciseDifficulty);
            tvLevel      = v.findViewById(R.id.tvExerciseLevel);
            tvMuscles    = v.findViewById(R.id.tvExerciseMuscles);
            tvEquipment  = v.findViewById(R.id.tvExerciseEquipment);
            tvBest       = v.findViewById(R.id.tvExerciseBest);
            progressLevel = v.findViewById(R.id.progressExerciseLevel);
        }
    }
}
