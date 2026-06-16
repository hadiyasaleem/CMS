package com.mbd.cmsteacher.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.mbd.cmsteacher.MarkAttendanceActivity;
import com.mbd.cmsteacher.R;

/**
 * AttendanceFragment — Attendance Registry Screen
 * ─────────────────────────────────────────────────────────────────────────
 * Matches the HTML "Attendance Registry | Academic Faculty" reference.
 *
 * Sections:
 *   § 1  Hero header: "Attendance Registry" + gold rule + session eyebrow
 *   § 2  Assigned Departments — two course cards (Architecture & Urban Planning)
 *   § 3  Academic Period — 8 semester selector buttons
 *   § 4  "MARK DAILY ATTENDANCE" primary CTA → launches MarkAttendanceActivity
 *   § 5  Faculty Integrity Standards card (90% compliance ring, audit summary)
 *
 * State:
 *   selectedDepartment — index of the tapped course card (0 or 1)
 *   selectedSemester   — 1–8, default 4
 *
 * Navigation:
 *   CTA button → MarkAttendanceActivity
 *     extras: EXTRA_DEPARTMENT, EXTRA_SEMESTER
 * ─────────────────────────────────────────────────────────────────────────
 */
public class AttendanceFragment extends Fragment {

    // ── Intent extras (forwarded to MarkAttendanceActivity) ──────────────
    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_SEMESTER   = "extra_semester";

    // ── Department constants ──────────────────────────────────────────────
    private static final int DEPT_ARCHITECTURE   = 0;
    private static final int DEPT_URBAN_PLANNING = 1;

    // ── Views — Department cards ──────────────────────────────────────────
    private View cardArchitecture;
    private View cardUrbanPlanning;

    // ── Views — Semester buttons ──────────────────────────────────────────
    private MaterialButton[] semesterButtons = new MaterialButton[8];

    // ── Views — CTA ───────────────────────────────────────────────────────
    private MaterialButton btnMarkAttendance;

    // ── State ─────────────────────────────────────────────────────────────
    private int selectedDepartment = DEPT_ARCHITECTURE;  // default: first card
    private int selectedSemester   = 4;                  // default: semester 4

    // ─────────────────────────────────────────────────────────────────────
    public AttendanceFragment() { /* required empty constructor */ }

    public static AttendanceFragment newInstance() {
        return new AttendanceFragment();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupDepartmentCards();
        setupSemesterButtons();
        setupCtaButton();

        // Apply default visual selections
        applyDepartmentSelection(selectedDepartment);
        applySemesterSelection(selectedSemester);
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews(View root) {
        cardArchitecture  = root.findViewById(R.id.card_architecture);
        cardUrbanPlanning = root.findViewById(R.id.card_urban_planning);

        semesterButtons[0] = root.findViewById(R.id.btn_sem_1);
        semesterButtons[1] = root.findViewById(R.id.btn_sem_2);
        semesterButtons[2] = root.findViewById(R.id.btn_sem_3);
        semesterButtons[3] = root.findViewById(R.id.btn_sem_4);
        semesterButtons[4] = root.findViewById(R.id.btn_sem_5);
        semesterButtons[5] = root.findViewById(R.id.btn_sem_6);
        semesterButtons[6] = root.findViewById(R.id.btn_sem_7);
        semesterButtons[7] = root.findViewById(R.id.btn_sem_8);

        btnMarkAttendance = root.findViewById(R.id.btn_mark_attendance);
    }

    // ── Department cards ──────────────────────────────────────────────────

    private void setupDepartmentCards() {
        cardArchitecture.setOnClickListener(v -> {
            selectedDepartment = DEPT_ARCHITECTURE;
            applyDepartmentSelection(selectedDepartment);
        });

        cardUrbanPlanning.setOnClickListener(v -> {
            selectedDepartment = DEPT_URBAN_PLANNING;
            applyDepartmentSelection(selectedDepartment);
        });
    }

    /**
     * Highlights the selected card with a gold left border tint;
     * the deselected card reverts to the navy tint.
     */
    private void applyDepartmentSelection(int dept) {
        // Use elevation delta as a simple active/inactive indicator
        // (border tints are defined in the layout via background drawables)
        if (dept == DEPT_ARCHITECTURE) {
            cardArchitecture.setSelected(true);
            cardUrbanPlanning.setSelected(false);
        } else {
            cardArchitecture.setSelected(false);
            cardUrbanPlanning.setSelected(true);
        }
    }

    // ── Semester buttons ──────────────────────────────────────────────────

    private void setupSemesterButtons() {
        for (int i = 0; i < semesterButtons.length; i++) {
            final int semesterNumber = i + 1;
            semesterButtons[i].setOnClickListener(v -> {
                selectedSemester = semesterNumber;
                applySemesterSelection(selectedSemester);
            });
        }
    }

    /**
     * Gives the active semester button a gold border + faint gold bg;
     * all others get the default outline style.
     */
    private void applySemesterSelection(int semester) {
        for (int i = 0; i < semesterButtons.length; i++) {
            MaterialButton btn = semesterButtons[i];
            if (i + 1 == semester) {
                // Active state — gold border
                btn.setBackgroundTintList(
                        requireContext().getColorStateList(R.color.tertiary_fixed));
                btn.setTextColor(
                        requireContext().getColor(R.color.primary));
                btn.setStrokeColor(
                        requireContext().getColorStateList(R.color.tertiary_fixed_dim));
                btn.setStrokeWidth(dpToPx(2));
            } else {
                // Inactive state
                btn.setBackgroundTintList(
                        requireContext().getColorStateList(R.color.surface_container_low));
                btn.setTextColor(
                        requireContext().getColor(R.color.primary));
                btn.setStrokeColor(
                        requireContext().getColorStateList(R.color.outline_variant));
                btn.setStrokeWidth(dpToPx(1));
            }
        }
    }

    // ── CTA button ────────────────────────────────────────────────────────

    private void setupCtaButton() {
        btnMarkAttendance.setOnClickListener(v -> launchMarkAttendance());
    }

    private void launchMarkAttendance() {
        String deptName = (selectedDepartment == DEPT_ARCHITECTURE)
                ? "Architecture & Design"
                : "Urban Planning";

        String courseName = (selectedDepartment == DEPT_ARCHITECTURE)
                ? "Advanced Architecture III"
                : "Sustainable Urbanism";

        Intent intent = new Intent(requireContext(), MarkAttendanceActivity.class);
        intent.putExtra(MarkAttendanceActivity.EXTRA_DEPARTMENT,  deptName);
        intent.putExtra(MarkAttendanceActivity.EXTRA_COURSE,      courseName);
        intent.putExtra(MarkAttendanceActivity.EXTRA_SEMESTER,    selectedSemester);
        startActivity(intent);
        requireActivity().overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out);
    }

    // ── Utility ───────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}