package com.mbd.cmsteacher;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MarkAttendanceActivity — "Mark Attendance" screen.
 * ─────────────────────────────────────────────────────────────────────────
 * Mirrors HTML reference 2 (Mark Attendance - Imperial Chancery).
 *
 * Sections:
 *   § 1  Session Header card — course name, date, session time, capacity
 *          + "Mark All Present" button
 *   § 2  Search bar  (filter by name or roll number)
 *   § 3  Student list — RecyclerView
 *          each row: avatar initials | name + roll no | P/A/L segmented control
 *   § 4  Summary strip (Present / Absent / Leave counts)
 *   § 5  Sticky "Submit Attendance Registry" bottom button
 *
 * Extras accepted (from AttendanceFragment):
 *   EXTRA_DEPARTMENT  — department name string
 *   EXTRA_COURSE      — course name string
 *   EXTRA_SEMESTER    — semester number (int)
 *
 * Firebase write is handled via AttendanceRepository (stub injected here).
 * ─────────────────────────────────────────────────────────────────────────
 */
public class MarkAttendanceActivity extends AppCompatActivity {

    // ── Intent extras ─────────────────────────────────────────────────────
    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_COURSE     = "extra_course";
    public static final String EXTRA_SEMESTER   = "extra_semester";

    // ── Attendance status constants ────────────────────────────────────────
    public static final int STATUS_PRESENT = 0;
    public static final int STATUS_ABSENT  = 1;
    public static final int STATUS_LEAVE   = 2;

    // ── Views ─────────────────────────────────────────────────────────────
    private TextView tvCourseName;
    private TextView tvSessionDate;
    private TextView tvSessionTime;
    private TextView tvCapacity;
    private MaterialButton btnMarkAllPresent;
    private TextInputEditText etSearch;
    private RecyclerView rvStudents;
    private TextView tvCountPresent;
    private TextView tvCountAbsent;
    private TextView tvCountLeave;
    private MaterialButton btnSubmit;

    // ── Data ──────────────────────────────────────────────────────────────
    private List<StudentRecord> masterList = new ArrayList<>();
    private List<StudentRecord> filteredList = new ArrayList<>();
    private StudentAttendanceAdapter adapter;

