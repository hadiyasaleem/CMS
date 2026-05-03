package com.mbd.cmsadmin.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mbd.cmsadmin.R;

/**
 * TimeTableManagementActivity — Academic Schedule Management (Screen 2)
 *
 * Displays the time-table grid for a selected semester and session.
 * Matches the HTML "Time Table Management" design exactly:
 *
 *   • Top app bar: account_balance icon + "Academic Ledger" title + page subtitle + avatar
 *   • Hero: institution name + red accent bar + description
 *   • Controls row: Semester spinner, Session spinner, Add New Slot button, Customize Grid button
 *   • Scrollable timetable grid:
 *       Rows    = Departments (Computer Science, Physics, Mathematics)
 *       Columns = Time Slots  (08:00-08:45, 08:45-09:30, 09:30-10:15, 10:15-11:00)
 *       Cells   = Subject card (name, code, teacher) OR status label (Free Slot, Lab Session…)
 *   • "Add Department" row at the bottom of the table
 *
 * The grid is built programmatically so it can handle any number of
 * departments / slots. For now it is seeded with static demo data
 * matching the HTML prototype. Wire Firestore in the "TODO" blocks.
 *
 * Layout: activity_timetable_management.xml
 */
public class TimeTableManagementActivity extends AppCompatActivity {

    // ── Spinner data ──────────────────────────────────────────────────────────
    private static final String[] SEMESTERS = {
            "Semester 1", "Semester 2", "Semester 4", "Semester 6", "Semester 8"
    };
    private static final String[] SESSIONS = { "Morning", "Evening" };

    // ── Views ─────────────────────────────────────────────────────────────────
    private Spinner  spinnerSemester;
    private Spinner  spinnerSession;
    private LinearLayout btnAddSlot;
    private LinearLayout btnCustomizeGrid;
    private LinearLayout btnBack;
    private LinearLayout containerDeptRows;   // filled programmatically

