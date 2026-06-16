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
import com.mbd.cmsteacher.adapters.MidtermMarkAdapter;
import com.mbd.cmsteacher.models.MidtermMarkEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MidtermMarkEntryActivity — Midterm Mark Entry Screen
 * ─────────────────────────────────────────────────────────────────────────
 * Matches the HTML "Imperial Chancery - Midterm Mark Entry" reference.
 *
 * Features:
 *   • Subject spinner (fake data — replace with Firestore query)
 *   • Search bar — live filter by name or roll number
 *   • RecyclerView of student rows (MidtermMarkAdapter)
 *       ↳ Single score EditText per student, max 100
 *       ↳ Value > 100 → red error style; negative → clamped to 0
 *   • Save Draft → Toast (TODO: Firestore draft persist)
 *   • Submit Marks → validates all entered, then submits with
 *       button loading state → success state (matches HTML JS simulation)
 *
 * Navigation:
 *   Started from ExamsFragment when selectedAssessment == TYPE_MIDTERM.
 *   Accepts extras: EXTRA_YEAR, EXTRA_SEMESTER, EXTRA_DEPARTMENT
 * ─────────────────────────────────────────────────────────────────────────
 */
public class MidtermMarkEntryActivity extends AppCompatActivity {

    // ── Intent extras ─────────────────────────────────────────────────────
    public static final String EXTRA_YEAR       = "extra_year";
    public static final String EXTRA_SEMESTER   = "extra_semester";
    public static final String EXTRA_DEPARTMENT = "extra_department";

    // ── Views ─────────────────────────────────────────────────────────────
    private Spinner          spinnerSubject;
    private EditText         etSearch;
    private RecyclerView     rvMarks;
    private MaterialButton   btnSaveDraft;
    private MaterialButton   btnSubmitMarks;

    // ── Data ──────────────────────────────────────────────────────────────
    private MidtermMarkAdapter       adapter;
    private List<MidtermMarkEntry>   masterList;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_midterm_mark_entry);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setupSubjectSpinner();
        setupRecyclerView();
        setupSearch();
        setupActionButtons();
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews() {
        spinnerSubject = findViewById(R.id.spinner_subject);
        etSearch       = findViewById(R.id.et_search);
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
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subjects);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(spinnerAdapter);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────

    private void setupRecyclerView() {
        masterList = loadFakeStudents();
        adapter = new MidtermMarkAdapter(new ArrayList<>(masterList));
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
        String lower = query.toLowerCase();
        List<MidtermMarkEntry> filtered = new ArrayList<>();

        for (MidtermMarkEntry e : masterList) {
            if (e.getStudentName().toLowerCase().contains(lower)
                    || e.getRollNumber().toLowerCase().contains(lower)) {
                filtered.add(e);
            }
        }

        adapter.getEntries().clear();
        adapter.getEntries().addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    // ── Action buttons ────────────────────────────────────────────────────

    private void setupActionButtons() {

        btnSaveDraft.setOnClickListener(v -> {
            // TODO: persist current state to Firestore as a draft
            Toast.makeText(this, "Draft saved.", Toast.LENGTH_SHORT).show();
        });

        btnSubmitMarks.setOnClickListener(v -> {
            // Validate: every student must have a score entered
            List<MidtermMarkEntry> entries = adapter.getEntries();

            // Check for out-of-range errors first
            for (MidtermMarkEntry e : entries) {
                if (e.isEntered() && e.getMarks() > 100) {
                    Toast.makeText(this,
                            "Fix scores exceeding 100 before submitting.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            // Check for missing entries
            int missing = 0;
            for (MidtermMarkEntry e : entries) {
                if (!e.isEntered()) missing++;
            }
            if (missing > 0) {
                Toast.makeText(this,
                        missing + " student(s) have no mark entered.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // ── Submission loading state (matches HTML JS simulation) ──
            btnSubmitMarks.setEnabled(false);
            btnSubmitMarks.setText("SUBMITTING…");
            btnSubmitMarks.setIconResource(R.drawable.ic_refresh);

            btnSubmitMarks.postDelayed(() -> {
                // TODO: write finalized marks to Firestore here
                btnSubmitMarks.setText("SUBMITTED");
                btnSubmitMarks.setIconResource(R.drawable.ic_verified);
                btnSubmitMarks.setBackgroundTintList(
                        getResources().getColorStateList(R.color.secondary, null));

                btnSubmitMarks.postDelayed(() -> {
                    btnSubmitMarks.setText("SUBMIT MARKS");
                    btnSubmitMarks.setIconResource(R.drawable.ic_verified);
                    btnSubmitMarks.setBackgroundTintList(
                            getResources().getColorStateList(R.color.primary, null));
                    btnSubmitMarks.setEnabled(true);
                }, 3000);

            }, 1500);
        });
    }

    // ── Fake data ─────────────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Replace with a Firestore query filtered by department + semester.
     */
    private List<MidtermMarkEntry> loadFakeStudents() {
        List<MidtermMarkEntry> list = new ArrayList<>();

        list.add(new MidtermMarkEntry(
                "Ahsan Raza",   "2024-CS-001", "Final Year • CS",
                R.drawable.ic_teacher_placeholder, 88));

        list.add(new MidtermMarkEntry(
                "Fatima Malik", "2024-CS-002", "Final Year • CS",
                R.drawable.ic_teacher_placeholder));        // no mark yet

        list.add(new MidtermMarkEntry(
                "Bilal Ahmed",  "2024-CS-003", "Final Year • CS",
                R.drawable.ic_teacher_placeholder, 94));

        list.add(new MidtermMarkEntry(
                "Sara Khan",    "2024-CS-004", "Final Year • CS",
                R.drawable.ic_teacher_placeholder, 76));

        list.add(new MidtermMarkEntry(
                "Usman Tariq",  "2024-CS-005", "Final Year • CS",
                R.drawable.ic_teacher_placeholder));        // no mark yet

        list.add(new MidtermMarkEntry(
                "Hina Javed",   "2024-CS-006", "Final Year • CS",
                R.drawable.ic_teacher_placeholder, 81));

        return list;
    }
}