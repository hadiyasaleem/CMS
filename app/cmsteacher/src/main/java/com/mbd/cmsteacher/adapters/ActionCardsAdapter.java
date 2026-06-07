package com.mbd.cmsteacher.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsteacher.R;
import com.mbd.cmsteacher.models.ActionCardItem;

import java.util.List;

/**
 * ActionCardsAdapter
 * Drives the 2-column Faculty Dashboard grid in HomeFragment.
 * Each card shows an icon, title, and subtitle.
 *
 * Set an OnCardClickListener to handle navigation from HomeFragment.
 */
public class ActionCardsAdapter
        extends RecyclerView.Adapter<ActionCardsAdapter.CardViewHolder> {

    private final List<ActionCardItem> items;

    public interface OnCardClickListener {
        void onCardClick(ActionCardItem item, int position);
    }

    private OnCardClickListener clickListener;

    public ActionCardsAdapter(List<ActionCardItem> items) {
        this.items = items;
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.clickListener = listener;
    }

    // ── RecyclerView overrides ────────────────────────────────────────────

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_action_card, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder h, int position) {
        ActionCardItem item = items.get(position);

        h.ivIcon.setImageResource(item.getIconResId());
        h.tvTitle.setText(item.getTitle());
        h.tvSubtitle.setText(item.getSubtitle());

        if (clickListener != null) {
            h.itemView.setOnClickListener(v ->
                    clickListener.onCardClick(item, h.getAdapterPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    static class CardViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView  tvTitle;
        final TextView  tvSubtitle;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon    = itemView.findViewById(R.id.iv_card_icon);
            tvTitle   = itemView.findViewById(R.id.tv_card_title);
            tvSubtitle = itemView.findViewById(R.id.tv_card_subtitle);
        }
    }
}