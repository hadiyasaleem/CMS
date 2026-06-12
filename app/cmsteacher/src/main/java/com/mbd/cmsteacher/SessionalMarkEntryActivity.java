package com.mbd.cmsteacher;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mbd.cmsteacher.adapters.SessionalMarkAdapter;
import com.mbd.cmsteacher.models.SessionalMarkEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SessionalMarkEntryActivity — Sessional Mark Entry Screen
 * ─────────────────────────────────────────────────────────────────────────
 * Matches the HTML "Imperial Chancery - Sessional Mark Entry" reference.
 *
 * Features:
 *   • Subject spinner (fake data — replace with Firestore query)
 *   • Search bar — filters adapter list by name or roll number
 *   • Sort toggle — Roll Number (default) / Name
 *   • RecyclerView — one row per student (SessionalMarkAdapter)
 *       ↳ Attendance (max 10) | Assignments (max 20) | Quizzes (max 20)
 *       ↳ Auto-calculated total with gold flash animation
 *       ↳ Red border + red text when value exceeds max
 *   • Save Draft → Snackbar confirmation (TODO: persist to Firestore)
 *   • Submit Marks → validates completeness, then submits (TODO: Firestore)
 *
 * Navigation:
 *   Started from ExamsFragment after "Proceed to Subject List" → subject select.
 *   Pass extras: EXTRA_YEAR, EXTRA_SEMESTER, EXTRA_DEPARTMENT, EXTRA_ASSESSMENT_TYPE
 * ─────────────────────────────────────────────────────────────────────────
 */
public class SessionalMarkEntryActivity extends AppCompatActivity {

    // ── Intent extras ─────────────────────────────────────────────────────
    public static final String EXTRA_YEAR            = "extra_year";
    public static final String EXTRA_SEMESTER        = "extra_semester";
    public static final String EXTRA_DEPARTMENT      = "extra_department";
    public static final String EXTRA_ASSESSMENT_TYPE = "extra_assessment_type";

    // ── Sort modes ────────────────────────────────────────────────────────
    private static final int SORT_ROLL = 0;
    private static final int SORT_NAME = 1;

    // ── Views ─────────────────────────────────────────────────────────────
    private Spinner               spinnerSubject;
    private EditText              etSearch;
    private MaterialButton        btnSortRoll;
    private MaterialButton        btnSortName;
    private RecyclerView          rvMarks;
    private MaterialButton        btnSaveDraft;
    private MaterialButton        btnSubmitMarks;

    // ── Data ──────────────────────────────────────────────────────────────
    private SessionalMarkAdapter  adapter;
    private List<SessionalMarkEntry> masterList;   // unfiltered full list
    private int                   currentSort = SORT_ROLL;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessional_mark_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setupSubjectSpinner();
        setupRecyclerView();
        setupSearch();
        setupSortButtons();
        setupActionButtons();
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews() {
        spinnerSubject = findViewById(R.id.spinner_subject);
        etSearch       = findViewById(R.id.et_search);
        btnSortRoll    = findViewById(R.id.btn_sort_roll);
        btnSortName    = findViewById(R.id.btn_sort_name);
        rvMarks        = findViewById(R.id.rv_marks);
        btnSaveDraft   = findViewById(R.id.btn_save_draft);
        btnSubmitMarks = findViewById(R.id.btn_submit_marks);
    }

    // ── Subject spinner ───────────────────────────────────────────────────

