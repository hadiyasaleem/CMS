package com.mbd.cmsadmin.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.activities.AttendanceReportActivity;
import com.mbd.cmsadmin.models.DepartmentItem;

import java.util.List;
import java.util.Locale;

/**
 * DepartmentsAdapter
 * Drives the expandable department list in AttendanceFragment.
 *
 * Each row is collapsed by default; tapping the header toggles an
 * animated semester grid. Tapping a semester tile opens
 * AttendanceReportActivity with the department + semester bundled.
 */
public class DepartmentsAdapter extends RecyclerView.Adapter<DepartmentsAdapter.DeptViewHolder> {

    private final List<DepartmentItem> departments;
    private       int                  expandedPosition = 0; // first one open by default

    public DepartmentsAdapter(List<DepartmentItem> departments) {
        this.departments = departments;
    }

    // ── RecyclerView overrides ────────────────────────────────────────────────

    @NonNull
    @Override
    public DeptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_department, parent, false);
        return new DeptViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeptViewHolder h, int position) {
        DepartmentItem dept    = departments.get(position);
        boolean        expanded = position == expandedPosition;

        // Header content
        h.tvName.setText(dept.getName());
        h.tvFaculty.setText(dept.getFaculty());
        h.ivIcon.setImageResource(dept.getIconResId());

        // Active department gets navy icon bg; collapsed get grey
        if (expanded) {
            h.iconBg.setBackgroundResource(R.drawable.bg_dept_icon_active);
            h.ivIcon.setColorFilter(
                    h.itemView.getContext().getResources().getColor(R.color.on_primary, null));
        } else {
            h.iconBg.setBackgroundResource(R.drawable.bg_dept_icon_inactive);
            h.ivIcon.setColorFilter(
                    h.itemView.getContext().getResources().getColor(R.color.primary, null));
        }

        // Expand / collapse chevron
        float targetRotation = expanded ? 180f : 0f;
        h.ivChevron.setRotation(targetRotation);
        if (expanded) {
            h.ivChevron.setColorFilter(
                    h.itemView.getContext().getResources().getColor(R.color.secondary, null));
        } else {
            h.ivChevron.setColorFilter(
                    h.itemView.getContext().getResources().getColor(R.color.on_surface_variant, null));
        }

        // Semester grid visibility
        h.semesterSection.setVisibility(expanded ? View.VISIBLE : View.GONE);

        // Build semester tiles (only when expanded, and only once)
        if (expanded) {
            buildSemesterGrid(h.semesterGrid, dept, h.itemView.getContext());
        }

        // Header click → toggle expansion
        h.header.setOnClickListener(v -> {
            int prev = expandedPosition;
            if (expanded) {
                expandedPosition = -1;           // collapse all
            } else {
                expandedPosition = h.getAdapterPosition();
            }
            notifyItemChanged(prev);
            notifyItemChanged(h.getAdapterPosition());

            // Animate chevron
            ObjectAnimator.ofFloat(h.ivChevron, "rotation",
                    h.ivChevron.getRotation(),
                    expandedPosition == h.getAdapterPosition() ? 180f : 0f
            ).setDuration(200).start();
        });
    }

    @Override
    public int getItemCount() {
        return departments == null ? 0 : departments.size();
    }

    // ── Semester grid builder ─────────────────────────────────────────────────

    private void buildSemesterGrid(GridLayout grid, DepartmentItem dept, Context ctx) {
        grid.removeAllViews();
        grid.setColumnCount(2);

        int totalSemesters = dept.getSemesterCount();

        for (int i = 1; i <= totalSemesters; i++) {
            final int semNum = i;
            boolean   isFirst = (i == 1);

            View tile = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_semester_tile, grid, false);

            TextView tvNum   = tile.findViewById(R.id.tv_semester_number);
            TextView tvLabel = tile.findViewById(R.id.tv_semester_label);

            tvNum.setText(String.format(Locale.getDefault(), "%02d", semNum));
            tvLabel.setText("Semester");

            // First semester gets the red left-border active style
            if (isFirst) {
                tile.setBackgroundResource(R.drawable.bg_semester_tile_active);
            }

            // Column weight so both tiles share width equally
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            lp.width  = 0;
            lp.setMargins(8, 8, 8, 8);
            tile.setLayoutParams(lp);

            tile.setOnClickListener(v -> {
                Intent intent = new Intent(ctx, AttendanceReportActivity.class);
                intent.putExtra(AttendanceReportActivity.EXTRA_DEPT_ID,   dept.getId());
                intent.putExtra(AttendanceReportActivity.EXTRA_DEPT_NAME, dept.getName());
                intent.putExtra(AttendanceReportActivity.EXTRA_SEMESTER,  semNum);
                ctx.startActivity(intent);
            });

            grid.addView(tile);
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class DeptViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout header;
        final LinearLayout iconBg;
        final ImageView    ivIcon;
        final TextView     tvName;
        final TextView     tvFaculty;
        final ImageView    ivChevron;
        final LinearLayout semesterSection;
        final GridLayout   semesterGrid;

        DeptViewHolder(@NonNull View itemView) {
            super(itemView);
            header          = itemView.findViewById(R.id.dept_header);
            iconBg          = itemView.findViewById(R.id.dept_icon_bg);
            ivIcon          = itemView.findViewById(R.id.dept_icon);
            tvName          = itemView.findViewById(R.id.tv_dept_name);
            tvFaculty       = itemView.findViewById(R.id.tv_dept_faculty);
            ivChevron       = itemView.findViewById(R.id.iv_chevron);
            semesterSection = itemView.findViewById(R.id.semester_section);
            semesterGrid    = itemView.findViewById(R.id.semester_grid);
        }
    }
}