package com.mbd.cmsadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;

import java.util.List;
import java.util.Locale;

/**
 * AbsenceWarningAdapter
 * Drives the small "Absence Warning" list inside AttendanceReportActivity's
 * insight bento card.
 *
 * Uses a simple String pair: student name + attendance percentage (float).
 * Replace the fake list in AttendanceReportActivity with Firestore data.
 */
public class AbsenceWarningAdapter
        extends RecyclerView.Adapter<AbsenceWarningAdapter.WarningVH> {

    public static class WarningEntry {
        public final String name;
        public final float  percent;
        public WarningEntry(String name, float percent) {
            this.name    = name;
            this.percent = percent;
        }
    }

    private final List<WarningEntry> entries;

    public AbsenceWarningAdapter(List<WarningEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public WarningVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_absence_warning, parent, false);
        return new WarningVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WarningVH h, int position) {
        WarningEntry e = entries.get(position);
        h.tvName.setText(e.name);
        h.tvPercent.setText(String.format(Locale.getDefault(), "%.1f%%", e.percent));
    }

    @Override
    public int getItemCount() { return entries == null ? 0 : entries.size(); }

    static class WarningVH extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvPercent;
        WarningVH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tv_warning_name);
            tvPercent = v.findViewById(R.id.tv_warning_percent);
        }
    }
}