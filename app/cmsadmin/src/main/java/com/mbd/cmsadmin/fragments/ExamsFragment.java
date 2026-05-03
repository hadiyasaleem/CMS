package com.mbd.cmsadmin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.activities.ExamsReportActivity;

/**
 * ExamFragment — Department & Semester Registry Selection
 *
 * Screen 1 of the Exams flow (matches HTML: exams-department-selection page).
 *
 * Layout:
 *   • Hero header: "Departmental Examination Registry Selection."
 *   • Computer Science bento card — 8 semester buttons (grid 2-col)
 *   • Mathematics card — expandable semester dropdown
 *   • English card — "View Semester Modules" action button
 *   • Physics card — horizontal scrollable semester tabs
 *   • CTA banner: "Need specific examination accommodation?"
 *
 * Navigation:
 *   Each semester button calls navigateToReport(department, semester)
 *   which launches ExamReportActivity with extras.
 *
 * Usage (from HomeActivity / BottomNav):
 *   getSupportFragmentManager()
 *       .beginTransaction()
 *       .replace(R.id.fragmentContainer, new ExamFragment())
 *       .commit();
 */
public class ExamsFragment extends Fragment {

    // ── Constants passed as Intent extras to ExamReportActivity ──────────
    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_SEMESTER   = "extra_semester";

    // ── Department labels ─────────────────────────────────────────────────
    private static final String DEPT_CS      = "Computer Science";
    private static final String DEPT_MATH    = "Mathematics";
    private static final String DEPT_ENGLISH = "English";
    private static final String DEPT_PHYSICS = "Physics";

    // ─────────────────────────────────────────────────────────────────────
    public static ExamsFragment newInstance() {
        return new ExamsFragment();
    }

    // ─────────────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exams, container, false);
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        bindComputerScienceButtons(root);
        bindMathematicsButtons(root);
        bindEnglishButton(root);
        bindPhysicsButtons(root);
        bindCtaButton(root);
    }

    // ── Computer Science — 8 semester buttons ────────────────────────────
    private void bindComputerScienceButtons(View root) {
        int[] semesterButtonIds = {
                R.id.btnCsSem1, R.id.btnCsSem2, R.id.btnCsSem3, R.id.btnCsSem4,
                R.id.btnCsSem5, R.id.btnCsSem6, R.id.btnCsSem7, R.id.btnCsSem8
        };

        for (int i = 0; i < semesterButtonIds.length; i++) {
            final int semesterNumber = i + 1;
            Button btn = root.findViewById(semesterButtonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v ->
                        navigateToReport(DEPT_CS, semesterNumber));
            }
        }
    }

    // ── Mathematics — S1-S8 buttons inside expandable section ────────────
    private void bindMathematicsButtons(View root) {
        int[] mathButtonIds = {
                R.id.btnMathS1, R.id.btnMathS2, R.id.btnMathS3, R.id.btnMathS4,
                R.id.btnMathS5, R.id.btnMathS6, R.id.btnMathS7, R.id.btnMathS8
        };

        for (int i = 0; i < mathButtonIds.length; i++) {
            final int semesterNumber = i + 1;
            Button btn = root.findViewById(mathButtonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v ->
                        navigateToReport(DEPT_MATH, semesterNumber));
            }
        }

        // Toggle expand/collapse for the math semester panel
        View mathHeader = root.findViewById(R.id.mathSemesterHeader);
        View mathGrid   = root.findViewById(R.id.mathSemesterGrid);
        View expandIcon = root.findViewById(R.id.ivMathExpand);

        if (mathHeader != null && mathGrid != null) {
            mathHeader.setOnClickListener(v -> {
                boolean isVisible = mathGrid.getVisibility() == View.VISIBLE;
                mathGrid.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                if (expandIcon != null) {
                    expandIcon.setRotation(isVisible ? 0f : 180f);
                }
            });
        }
    }

    // ── English — single "View Semester Modules" button ──────────────────
    private void bindEnglishButton(View root) {
        View btn = root.findViewById(R.id.btnEnglishModules);
        if (btn != null) {
            // Default to semester 1 for English; host activity can pass a picker dialog
            btn.setOnClickListener(v -> navigateToReport(DEPT_ENGLISH, 1));
        }
    }

    // ── Physics — SEM I – SEM IV horizontal tabs ─────────────────────────
    private void bindPhysicsButtons(View root) {
        int[] physicsButtonIds = {
                R.id.btnPhySem1, R.id.btnPhySem2, R.id.btnPhySem3, R.id.btnPhySem4
        };

        for (int i = 0; i < physicsButtonIds.length; i++) {
            final int semesterNumber = i + 1;
            Button btn = root.findViewById(physicsButtonIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v ->
                        navigateToReport(DEPT_PHYSICS, semesterNumber));
            }
        }
    }

    // ── CTA banner button ─────────────────────────────────────────────────
    private void bindCtaButton(View root) {
        View btnFileApplication = root.findViewById(R.id.btnFileApplication);
        if (btnFileApplication != null) {
            btnFileApplication.setOnClickListener(v -> {
                // TODO: Navigate to ApplicationFormActivity or show a bottom sheet
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Launch ExamReportActivity for the selected department + semester.
     *
     * @param department Human-readable department name (e.g. "Computer Science")
     * @param semester   1-based semester number
     */
    private void navigateToReport(String department, int semester) {
        if (getActivity() == null) return;

        Intent intent = new Intent(getActivity(), ExamsReportActivity.class);
        intent.putExtra(EXTRA_DEPARTMENT, department);
        intent.putExtra(EXTRA_SEMESTER, semester);
        startActivity(intent);
        getActivity().overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }
}