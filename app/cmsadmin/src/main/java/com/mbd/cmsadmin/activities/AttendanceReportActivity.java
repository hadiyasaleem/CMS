package com.mbd.cmsadmin.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.adapters.AbsenceWarningAdapter;
import com.mbd.cmsadmin.adapters.AttendanceTableAdapter;
import com.mbd.cmsadmin.models.StudentAttendance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AttendanceReportActivity
 * ─────────────────────────────────────────────────────────────────────────────
 * Launched from DepartmentsAdapter when the user taps a semester tile.
 *
 * Extras (from DepartmentsAdapter):
 *   EXTRA_DEPT_ID   (String)  — Firestore document ID
 *   EXTRA_DEPT_NAME (String)  — display name, e.g. "Computer Science"
 *   EXTRA_SEMESTER  (int)     — 1–8
 *
 * Screens (Images 2 & 3):
 *   § 1  Header: archive label, dept name, subtitle
 *   § 2  Shift toggle: Morning / Evening
 *   § 3  Date picker + period tab strip
 *   § 4  Export / Print row
 *   § 5  Horizontally-scrollable attendance table (RecyclerView)
 *   § 6  Insights bento: Avg attendance · Absence warnings · Perfect attendance
 *
 * ── FAKE DATA ──
 * All table data, avg %, warnings and perfect-count are hard-coded.
 * Search for "FAKE DATA" comments and replace with Firestore queries.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class AttendanceReportActivity extends AppCompatActivity {

    // Intent extras
    public static final String EXTRA_DEPT_ID   = "dept_id";
    public static final String EXTRA_DEPT_NAME = "dept_name";
    public static final String EXTRA_SEMESTER  = "semester";

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView     tvDeptName;
    private TextView     tvSubtitle;
    private TextView     tvArchiveLabel;
    private TextView     tvSelectedDate;
    private TextView     tvAvgAttendance;
    private TextView     tvPerfectCount;

    // Shift toggle
    private TextView     btnMorning;
    private TextView     btnEvening;
    private boolean      isMorning = true;

    // Period tabs
    private TextView     tabDaily, tabWeekly, tabMonthly, tabFullSemester;
    private String       activePeriod = "MONTHLY";

    // Table
    private RecyclerView           rvAttendance;
    private AttendanceTableAdapter tableAdapter;
    private LinearLayout           headerDateColumns;

    // Absence warnings
    private RecyclerView           rvAbsenceWarnings;
    private AbsenceWarningAdapter  warningAdapter;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        // Hide default action bar — activity uses its own header
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        String deptId   = getIntent().getStringExtra(EXTRA_DEPT_ID);
        String deptName = getIntent().getStringExtra(EXTRA_DEPT_NAME);
        int    semester = getIntent().getIntExtra(EXTRA_SEMESTER, 1);

        bindViews();
        populateHeader(deptName, semester);
        setupShiftToggle();
        setupPeriodTabs();
        setupExportPrint();
        setupAttendanceTable();
        setupInsightCards();
    }

    // ── View binding ──────────────────────────────────────────────────────────

    private void bindViews() {
        tvDeptName         = findViewById(R.id.tv_dept_name);
        tvSubtitle         = findViewById(R.id.tv_subtitle);
        tvArchiveLabel     = findViewById(R.id.tv_archive_label);
        tvSelectedDate     = findViewById(R.id.tv_selected_date);
        tvAvgAttendance    = findViewById(R.id.tv_avg_attendance);
        tvPerfectCount     = findViewById(R.id.tv_perfect_count);
        btnMorning         = findViewById(R.id.btn_morning);
        btnEvening         = findViewById(R.id.btn_evening);
        tabDaily           = findViewById(R.id.tab_daily);
        tabWeekly          = findViewById(R.id.tab_weekly);
        tabMonthly         = findViewById(R.id.tab_monthly);
        tabFullSemester    = findViewById(R.id.tab_full_semester);
        rvAttendance       = findViewById(R.id.rv_attendance);
        headerDateColumns  = findViewById(R.id.header_date_columns);
        rvAbsenceWarnings  = findViewById(R.id.rv_absence_warnings);

        // Perfect attendance CTA
        findViewById(R.id.btn_view_honors).setOnClickListener(v ->
                Toast.makeText(this, "Honors List", Toast.LENGTH_SHORT).show());
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private void populateHeader(String deptName, int semester) {
        if (deptName != null) {
            tvDeptName.setText("Department of\n" + deptName);
            tvSubtitle.setText("Detailed Ledger of Attendance: Semester "
                    + toRoman(semester) + " (" + deptName + ")");
        }
    }

    // ── Shift toggle ──────────────────────────────────────────────────────────

    private void setupShiftToggle() {
        btnMorning.setOnClickListener(v -> setShift(true));
        btnEvening.setOnClickListener(v -> setShift(false));
    }

    private void setShift(boolean morning) {
        isMorning = morning;
        btnMorning.setBackgroundColor(morning
                ? getColor(R.color.primary)
                : android.graphics.Color.TRANSPARENT);
        btnMorning.setTextColor(morning
                ? getColor(R.color.on_primary)
                : getColor(R.color.on_surface_variant));
        btnEvening.setBackgroundColor(!morning
                ? getColor(R.color.primary)
                : android.graphics.Color.TRANSPARENT);
        btnEvening.setTextColor(!morning
                ? getColor(R.color.on_primary)
                : getColor(R.color.on_surface_variant));

        // TODO: reload data for the selected shift
    }

    // ── Period tabs ───────────────────────────────────────────────────────────

    private void setupPeriodTabs() {
        View.OnClickListener tabListener = v -> {
            String tag = (String) v.getTag();
            activePeriod = tag;
            updateTabStyles();
            // TODO: reload table data for new period
        };

        tabDaily.setTag("DAILY");
        tabWeekly.setTag("WEEKLY");
        tabMonthly.setTag("MONTHLY");
        tabFullSemester.setTag("FULL_SEMESTER");

        tabDaily.setOnClickListener(tabListener);
        tabWeekly.setOnClickListener(tabListener);
        tabMonthly.setOnClickListener(tabListener);
        tabFullSemester.setOnClickListener(tabListener);

        updateTabStyles(); // apply initial active style
    }

    private void updateTabStyles() {
        TextView[] tabs    = { tabDaily, tabWeekly, tabMonthly, tabFullSemester };
        String[]   tags    = { "DAILY", "WEEKLY", "MONTHLY", "FULL_SEMESTER" };
        for (int i = 0; i < tabs.length; i++) {
            boolean active = tags[i].equals(activePeriod);
            tabs[i].setTextColor(active
                    ? getColor(R.color.secondary)
                    : getColor(R.color.on_surface_variant));
            tabs[i].setBackgroundColor(active
                    ? getColor(R.color.surface_container_lowest)
                    : android.graphics.Color.TRANSPARENT);
        }
    }

    // ── Export / Print ────────────────────────────────────────────────────────

    private void setupExportPrint() {
        findViewById(R.id.btn_export).setOnClickListener(v ->
                Toast.makeText(this, "Exporting ledger…", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btn_print).setOnClickListener(v ->
                Toast.makeText(this, "Print dialog", Toast.LENGTH_SHORT).show());
    }

    // ── Attendance table ──────────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Replace buildFakeDates() and buildFakeStudents() bodies with Firestore
     * queries targeting:
     *   db.collection("attendance")
     *     .whereEqualTo("deptId", deptId)
     *     .whereEqualTo("semester", semester)
     *     .get() → map to StudentAttendance list
     */
    private void setupAttendanceTable() {
        List<String>           dates    = buildFakeDates();
        List<StudentAttendance> students = buildFakeStudents();

        // Build dynamic date header columns
        headerDateColumns.removeAllViews();
        for (String date : dates) {
            TextView th = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dpToPx(52), LinearLayout.LayoutParams.WRAP_CONTENT);
            th.setLayoutParams(lp);
            th.setText(date);
            th.setTextSize(9f);
            th.setTypeface(null, android.graphics.Typeface.BOLD);
            th.setTextColor(getColor(R.color.on_surface_variant));
            th.setGravity(android.view.Gravity.CENTER);
            th.setLetterSpacing(0.12f);
            headerDateColumns.addView(th);
        }

        tableAdapter = new AttendanceTableAdapter(students, dates);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        rvAttendance.setAdapter(tableAdapter);
        rvAttendance.setNestedScrollingEnabled(false);

        // Synchronise horizontal scroll between header and body
        HorizontalScrollSyncHelper.sync(
                findViewById(R.id.hsv_table_header),
                findViewById(R.id.hsv_table_body));
    }

    // ── Insight cards ─────────────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Replace with computed values from Firestore attendance data.
     */
    private void setupInsightCards() {
        // Avg attendance — already set in layout default; update here
        tvAvgAttendance.setText("88.4%");
        tvPerfectCount.setText("14 Students");

        // Absence warnings
        List<AbsenceWarningAdapter.WarningEntry> warnings = new ArrayList<>();
        warnings.add(new AbsenceWarningAdapter.WarningEntry("Elias Sterling", 70.0f));
        warnings.add(new AbsenceWarningAdapter.WarningEntry("Finnian Cole",   72.5f));

        warningAdapter = new AbsenceWarningAdapter(warnings);
        rvAbsenceWarnings.setLayoutManager(new LinearLayoutManager(this));
        rvAbsenceWarnings.setAdapter(warningAdapter);
        rvAbsenceWarnings.setNestedScrollingEnabled(false);
    }

    // ── FAKE DATA helpers ─────────────────────────────────────────────────────

    private List<String> buildFakeDates() {
        return new ArrayList<>(Arrays.asList(
                "01\nOCT", "02\nOCT", "03\nOCT", "04\nOCT", "05\nOCT",
                "08\nOCT", "09\nOCT", "10\nOCT", "11\nOCT", "12\nOCT"
        ));
    }

    private List<StudentAttendance> buildFakeStudents() {
        List<StudentAttendance> list = new ArrayList<>();

        list.add(new StudentAttendance("Alistair Thorne",  "ROLL NO: TP-2023-001",
                Arrays.asList("P","P","A","P","P","P","P","P","P","P"), 90.0f));
        list.add(new StudentAttendance("Beatrice Vance",   "ROLL NO: TP-2023-002",
                Arrays.asList("P","P","P","P","P","P","P","P","P","P"), 100.0f));
        list.add(new StudentAttendance("Caspian Reed",     "ROLL NO: TP-2023-005",
                Arrays.asList("A","P","P","P","A","P","P","P","P","P"), 80.0f));
        list.add(new StudentAttendance("Dorothea Blackwell","ROLL NO: TP-2023-008",
                Arrays.asList("P","P","P","P","P","P","P","P","P","P"), 100.0f));
        list.add(new StudentAttendance("Elias Sterling",   "ROLL NO: TP-2023-012",
                Arrays.asList("P","A","A","P","P","P","P","P","P","A"), 70.0f));

        return list;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static String toRoman(int n) {
        String[] roman = {"I","II","III","IV","V","VI","VII","VIII"};
        return (n >= 1 && n <= 8) ? roman[n - 1] : String.valueOf(n);
    }

    // ── Horizontal scroll sync helper (inner class) ───────────────────────────

    /**
     * Synchronises two HorizontalScrollViews so that the date header
     * and the table body always scroll in lockstep.
     */
    private static class HorizontalScrollSyncHelper {
        static void sync(android.widget.HorizontalScrollView header,
                         android.widget.HorizontalScrollView body) {
            if (header == null || body == null) return;

            header.getViewTreeObserver().addOnScrollChangedListener(() ->
                    body.scrollTo(header.getScrollX(), body.getScrollY()));
            body.getViewTreeObserver().addOnScrollChangedListener(() ->
                    header.scrollTo(body.getScrollX(), header.getScrollY()));
        }
    }
}