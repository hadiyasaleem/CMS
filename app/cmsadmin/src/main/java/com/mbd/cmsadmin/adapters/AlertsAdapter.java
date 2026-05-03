package com.mbd.cmsadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.models.AlertItem;

import java.util.List;
import java.util.Locale;

/**
 * AlertsAdapter
 * Binds a List<AlertItem> to the "Recent Academic Alerts" RecyclerView
 * in HomeFragment.
 *
 * To swap in Firestore data later:
 *   1. Query your collection and map each DocumentSnapshot → AlertItem.
 *   2. Call adapter.updateAlerts(newList) from the success callback.
 */
public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<AlertItem> alerts;

    /** Optional click callback — set from HomeFragment if needed. */
    public interface OnAlertClickListener {
        void onAlertClick(AlertItem item, int position);
    }

    private OnAlertClickListener clickListener;

    // ── Constructor ───────────────────────────────────────────────────────────
    public AlertsAdapter(List<AlertItem> alerts) {
        this.alerts = alerts;
    }

    public void setOnAlertClickListener(OnAlertClickListener listener) {
        this.clickListener = listener;
    }

    // ── Data mutation ─────────────────────────────────────────────────────────

    /** Replace the entire dataset (call from Firestore callback). */
    public void updateAlerts(List<AlertItem> newAlerts) {
        this.alerts = newAlerts;
        notifyDataSetChanged();
    }

    // ── RecyclerView.Adapter overrides ────────────────────────────────────────

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertItem item = alerts.get(position);

        // Zero-padded number badge: 01, 02, 03 …
        holder.tvNumber.setText(String.format(Locale.getDefault(), "%02d", position + 1));
        holder.tvTitle.setText(item.getTitle());
        holder.tvBody.setText(item.getBody());
        holder.tvMeta.setText(item.getMeta());

        // Ripple / click
        if (clickListener != null) {
            holder.itemView.setOnClickListener(v ->
                    clickListener.onAlertClick(item, holder.getAdapterPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return alerts == null ? 0 : alerts.size();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNumber;
        final TextView tvTitle;
        final TextView tvBody;
        final TextView tvMeta;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_alert_number);
            tvTitle  = itemView.findViewById(R.id.tv_alert_title);
            tvBody   = itemView.findViewById(R.id.tv_alert_body);
            tvMeta   = itemView.findViewById(R.id.tv_alert_meta);
        }
    }
}