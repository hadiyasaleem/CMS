package com.mbd.cmsteacher.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import com.mbd.cmsteacher.models.SessionalMarkEntry;

import java.util.List;

/**
 * SessionalMarkAdapter
 * ─────────────────────────────────────────────────────────────────────────
 * Drives the sessional mark entry RecyclerView.
 *
 * Each row has:
 *   • Student avatar + name + roll number
 *   • EditText  — Attendance   (max 10)
 *   • EditText  — Assignments  (max 20)
 *   • EditText  — Quizzes      (max 20)
 *   • Total TextView — auto-updated on any input change, flashes gold
 *
 * Input validation:
 *   • Value > max  → red border + red text on that field
 *   • Value ≤ max  → border resets to normal
 *
 * IMPORTANT: TextWatchers are detached before rebinding and reattached
 * after to avoid recycled-view feedback loops.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class SessionalMarkAdapter
        extends RecyclerView.Adapter<SessionalMarkAdapter.MarkViewHolder> {

    // ── Colour constants (hex) ────────────────────────────────────────────
    private static final int COLOR_ERROR       = Color.parseColor("#BA1A1A");
    private static final int COLOR_GOLD        = Color.parseColor("#B18000");
    private static final int COLOR_NAVY        = Color.parseColor("#000A1E");
    private static final int COLOR_NORMAL_BG   = Color.parseColor("#EEEEEE");
    private static final int COLOR_ERROR_BG    = Color.parseColor("#FFDAD6");

    private final List<SessionalMarkEntry> entries;

    public SessionalMarkAdapter(List<SessionalMarkEntry> entries) {
        this.entries = entries;
    }

    public List<SessionalMarkEntry> getEntries() { return entries; }

    // ── RecyclerView overrides ─────────────────────────────────────────────

    @NonNull
    @Override
    public MarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sessional_mark, parent, false);
        return new MarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MarkViewHolder h, int position) {
        SessionalMarkEntry entry = entries.get(position);

        // ── Student info ──────────────────────────────────────────────────
        h.ivAvatar.setImageResource(entry.getAvatarResId());
        h.tvName.setText(entry.getStudentName());
        h.tvRoll.setText("Roll: " + entry.getRollNumber());

        // ── Detach old watchers before setting text ───────────────────────
        h.detachWatchers();

        // ── Populate mark fields ──────────────────────────────────────────
        setFieldValue(h.etAttendance,  entry.getAttendance(),  entry);
        setFieldValue(h.etAssignments, entry.getAssignments(), entry);
        setFieldValue(h.etQuizzes,     entry.getQuizzes(),     entry);

        // ── Total ─────────────────────────────────────────────────────────
        h.tvTotal.setText(String.valueOf(entry.getTotal()));
        h.tvTotal.setTextColor(COLOR_NAVY);

        // ── Stripe alternate rows ─────────────────────────────────────────
        h.itemView.setBackgroundColor(
                position % 2 == 0
                        ? Color.parseColor("#FFFFFF")
                        : Color.parseColor("#F9F9F9"));

        // ── Attach new watchers ───────────────────────────────────────────
        h.attachWatchers(entry);
    }

    @Override
    public int getItemCount() { return entries == null ? 0 : entries.size(); }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void setFieldValue(EditText et, int value, SessionalMarkEntry entry) {
        if (entry.isNotEntered(value)) {
            et.setText("");
            et.setHint("–");
        } else {
            et.setText(String.valueOf(value));
        }
        resetFieldStyle(et);
    }

    static void resetFieldStyle(EditText et) {
        et.setBackgroundResource(R.drawable.bg_mark_input);
        et.setTextColor(COLOR_NAVY);
    }

    static void markFieldError(EditText et) {
        et.setBackgroundResource(R.drawable.bg_mark_input_error);
        et.setTextColor(COLOR_ERROR);
    }

    // ── ViewHolder ──────────────────────────────────────────────────────────

    static class MarkViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivAvatar;
        final TextView  tvName;
        final TextView  tvRoll;
        final EditText  etAttendance;
        final EditText  etAssignments;
        final EditText  etQuizzes;
        final TextView  tvTotal;

        // Hold references to detach/reattach
        private MarkWatcher watcherAttendance;
        private MarkWatcher watcherAssignments;
        private MarkWatcher watcherQuizzes;

        MarkViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar      = itemView.findViewById(R.id.iv_student_avatar);
            tvName        = itemView.findViewById(R.id.tv_student_name);
            tvRoll        = itemView.findViewById(R.id.tv_student_roll);
            etAttendance  = itemView.findViewById(R.id.et_attendance);
            etAssignments = itemView.findViewById(R.id.et_assignments);
            etQuizzes     = itemView.findViewById(R.id.et_quizzes);
            tvTotal       = itemView.findViewById(R.id.tv_total);
        }

        void detachWatchers() {
            if (watcherAttendance  != null) etAttendance .removeTextChangedListener(watcherAttendance);
            if (watcherAssignments != null) etAssignments.removeTextChangedListener(watcherAssignments);
            if (watcherQuizzes     != null) etQuizzes    .removeTextChangedListener(watcherQuizzes);
        }

        void attachWatchers(SessionalMarkEntry entry) {
            watcherAttendance  = new MarkWatcher(etAttendance,  10, entry, this, MarkWatcher.FIELD_ATTENDANCE);
            watcherAssignments = new MarkWatcher(etAssignments, 20, entry, this, MarkWatcher.FIELD_ASSIGNMENTS);
            watcherQuizzes     = new MarkWatcher(etQuizzes,     20, entry, this, MarkWatcher.FIELD_QUIZZES);

            etAttendance .addTextChangedListener(watcherAttendance);
            etAssignments.addTextChangedListener(watcherAssignments);
            etQuizzes    .addTextChangedListener(watcherQuizzes);
        }
    }

    // ── TextWatcher for a single mark field ──────────────────────────────────

    static class MarkWatcher implements TextWatcher {

        static final int FIELD_ATTENDANCE  = 0;
        static final int FIELD_ASSIGNMENTS = 1;
        static final int FIELD_QUIZZES     = 2;

        private final EditText           field;
        private final int                max;
        private final SessionalMarkEntry entry;
        private final MarkViewHolder     holder;
        private final int                fieldId;

        MarkWatcher(EditText field, int max, SessionalMarkEntry entry,
                    MarkViewHolder holder, int fieldId) {
            this.field   = field;
            this.max     = max;
            this.entry   = entry;
            this.holder  = holder;
            this.fieldId = fieldId;
        }

        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c)     {}

        @Override
        public void afterTextChanged(Editable s) {
            String raw = s.toString().trim();
            int value  = raw.isEmpty() ? SessionalMarkEntry.notEnteredValue()
                    : parseOrDefault(raw);

            // Write back to model
            switch (fieldId) {
                case FIELD_ATTENDANCE:  entry.setAttendance(value);  break;
                case FIELD_ASSIGNMENTS: entry.setAssignments(value); break;
                case FIELD_QUIZZES:     entry.setQuizzes(value);     break;
            }

            // Validate
            if (!raw.isEmpty() && value != SessionalMarkEntry.notEnteredValue() && value > max) {
                markFieldError(field);
            } else {
                resetFieldStyle(field);
            }

            // Recalculate total and flash gold
            int total = entry.getTotal();
            holder.tvTotal.setText(String.valueOf(total));
            flashGold(holder.tvTotal);
        }

        private int parseOrDefault(String s) {
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { return 0; }
        }

        /** Brief gold colour flash on the total display. */
        private void flashGold(TextView tv) {
            ValueAnimator anim = ValueAnimator.ofObject(
                    new ArgbEvaluator(), COLOR_GOLD, COLOR_NAVY);
            anim.setDuration(400);
            anim.addUpdateListener(a -> tv.setTextColor((int) a.getAnimatedValue()));
            anim.start();
        }
    }
}