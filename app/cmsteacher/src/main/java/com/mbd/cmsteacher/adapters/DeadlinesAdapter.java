package com.mbd.cmsteacher.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsteacher.R;
import com.mbd.cmsteacher.models.DeadlineItem;

import java.util.List;

/**
 * DeadlinesAdapter
 * Drives the "Academic Deadlines" vertical list in HomeFragment.
 * Each item has a coloured left-border strip, leading icon, title, subtitle,
 * and a due-date label — all matching the HTML reference.
 *
 * Left border / icon colour logic:
 *   HIGH → secondary (crimson #B22B1D)
 *   MED  → accent gold (#B18000)
 *   LOW  → outline (#75777D)
 */
public class DeadlinesAdapter
        extends RecyclerView.Adapter<DeadlinesAdapter.DeadlineViewHolder> {

    private List<DeadlineItem> items;

    private static final String COLOR_HIGH = "#B22B1D";
    private static final String COLOR_MED  = "#B18000";
    private static final String COLOR_LOW  = "#75777D";

    public DeadlinesAdapter(List<DeadlineItem> items) {
        this.items = items;
    }

    public void updateItems(List<DeadlineItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    // ── RecyclerView overrides ────────────────────────────────────────────

    @NonNull
    @Override
    public DeadlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deadline, parent, false);
        return new DeadlineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineViewHolder h, int position) {
        DeadlineItem item = items.get(position);

        // Resolve colour from urgency
        String color = colorForUrgency(item.getUrgency());

        // Left border strip colour
        h.viewLeftBorder.setBackgroundColor(Color.parseColor(color));

        // Icon
        h.ivIcon.setImageResource(item.getIconResId());
        h.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor(color)));

        // Text
        h.tvTitle.setText(item.getTitle());
        h.tvSubtitle.setText(item.getSubtitle());
        h.tvDueLabel.setText(item.getDueLabel());
        h.tvDueLabel.setTextColor(Color.parseColor(color));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String colorForUrgency(String urgency) {
        switch (urgency) {
            case DeadlineItem.URGENCY_HIGH: return COLOR_HIGH;
            case DeadlineItem.URGENCY_MED:  return COLOR_MED;
            default:                        return COLOR_LOW;
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    static class DeadlineViewHolder extends RecyclerView.ViewHolder {
        final View      viewLeftBorder;
        final ImageView ivIcon;
        final TextView  tvTitle;
        final TextView  tvSubtitle;
        final TextView  tvDueLabel;

        DeadlineViewHolder(@NonNull View itemView) {
            super(itemView);
            viewLeftBorder = itemView.findViewById(R.id.view_left_border);
            ivIcon         = itemView.findViewById(R.id.iv_deadline_icon);
            tvTitle        = itemView.findViewById(R.id.tv_deadline_title);
            tvSubtitle     = itemView.findViewById(R.id.tv_deadline_subtitle);
            tvDueLabel     = itemView.findViewById(R.id.tv_deadline_due);
        }
    }
}