package com.mbd.cmsteacher.adapters;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsteacher.R;
import com.mbd.cmsteacher.models.MidtermMarkEntry;

import java.util.List;

/**
 * MidtermMarkAdapter
 * ─────────────────────────────────────────────────────────────────────────
 * Drives the midterm mark entry RecyclerView.
 *
 * Each row:
 *   [avatar | name + yearAndDept] [roll number] [EditText score / 100]
 *
 * Validation:
 *   • value > 100  → red border + red text
 *   • value < 0    → clamped to 0
 *   • value ≤ 100  → normal style restored
 *
 * TextWatchers are detached before rebind and reattached after,
 * preventing recycled-view feedback loops.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class MidtermMarkAdapter
        extends RecyclerView.Adapter<MidtermMarkAdapter.MidtermViewHolder> {

    private static final int COLOR_NAVY  = Color.parseColor("#000A1E");
    private static final int COLOR_ERROR = Color.parseColor("#BA1A1A");

    private final List<MidtermMarkEntry> entries;

    public MidtermMarkAdapter(List<MidtermMarkEntry> entries) {
        this.entries = entries;
    }

    public List<MidtermMarkEntry> getEntries() { return entries; }

    // ── RecyclerView overrides ─────────────────────────────────────────────

    @NonNull
    @Override
    public MidtermViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_midterm_mark, parent, false);
        return new MidtermViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MidtermViewHolder h, int position) {
        MidtermMarkEntry entry = entries.get(position);

        // ── Student info ──────────────────────────────────────────────────
        h.ivAvatar.setImageResource(entry.getAvatarResId());
        h.tvName.setText(entry.getStudentName());
        h.tvMeta.setText(entry.getYearAndDept());
        h.tvRoll.setText(entry.getRollNumber());

        // ── Detach old watcher before setting text ────────────────────────
        h.detachWatcher();

        // ── Populate mark field ───────────────────────────────────────────
        if (entry.isEntered()) {
            h.etMarks.setText(String.valueOf(entry.getMarks()));
        } else {
            h.etMarks.setText("");
            h.etMarks.setHint("--");
        }
        resetStyle(h.etMarks);

        // ── Alternate row stripe ──────────────────────────────────────────
        h.itemView.setBackgroundColor(
                position % 2 == 0
                        ? Color.parseColor("#FFFFFF")
                        : Color.parseColor("#F9F9F9"));

        // ── Attach fresh watcher ──────────────────────────────────────────
        h.attachWatcher(entry);
    }

    @Override
    public int getItemCount() { return entries == null ? 0 : entries.size(); }

    // ── Style helpers ──────────────────────────────────────────────────────

    static void resetStyle(EditText et) {
        et.setBackgroundResource(R.drawable.bg_midterm_input);
        et.setTextColor(COLOR_NAVY);
    }

    static void errorStyle(EditText et) {
        et.setBackgroundResource(R.drawable.bg_mark_input_error);
        et.setTextColor(COLOR_ERROR);
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────

    static class MidtermViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivAvatar;
        final TextView  tvName;
        final TextView  tvMeta;
        final TextView  tvRoll;
        final EditText  etMarks;

        private ScoreWatcher watcher;

        MidtermViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_student_avatar);
            tvName   = itemView.findViewById(R.id.tv_student_name);
            tvMeta   = itemView.findViewById(R.id.tv_student_meta);
            tvRoll   = itemView.findViewById(R.id.tv_student_roll);
            etMarks  = itemView.findViewById(R.id.et_marks);
        }

        void detachWatcher() {
            if (watcher != null) etMarks.removeTextChangedListener(watcher);
        }

        void attachWatcher(MidtermMarkEntry entry) {
            watcher = new ScoreWatcher(etMarks, entry, this);
            etMarks.addTextChangedListener(watcher);
        }
    }

    // ── TextWatcher ────────────────────────────────────────────────────────

    static class ScoreWatcher implements TextWatcher {

        private final EditText         field;
        private final MidtermMarkEntry entry;
        private final MidtermViewHolder holder;

        ScoreWatcher(EditText field, MidtermMarkEntry entry, MidtermViewHolder holder) {
            this.field  = field;
            this.entry  = entry;
            this.holder = holder;
        }

        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c)     {}

        @Override
        public void afterTextChanged(Editable s) {
            String raw = s.toString().trim();

            if (raw.isEmpty()) {
                entry.setMarks(MidtermMarkEntry.notEnteredValue());
                resetStyle(field);
                return;
            }

            int value;
            try { value = Integer.parseInt(raw); }
            catch (NumberFormatException e) { value = 0; }

            // Clamp negatives silently
            if (value < 0) {
                value = 0;
                field.removeTextChangedListener(this);
                field.setText("0");
                field.setSelection(field.getText().length());
                field.addTextChangedListener(this);
            }

            entry.setMarks(value);

            if (value > 100) {
                errorStyle(field);
            } else {
                resetStyle(field);
            }
        }
    }
}