    private void setupSubjectSpinner() {
        // ── FAKE DATA ── replace with subjects fetched from Firestore
        //    filtered by department + semester passed via Intent extras.
        String[] subjects = {
                "Data Structures (CS-301)",
                "Operating Systems (CS-401)",
                "Software Engineering (CS-402)",
                "Database Systems (CS-403)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────

    private void setupRecyclerView() {
        masterList = loadFakeStudents();

        adapter = new SessionalMarkAdapter(new ArrayList<>(masterList));
        rvMarks.setLayoutManager(new LinearLayoutManager(this));
        rvMarks.setAdapter(adapter);
        rvMarks.setNestedScrollingEnabled(false);
    }

    // ── Search ────────────────────────────────────────────────────────────

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c)     {}

            @Override
            public void afterTextChanged(Editable s) {
                filterList(s.toString().trim());
            }
        });
    }

    private void filterList(String query) {
        List<SessionalMarkEntry> filtered = new ArrayList<>();
        String lower = query.toLowerCase();

        for (SessionalMarkEntry e : masterList) {
            if (e.getStudentName().toLowerCase().contains(lower)
                    || e.getRollNumber().toLowerCase().contains(lower)) {
                filtered.add(e);
            }
        }

        adapter.getEntries().clear();
        adapter.getEntries().addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    // ── Sort buttons ──────────────────────────────────────────────────────

    private void setupSortButtons() {
        btnSortRoll.setOnClickListener(v -> applySort(SORT_ROLL));
        btnSortName.setOnClickListener(v -> applySort(SORT_NAME));
    }

    private void applySort(int mode) {
        currentSort = mode;

        // Update button visual states
        if (mode == SORT_ROLL) {
            btnSortRoll.setBackgroundTintList(
                    getResources().getColorStateList(R.color.primary, null));
            btnSortRoll.setTextColor(getResources().getColor(R.color.on_primary, null));
            btnSortName.setBackgroundTintList(
                    getResources().getColorStateList(R.color.surface_container_high, null));
            btnSortName.setTextColor(getResources().getColor(R.color.on_surface_variant, null));
        } else {
            btnSortName.setBackgroundTintList(
                    getResources().getColorStateList(R.color.primary, null));
            btnSortName.setTextColor(getResources().getColor(R.color.on_primary, null));
            btnSortRoll.setBackgroundTintList(
                    getResources().getColorStateList(R.color.surface_container_high, null));
            btnSortRoll.setTextColor(getResources().getColor(R.color.on_surface_variant, null));
        }

        // Sort the working list
        List<SessionalMarkEntry> working = adapter.getEntries();
        if (mode == SORT_ROLL) {
            Collections.sort(working,
                    (a, b) -> a.getRollNumber().compareTo(b.getRollNumber()));
        } else {
            Collections.sort(working,
                    (a, b) -> a.getStudentName().compareToIgnoreCase(b.getStudentName()));
        }
        adapter.notifyDataSetChanged();
    }

    // ── Action buttons ────────────────────────────────────────────────────

    private void setupActionButtons() {

        btnSaveDraft.setOnClickListener(v -> {
            // TODO: persist current adapter state to Firestore as a draft
            Toast.makeText(this, "Draft saved successfully.", Toast.LENGTH_SHORT).show();
        });

        btnSubmitMarks.setOnClickListener(v -> {
            // Validate: every student must have all three fields entered
            List<SessionalMarkEntry> entries = adapter.getEntries();
            int incomplete = 0;
            for (SessionalMarkEntry e : entries) {
                if (!e.isFullyEntered()) incomplete++;
            }

            if (incomplete > 0) {
                Toast.makeText(this,
                        incomplete + " student(s) have incomplete marks.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // TODO: write finalized marks to Firestore and mark as submitted
            Toast.makeText(this, "Marks submitted successfully.", Toast.LENGTH_SHORT).show();
            btnSubmitMarks.setEnabled(false);
            btnSubmitMarks.setAlpha(0.5f);
        });
    }

    // ── Fake data ─────────────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Replace with a Firestore query:
     *   db.collection("students")
     *     .whereEqualTo("department", dept)
     *     .whereEqualTo("semester", semester)
     *     .get()
     *     .addOnSuccessListener(...)
     */
    private List<SessionalMarkEntry> loadFakeStudents() {
        List<SessionalMarkEntry> list = new ArrayList<>();

        list.add(new SessionalMarkEntry(
                "Ahsan Raza",     "2024-CS-001",
                R.drawable.ic_teacher_placeholder, 9, 18, 16));

        list.add(new SessionalMarkEntry(
                "Fatima Malik",   "2024-CS-002",
                R.drawable.ic_teacher_placeholder, 10, 19, 20));

        list.add(new SessionalMarkEntry(
                "Bilal Ahmed",    "2024-CS-003",
                R.drawable.ic_teacher_placeholder));   // no marks entered yet

        list.add(new SessionalMarkEntry(
                "Sara Khan",      "2024-CS-004",
                R.drawable.ic_teacher_placeholder, 8, 15, 17));

        list.add(new SessionalMarkEntry(
                "Usman Tariq",    "2024-CS-005",
                R.drawable.ic_teacher_placeholder));   // no marks entered yet

        list.add(new SessionalMarkEntry(
                "Hina Javed",     "2024-CS-006",
                R.drawable.ic_teacher_placeholder, 7, 12, 14));

        return list;
    }
}