    // ── State ─────────────────────────────────────────────────────────────────
    private int selectedSemesterIndex = 3;   // default: Semester 6
    private int selectedSessionIndex  = 0;   // default: Morning

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table_management);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.timetableRoot), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        bindViews();
        setupSpinners();
        setupButtons();
        buildTimetableGrid();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void bindViews() {
        spinnerSemester    = findViewById(R.id.spinnerSemester);
        spinnerSession     = findViewById(R.id.spinnerSession);
        btnAddSlot         = findViewById(R.id.btnAddSlot);
        btnCustomizeGrid   = findViewById(R.id.btnCustomizeGrid);
        btnBack            = findViewById(R.id.btnBackTimetable);
        containerDeptRows  = findViewById(R.id.containerDeptRows);
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void setupSpinners() {
        // Semester spinner
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_semester, SEMESTERS);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semAdapter);
        spinnerSemester.setSelection(selectedSemesterIndex);
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedSemesterIndex = pos;
                buildTimetableGrid();    // refresh grid on change
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Session spinner
        ArrayAdapter<String> sessionAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_semester, SESSIONS);
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSession.setAdapter(sessionAdapter);
        spinnerSession.setSelection(selectedSessionIndex);
        spinnerSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedSessionIndex = pos;
                buildTimetableGrid();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void setupButtons() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnAddSlot.setOnClickListener(v ->
                Toast.makeText(this, "Add New Slot — coming soon", Toast.LENGTH_SHORT).show());

        btnCustomizeGrid.setOnClickListener(v ->
                Toast.makeText(this, "Customize Grid — coming soon", Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds the timetable department rows programmatically from demo data.
     * Replace loadDemoData() with a Firestore query when ready.
     */
    private void buildTimetableGrid() {
        if (containerDeptRows == null) return;
        containerDeptRows.removeAllViews();

        // ── FAKE DATA ── swap with Firestore ──────────────────────────────
        DepartmentRow[] rows = loadDemoData();
        // ─────────────────────────────────────────────────────────────────

        for (DepartmentRow dept : rows) {
            View row = buildDepartmentRow(dept);
            containerDeptRows.addView(row);

            // Thin divider between rows
            View divider = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(getResources().getColor(R.color.surface_container, null));
            containerDeptRows.addView(divider);
        }

        // "Add Department" row
        containerDeptRows.addView(buildAddDepartmentRow());
    }

    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Inflates a single department row: dept label + cells for each slot.
     */
    private View buildDepartmentRow(DepartmentRow dept) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setBackgroundColor(getResources().getColor(R.color.surface_container_low, null));

        // ── Department label (left sticky column) ────────────────────────
        LinearLayout labelCol = new LinearLayout(this);
        labelCol.setOrientation(LinearLayout.HORIZONTAL);
        labelCol.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(dpToPx(120), dpToPx(100));
        labelCol.setLayoutParams(labelLp);
        labelCol.setPadding(dpToPx(12), dpToPx(12), dpToPx(8), dpToPx(12));

        // Coloured left accent bar
        View accent = new View(this);
        LinearLayout.LayoutParams accentLp = new LinearLayout.LayoutParams(dpToPx(3), dpToPx(32));
        accentLp.setMarginEnd(dpToPx(8));
        accent.setLayoutParams(accentLp);
        accent.setBackgroundColor(getResources().getColor(dept.accentColor, null));
        labelCol.addView(accent);

        TextView deptName = new TextView(this);
        deptName.setText(dept.name);
        deptName.setTextSize(12f);
        deptName.setTypeface(null, android.graphics.Typeface.BOLD);
        deptName.setTextColor(getResources().getColor(R.color.on_surface, null));
        labelCol.addView(deptName);

        row.addView(labelCol);

        // ── Cells for each slot ─────────────────────────────────────────
        for (SlotCell cell : dept.slots) {
            View cellView = buildSlotCell(cell);
            LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(
                    dpToPx(150), dpToPx(100));
            cellLp.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            cellView.setLayoutParams(cellLp);
            row.addView(cellView);
        }

        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Builds a single timetable cell:
     *   - Subject card (white bg, blue left border, title + code + teacher)
     *   - OR status label cell (grey dashed, e.g. "Free Slot", "Lab Session")
     */
    private View buildSlotCell(SlotCell cell) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(android.view.Gravity.CENTER);

        if (cell.isEmpty) {
            // Status label cell (Free Slot / Lab Session / Meeting / Seminar)
            wrapper.setBackgroundResource(R.drawable.bg_timetable_empty_cell);
            wrapper.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            TextView status = new TextView(this);
            status.setText(cell.statusLabel);
            status.setTextSize(8f);
            status.setTypeface(null, android.graphics.Typeface.BOLD);
            status.setAllCaps(true);
            status.setLetterSpacing(0.15f);
            status.setTextColor(getResources().getColor(R.color.outline, null));
            status.setGravity(android.view.Gravity.CENTER);
            wrapper.addView(status);

        } else {
            // Subject card
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            wrapper.setBackgroundColor(getResources().getColor(R.color.surface_container_lowest, null));
            wrapper.setElevation(dpToPx(1));

            // Blue left border
            View border = new View(this);
            border.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(3),
                    LinearLayout.LayoutParams.MATCH_PARENT));
            border.setBackgroundColor(getResources().getColor(R.color.on_primary_container, null));
            wrapper.addView(border);

            // Content column
            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            content.setLayoutParams(contentLp);

            TextView subjectName = new TextView(this);
            subjectName.setText(cell.subjectName);
            subjectName.setTextSize(11f);
            subjectName.setTypeface(null, android.graphics.Typeface.BOLD);
            subjectName.setTextColor(getResources().getColor(R.color.on_surface, null));
            content.addView(subjectName);

            TextView subjectCode = new TextView(this);
            subjectCode.setText(cell.subjectCode);
            subjectCode.setTextSize(8f);
            subjectCode.setTypeface(null, android.graphics.Typeface.BOLD);
            subjectCode.setTextColor(getResources().getColor(R.color.primary, null));
            subjectCode.setAlpha(0.6f);
            content.addView(subjectCode);

            // Spacer
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
            content.addView(spacer);

            // Teacher row
            LinearLayout teacherRow = new LinearLayout(this);
            teacherRow.setOrientation(LinearLayout.HORIZONTAL);
            teacherRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView teacherName = new TextView(this);
            teacherName.setText(cell.teacherName);
            teacherName.setTextSize(8.5f);
            teacherName.setTextColor(getResources().getColor(R.color.on_surface_variant, null));
            teacherName.setTypeface(android.graphics.Typeface.defaultFromStyle(
                    android.graphics.Typeface.ITALIC));
            teacherRow.addView(teacherName);
            content.addView(teacherRow);

            wrapper.addView(content);

            // Edit button (top-right)
            wrapper.setOnClickListener(v ->
                    Toast.makeText(this, "Edit: " + cell.subjectName, Toast.LENGTH_SHORT).show());
        }

        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    private View buildAddDepartmentRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(56)));
        row.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        row.setBackgroundColor(getResources().getColor(R.color.surface_container_low, null));
        row.setClickable(true);
        row.setFocusable(true);
        row.setForeground(obtainStyledAttributes(new int[]{
                android.R.attr.selectableItemBackground}).getDrawable(0));

        // Dashed border background applied to inner view
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.HORIZONTAL);
        inner.setGravity(android.view.Gravity.CENTER);
        inner.setBackgroundResource(R.drawable.bg_add_dept_row);
        LinearLayout.LayoutParams innerLp = new LinearLayout.LayoutParams(
                dpToPx(180), dpToPx(40));
        inner.setLayoutParams(innerLp);
        inner.setPadding(dpToPx(12), 0, dpToPx(12), 0);

        TextView label = new TextView(this);
        label.setText("+ ADD DEPARTMENT");
        label.setTextSize(9f);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setLetterSpacing(0.15f);
        label.setTextColor(getResources().getColor(R.color.on_surface_variant, null));
        inner.addView(label);

        row.addView(inner);
        row.setOnClickListener(v ->
                Toast.makeText(this, "Add Department — coming soon", Toast.LENGTH_SHORT).show());

        return row;
    }

    // ── Demo data ─────────────────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Replace with Firestore query:
     *   db.collection("timetable")
     *     .whereEqualTo("semester", SEMESTERS[selectedSemesterIndex])
     *     .whereEqualTo("session", SESSIONS[selectedSessionIndex])
     *     .get()
     *     .addOnSuccessListener(snapshot -> { ... buildTimetableGrid(...) });
     */
    private DepartmentRow[] loadDemoData() {
        return new DepartmentRow[]{
                new DepartmentRow("Computer Science", R.color.secondary,
                        new SlotCell[]{
                                SlotCell.subject("Data Structures", "CS-301", "Prof. Alistair Vance"),
                                SlotCell.subject("Operating Systems", "CS-304", "Dr. Sarah Jenkins"),
                                SlotCell.status("Lab Session"),
                                SlotCell.subject("Algorithm Design", "CS-308", "Prof. Vance")
                        }),
                new DepartmentRow("Physics", R.color.tertiary_fixed_dim,
                        new SlotCell[]{
                                SlotCell.subject("Quantum Mechanics", "PH-402", "Dr. Robert Oppen"),
                                SlotCell.status("Free Slot"),
                                SlotCell.subject("Electromagnetism", "PH-405", "Prof. Marie Curie"),
                                SlotCell.subject("Nuclear Physics", "PH-409", "Dr. Fermi")
                        }),
                new DepartmentRow("Mathematics", R.color.on_primary_container,
                        new SlotCell[]{
                                SlotCell.status("Meeting"),
                                SlotCell.subject("Linear Algebra", "MA-201", "Dr. Emmy Noether"),
                                SlotCell.subject("Vector Calculus", "MA-205", "Prof. Gauss"),
                                SlotCell.status("Seminar")
                        })
        };
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);
    }

    // ── Data classes ──────────────────────────────────────────────────────────

    /** Represents a single department row in the timetable. */
    private static class DepartmentRow {
        final String     name;
        final int        accentColor;   // color resource id
        final SlotCell[] slots;

        DepartmentRow(String name, int accentColor, SlotCell[] slots) {
            this.name        = name;
            this.accentColor = accentColor;
            this.slots       = slots;
        }
    }

    /** Represents one timetable cell — either a subject card or a status label. */
    private static class SlotCell {
        final boolean isEmpty;
        final String  subjectName;
        final String  subjectCode;
        final String  teacherName;
        final String  statusLabel;

        private SlotCell(boolean isEmpty, String subjectName, String subjectCode,
                         String teacherName, String statusLabel) {
            this.isEmpty     = isEmpty;
            this.subjectName = subjectName;
            this.subjectCode = subjectCode;
            this.teacherName = teacherName;
            this.statusLabel = statusLabel;
        }

        static SlotCell subject(String name, String code, String teacher) {
            return new SlotCell(false, name, code, teacher, null);
        }

        static SlotCell status(String label) {
            return new SlotCell(true, null, null, null, label);
        }
    }
}