package com.mbd.cmsteacher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.mbd.cmsteacher.R;

/**
 * ExamsFragment — Exam Management / Mark Entry Selection Screen
 * ─────────────────────────────────────────────────────────────────────────
 * Matches the HTML "Exams: Selection" reference exactly.
 *
 * Sections:
 *   § 1  Header: "Exam Management" + gold rule + "Mark Entry Parameters" eyebrow
 *   § 2  Academic Year spinner
 *   § 3  Semester spinner
 *   § 4  Department row (static — CS&E selected)
 *   § 5  Assessment type toggle: Midterm | Sessional
 *   § 6  "Proceed to Subject List" button
 *
 * Radio indicators are ImageViews swapping between
 * ic_radio_checked and ic_radio_unchecked drawables.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class ExamsFragment extends Fragment {

    // ── Assessment type constants ─────────────────────────────────────────
    private static final String TYPE_MIDTERM   = "MIDTERM";
    private static final String TYPE_SESSIONAL = "SESSIONAL";

    // ── Views ─────────────────────────────────────────────────────────────
    private Spinner          spinnerYear;
    private Spinner          spinnerSemester;

    private MaterialCardView cardMidterm;
    private MaterialCardView cardSessional;

    private ImageView        ivMidtermRadio;
    private ImageView        ivSessionalRadio;

    private MaterialButton   btnProceed;

    // ── State ─────────────────────────────────────────────────────────────
    private String selectedAssessment = TYPE_MIDTERM;   // Midterm selected by default

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupSpinners();
        setupAssessmentCards();
        setupProceedButton();

        // Apply default selection (Midterm)
        applySelection(TYPE_MIDTERM);
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews(View root) {
        spinnerYear      = root.findViewById(R.id.spinner_year);
        spinnerSemester  = root.findViewById(R.id.spinner_semester);
        cardMidterm      = root.findViewById(R.id.card_midterm);
        cardSessional    = root.findViewById(R.id.card_sessional);
        ivMidtermRadio   = root.findViewById(R.id.iv_midterm_radio);
        ivSessionalRadio = root.findViewById(R.id.iv_sessional_radio);
        btnProceed       = root.findViewById(R.id.btn_proceed);
    }

    // ── Spinners ──────────────────────────────────────────────────────────

    private void setupSpinners() {
        // Academic Year
        String[] years = {"2023 - 2024", "2022 - 2023", "2021 - 2022"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Semester
        String[] semesters = {"Odd Semester (I)", "Even Semester (II)", "Summer Session"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);
    }

    // ── Assessment card toggle ────────────────────────────────────────────

    private void setupAssessmentCards() {
        cardMidterm.setOnClickListener(v -> applySelection(TYPE_MIDTERM));
        cardSessional.setOnClickListener(v -> applySelection(TYPE_SESSIONAL));
    }

    /**
     * Visually selects one assessment card and deselects the other.
     * Swaps the radio ImageView drawable, card stroke, elevation,
     * and background — mirroring the JS selectAssessment() in the HTML reference.
     */
    private void applySelection(String type) {
        selectedAssessment = type;

        if (type.equals(TYPE_MIDTERM)) {
            // ── Midterm: active ───────────────────────────────────────
            cardMidterm.setCardElevation(dpToPx(8));
            cardMidterm.setCardBackgroundColor(
                    getResources().getColor(R.color.surface_container_lowest, null));
            cardMidterm.setStrokeColor(
                    getResources().getColor(R.color.primary, null));
            cardMidterm.setStrokeWidth(dpToPx(2));
            ivMidtermRadio.setImageResource(R.drawable.ic_radio_checked);

            // ── Sessional: inactive ───────────────────────────────────
            cardSessional.setCardElevation(dpToPx(1));
            cardSessional.setCardBackgroundColor(
                    getResources().getColor(R.color.surface_container_low, null));
            cardSessional.setStrokeColor(
                    getResources().getColor(R.color.outline_variant, null));
            cardSessional.setStrokeWidth(dpToPx(1));
            ivSessionalRadio.setImageResource(R.drawable.ic_radio_unchecked);

        } else {
            // ── Sessional: active ─────────────────────────────────────
            cardSessional.setCardElevation(dpToPx(8));
            cardSessional.setCardBackgroundColor(
                    getResources().getColor(R.color.surface_container_lowest, null));
            cardSessional.setStrokeColor(
                    getResources().getColor(R.color.primary, null));
            cardSessional.setStrokeWidth(dpToPx(2));
            ivSessionalRadio.setImageResource(R.drawable.ic_radio_checked);

            // ── Midterm: inactive ─────────────────────────────────────
            cardMidterm.setCardElevation(dpToPx(1));
            cardMidterm.setCardBackgroundColor(
                    getResources().getColor(R.color.surface_container_low, null));
            cardMidterm.setStrokeColor(
                    getResources().getColor(R.color.outline_variant, null));
            cardMidterm.setStrokeWidth(dpToPx(1));
            ivMidtermRadio.setImageResource(R.drawable.ic_radio_unchecked);
        }
    }

    // ── Proceed button ────────────────────────────────────────────────────

    private void setupProceedButton() {
        btnProceed.setOnClickListener(v -> {
            String year     = spinnerYear.getSelectedItem().toString();
            String semester = spinnerSemester.getSelectedItem().toString();

            // TODO: navigate to SubjectListActivity / SubjectListFragment,
            //       passing year, semester, dept, and selectedAssessment as extras.
            String msg = selectedAssessment + " · " + year + " · " + semester;
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    // ── Utility ───────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}