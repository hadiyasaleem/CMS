package com.mbd.cmsadmin.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.models.StudentAttendance;

import java.util.List;
import java.util.Locale;

/**
 * AttendanceTableAdapter
 * Drives the horizontally-scrollable student attendance table in
 * AttendanceReportActivity.
 *
 * Each row contains:
 *   - Student name + roll number  (fixed left column, conceptually)
 *   - One TextView per date column ("P" green / "A" red)
 *   - Summary percentage
 *
 * The date columns are added dynamically from the dateHeaders list so
 * the table can handle any number of columns (daily, weekly, monthly views).
 *
 * To integrate Firestore later, replace the fake list in
 * AttendanceReportActivity with a query result and call
 * adapter.updateData(students, dates).
 */
public class AttendanceTableAdapter
        extends RecyclerView.Adapter<AttendanceTableAdapter.RowViewHolder> {

    private List<StudentAttendance> students;
    private List<String>            dateHeaders; // ["01 Oct", "02 Oct", …]

    // Colour constants — resolved once for performance
    private static final String COLOR_PRESENT = "#2E7D32"; // emerald-700 equivalent
    private static final String COLOR_ABSENT  = "#B22B1D"; // secondary (crimson)
    private static final String COLOR_LEAVE   = "#F7BD48"; // tertiary-fixed-dim (amber)

    public AttendanceTableAdapter(List<StudentAttendance> students,
                                  List<String>            dateHeaders) {
        this.students    = students;
        this.dateHeaders = dateHeaders;
    }

    /** Call from Firestore success callback. */
    public void updateData(List<StudentAttendance> newStudents,
                           List<String>            newDates) {
        this.students    = newStudents;
        this.dateHeaders = newDates;
        notifyDataSetChanged();
    }

    // ── RecyclerView overrides ────────────────────────────────────────────────

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_row, parent, false);
        return new RowViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder h, int position) {
        StudentAttendance student = students.get(position);

        h.tvStudentName.setText(student.getStudentName());
        h.tvRollNo.setText(student.getRollNo());

        // Build dynamic date cells
        h.containerDates.removeAllViews();
        List<String> records = student.getAttendanceRecords();
        for (int i = 0; i < records.size(); i++) {
            String status = records.get(i);
            TextView cell = buildStatusCell(h.itemView.getContext(), status);
            h.containerDates.addView(cell);
        }

        // Summary
        h.tvSummary.setText(
                String.format(Locale.getDefault(), "%.1f%%", student.getSummaryPercent()));

        // Colour-code summary: below 75% → red
        if (student.getSummaryPercent() < 75f) {
            h.tvSummary.setTextColor(Color.parseColor(COLOR_ABSENT));
        } else {
            h.tvSummary.setTextColor(
                    h.itemView.getContext().getResources()
                            .getColor(R.color.primary, null));
        }
    }

    @Override
    public int getItemCount() {
        return students == null ? 0 : students.size();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TextView buildStatusCell(android.content.Context ctx, String status) {
        TextView tv = new TextView(ctx);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                dpToPx(ctx, 52), ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText(status);
        tv.setTextSize(13f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(0, dpToPx(ctx, 4), 0, dpToPx(ctx, 4));

        switch (status) {
            case "P":
                tv.setTextColor(Color.parseColor(COLOR_PRESENT));
                break;
            case "A":
                tv.setTextColor(Color.parseColor(COLOR_ABSENT));
                break;
            case "L":
                tv.setTextColor(Color.parseColor(COLOR_LEAVE));
                break;
            default:
                tv.setTextColor(Color.parseColor(COLOR_PRESENT));
        }
        return tv;
    }

    private int dpToPx(android.content.Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class RowViewHolder extends RecyclerView.ViewHolder {
        final TextView     tvStudentName;
        final TextView     tvRollNo;
        final LinearLayout containerDates; // date cells injected here
        final TextView     tvSummary;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName  = itemView.findViewById(R.id.tv_student_name);
            tvRollNo       = itemView.findViewById(R.id.tv_roll_no);
            containerDates = itemView.findViewById(R.id.container_dates);
            tvSummary      = itemView.findViewById(R.id.tv_summary);
        }
    }
}