    // ── State ─────────────────────────────────────────────────────────────
    private String department;
    private String course;
    private int semester;
    private boolean isSubmitting = false;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        // Toolbar / back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // title is in layout
        }

        readExtras();
        bindViews();
        populateSampleStudents();     // TODO: replace with Firebase fetch
        setupRecyclerView();
        setupSessionHeader();
        setupSearch();
        setupMarkAllPresent();
        setupSubmitButton();
        updateSummary();
    }

    // ── Extras ────────────────────────────────────────────────────────────

    private void readExtras() {
        department = getIntent().getStringExtra(EXTRA_DEPARTMENT);
        course     = getIntent().getStringExtra(EXTRA_COURSE);
        semester   = getIntent().getIntExtra(EXTRA_SEMESTER, 1);

        if (department == null) department = "Architecture & Design";
        if (course     == null) course     = "Advanced Architecture III";
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews() {
        tvCourseName    = findViewById(R.id.tv_course_name);
        tvSessionDate   = findViewById(R.id.tv_session_date);
        tvSessionTime   = findViewById(R.id.tv_session_time);
        tvCapacity      = findViewById(R.id.tv_capacity);
        btnMarkAllPresent = findViewById(R.id.btn_mark_all_present);
        etSearch        = findViewById(R.id.et_search);
        rvStudents      = findViewById(R.id.rv_students);
        tvCountPresent  = findViewById(R.id.tv_count_present);
        tvCountAbsent   = findViewById(R.id.tv_count_absent);
        tvCountLeave    = findViewById(R.id.tv_count_leave);
        btnSubmit       = findViewById(R.id.btn_submit_attendance);
    }

    // ── Session header ────────────────────────────────────────────────────

    private void setupSessionHeader() {
        tvCourseName.setText(course);

        // Date
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date());
        tvSessionDate.setText(date);

        // Session time (static for now; could be fetched from Firestore schedule)
        tvSessionTime.setText("Morning Session  (09:00 – 11:30)");

        // Capacity
        tvCapacity.setText(masterList.size() + " Students");
    }

    // ── Sample data (replace with Firestore fetch) ────────────────────────

    private void populateSampleStudents() {
        masterList.add(new StudentRecord("Alexander Montgomery", "ARCH-2024-001", STATUS_PRESENT));
        masterList.add(new StudentRecord("Beatrice Wentworth",   "ARCH-2024-042", STATUS_ABSENT));
        masterList.add(new StudentRecord("Caspian J. St. James", "ARCH-2024-015", STATUS_PRESENT));
        masterList.add(new StudentRecord("Diana Harrington",     "ARCH-2024-009", STATUS_LEAVE));
        masterList.add(new StudentRecord("Edward Finch-Hatton",  "ARCH-2024-031", STATUS_PRESENT));
        masterList.add(new StudentRecord("Farrukh Tashkentov",   "ARCH-2024-007", STATUS_ABSENT));
        masterList.add(new StudentRecord("Grace Pemberton",      "ARCH-2024-018", STATUS_PRESENT));
        masterList.add(new StudentRecord("Hamza Al-Rasheed",     "ARCH-2024-023", STATUS_PRESENT));

        filteredList.addAll(masterList);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new StudentAttendanceAdapter(this, filteredList, this::updateSummary);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);
        rvStudents.setNestedScrollingEnabled(false);
    }

    // ── Search ────────────────────────────────────────────────────────────

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterStudents(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterStudents(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(masterList);
        } else {
            String lower = query.toLowerCase(Locale.getDefault());
            for (StudentRecord r : masterList) {
                if (r.name.toLowerCase(Locale.getDefault()).contains(lower)
                        || r.rollNo.toLowerCase(Locale.getDefault()).contains(lower)) {
                    filteredList.add(r);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    // ── Mark All Present ──────────────────────────────────────────────────

    private void setupMarkAllPresent() {
        btnMarkAllPresent.setOnClickListener(v -> {
            for (StudentRecord r : masterList) r.status = STATUS_PRESENT;
            adapter.notifyDataSetChanged();
            updateSummary();
            showToast("ALL STUDENTS MARKED PRESENT");
        });
    }

    // ── Summary strip ─────────────────────────────────────────────────────

    public void updateSummary() {
        int present = 0, absent = 0, leave = 0;
        for (StudentRecord r : masterList) {
            if (r.status == STATUS_PRESENT) present++;
            else if (r.status == STATUS_ABSENT) absent++;
            else if (r.status == STATUS_LEAVE) leave++;
        }
        tvCountPresent.setText(String.valueOf(present));
        tvCountAbsent.setText(String.valueOf(absent));
        tvCountLeave.setText(String.valueOf(leave));
    }

    // ── Submit ────────────────────────────────────────────────────────────

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (isSubmitting) return;
            submitAttendance();
        });
    }

    private void submitAttendance() {
        isSubmitting = true;
        btnSubmit.setText("PROCESSING REGISTRY…");
        btnSubmit.setEnabled(false);
        btnSubmit.setIconResource(R.drawable.ic_sync);

        // TODO: replace stub with real Firebase Firestore write via repository
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            btnSubmit.setIconResource(R.drawable.ic_check_circle);
            btnSubmit.setText("REGISTRY SUBMITTED");
            btnSubmit.setBackgroundTintList(
                    getColorStateList(R.color.color_success));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                btnSubmit.setText("SUBMIT ATTENDANCE REGISTRY");
                btnSubmit.setIconResource(R.drawable.ic_how_to_reg);
                btnSubmit.setBackgroundTintList(
                        getColorStateList(R.color.scholar_navy));
                btnSubmit.setEnabled(true);
                isSubmitting = false;
            }, 3000);

        }, 1500);
    }

    // ── Toolbar back ──────────────────────────────────────────────────────

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Data model
    // ══════════════════════════════════════════════════════════════════════

    public static class StudentRecord {
        public String name;
        public String rollNo;
        public int status;  // 0=present, 1=absent, 2=leave

        public StudentRecord(String name, String rollNo, int status) {
            this.name   = name;
            this.rollNo = rollNo;
            this.status = status;
        }

        /** Returns initials from the first two name words. */
        public String initials() {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                return String.valueOf(parts[0].charAt(0))
                        + parts[1].charAt(0);
            }
            return parts[0].length() >= 2
                    ? parts[0].substring(0, 2).toUpperCase()
                    : parts[0].toUpperCase();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RecyclerView adapter
    // ══════════════════════════════════════════════════════════════════════

    public static class StudentAttendanceAdapter
            extends RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder> {

        private final Context context;
        private final List<StudentRecord> list;
        private final Runnable onStatusChanged;

        public StudentAttendanceAdapter(Context context,
                                        List<StudentRecord> list,
                                        Runnable onStatusChanged) {
            this.context         = context;
            this.list            = list;
            this.onStatusChanged = onStatusChanged;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_student_attendance, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            StudentRecord record = list.get(position);

            h.tvInitials.setText(record.initials());
            h.tvName.setText(record.name);
            h.tvRollNo.setText("Roll No: " + record.rollNo);

            // Restore segmented control state without firing listener
            h.rgStatus.setOnCheckedChangeListener(null);
            switch (record.status) {
                case STATUS_PRESENT: h.rgStatus.check(h.rbPresent.getId()); break;
                case STATUS_ABSENT:  h.rgStatus.check(h.rbAbsent.getId());  break;
                case STATUS_LEAVE:   h.rgStatus.check(h.rbLeave.getId());   break;
            }

            // Live update
            h.rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == h.rbPresent.getId()) record.status = STATUS_PRESENT;
                else if (checkedId == h.rbAbsent.getId())  record.status = STATUS_ABSENT;
                else if (checkedId == h.rbLeave.getId())   record.status = STATUS_LEAVE;
                if (onStatusChanged != null) onStatusChanged.run();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView    tvInitials, tvName, tvRollNo;
            RadioGroup  rgStatus;
            RadioButton rbPresent, rbAbsent, rbLeave;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvInitials = itemView.findViewById(R.id.tv_initials);
                tvName     = itemView.findViewById(R.id.tv_student_name);
                tvRollNo   = itemView.findViewById(R.id.tv_roll_no);
                rgStatus   = itemView.findViewById(R.id.rg_status);
                rbPresent  = itemView.findViewById(R.id.rb_present);
                rbAbsent   = itemView.findViewById(R.id.rb_absent);
                rbLeave    = itemView.findViewById(R.id.rb_leave);
            }
        }
    }
}