package com.mbd.cmsteacher.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.mbd.cmsteacher.R;

import java.util.Calendar;

/**
 * ScheduleFragment — Teacher Master Schedule Screen
 * ─────────────────────────────────────────────────────────────────────────
 * Matches the HTML "Teacher Schedule | Imperial Chancery" reference exactly.
 *
 * Sections:
 *   § 1  Header: "Faculty of Law" eyebrow + "Master Schedule" display headline
 *   § 2  Week date picker — horizontal 6-day strip, Monday highlighted
 *   § 3  Semester + Session spinners
 *   § 4  Departmental Timetable — horizontal-scrolling table card
 *   § 5  Timeline — ordered events (Lecture → Faculty Meeting → Seminar → Evening Reception)
 *   § 6  Preparation Notes — 2-column note cards
 *
 * All data is static fake data.
 * Replace loadFake*() methods with Firestore queries when backend is ready.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class ScheduleFragment extends Fragment {

    // ── Day-of-week constants ─────────────────────────────────────────────
    private static final int DAY_MON = 0;
    private static final int DAY_TUE = 1;
    private static final int DAY_WED = 2;
    private static final int DAY_THU = 3;
    private static final int DAY_FRI = 4;
    private static final int DAY_SAT = 5;

    // ── Views ─────────────────────────────────────────────────────────────
    // Date navigation
    private TextView    tvCurrentDate;
    private TextView    tvCurrentTerm;
    private MaterialButton btnPrevWeek;
    private MaterialButton btnNextWeek;

    // Day picker strip
    private View[] dayCards;
    private TextView[] tvDayNames;
    private TextView[] tvDayNumbers;

    // Spinners
    private Spinner spinnerSemester;
    private Spinner spinnerSession;

    // Timeline event cards
    private View cardLecture;
    private View cardFacultyMeeting;
    private View cardSeminar;
    private View cardEvening;

    // Note cards
    private TextView tvNoteJurisprudence;
    private TextView tvNoteFacultyCouncil;

    // ── State ─────────────────────────────────────────────────────────────
    private int selectedDay = DAY_MON;   // Monday selected by default

    // ─────────────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupWeekNavigation();
        setupDayPicker();
        setupSpinners();
        setupTimelineListeners();
        loadFakeSchedule();
    }

    // ── View binding ──────────────────────────────────────────────────────

    private void bindViews(View root) {
        tvCurrentDate    = root.findViewById(R.id.tv_current_date);
        tvCurrentTerm    = root.findViewById(R.id.tv_current_term);
        btnPrevWeek      = root.findViewById(R.id.btn_prev_week);
        btnNextWeek      = root.findViewById(R.id.btn_next_week);

        spinnerSemester  = root.findViewById(R.id.spinner_semester);
        spinnerSession   = root.findViewById(R.id.spinner_session);

        // Day picker — 6 cards (Mon–Sat)
        dayCards = new View[] {
                root.findViewById(R.id.day_card_mon),
                root.findViewById(R.id.day_card_tue),
                root.findViewById(R.id.day_card_wed),
                root.findViewById(R.id.day_card_thu),
                root.findViewById(R.id.day_card_fri),
                root.findViewById(R.id.day_card_sat),
        };
        tvDayNames = new TextView[] {
                root.findViewById(R.id.tv_day_name_mon),
                root.findViewById(R.id.tv_day_name_tue),
                root.findViewById(R.id.tv_day_name_wed),
                root.findViewById(R.id.tv_day_name_thu),
                root.findViewById(R.id.tv_day_name_fri),
                root.findViewById(R.id.tv_day_name_sat),
        };
        tvDayNumbers = new TextView[] {
                root.findViewById(R.id.tv_day_num_mon),
                root.findViewById(R.id.tv_day_num_tue),
                root.findViewById(R.id.tv_day_num_wed),
                root.findViewById(R.id.tv_day_num_thu),
                root.findViewById(R.id.tv_day_num_fri),
                root.findViewById(R.id.tv_day_num_sat),
        };

        // Timeline cards
        cardLecture       = root.findViewById(R.id.card_lecture);
        cardFacultyMeeting = root.findViewById(R.id.card_faculty_meeting);
        cardSeminar       = root.findViewById(R.id.card_seminar);
        cardEvening       = root.findViewById(R.id.card_evening);

        // Notes
        tvNoteJurisprudence  = root.findViewById(R.id.tv_note_jurisprudence);
        tvNoteFacultyCouncil = root.findViewById(R.id.tv_note_faculty_council);
    }

    // ── Week navigation ───────────────────────────────────────────────────

    private void setupWeekNavigation() {
        // ── FAKE DATA ── replace with real week offset logic
        tvCurrentDate.setText("October 24, 2023");
        tvCurrentTerm.setText("MICHAELMAS TERM · WEEK 4");

        btnPrevWeek.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Previous week", Toast.LENGTH_SHORT).show());

        btnNextWeek.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Next week", Toast.LENGTH_SHORT).show());
    }

    // ── Day picker ────────────────────────────────────────────────────────

    private void setupDayPicker() {
        applyDaySelection(DAY_MON);

        for (int i = 0; i < dayCards.length; i++) {
            final int index = i;
            dayCards[i].setOnClickListener(v -> applyDaySelection(index));
        }
    }

    /**
     * Highlights the selected day card with a gold bottom border and navy text;
     * all others revert to the default unselected appearance.
     */
    private void applyDaySelection(int dayIndex) {
        selectedDay = dayIndex;

        for (int i = 0; i < dayCards.length; i++) {
            View card        = dayCards[i];
            TextView tvName  = tvDayNames[i];
            TextView tvNum   = tvDayNumbers[i];

            if (i == dayIndex) {
                // Active: gold underline border, slightly elevated background
                card.setBackgroundResource(R.drawable.bg_day_card_selected);
                tvName.setTextColor(Color.parseColor("#5D4200"));
                tvNum.setTextColor(Color.parseColor("#000A1E"));
            } else {
                // Inactive: transparent background
                card.setBackgroundResource(R.drawable.bg_day_card_default);
                tvName.setTextColor(Color.parseColor("#44474E"));
                // Saturday dimmed
                int alpha = (i == DAY_SAT) ? 0x80 : 0xFF;
                tvName.setAlpha(i == DAY_SAT ? 0.5f : 1f);
                tvNum.setAlpha(i == DAY_SAT ? 0.5f : 1f);
                tvNum.setTextColor(Color.parseColor("#000A1E"));
            }
        }

        // ── FAKE DATA ── in prod, reload timetable rows for selected day
        // loadTimetableForDay(dayIndex);
    }

    // ── Spinners ──────────────────────────────────────────────────────────

    private void setupSpinners() {
        // Semester
        String[] semesters = {
                "Michaelmas Term 2023",
                "Hilary Term 2024",
                "Trinity Term 2024"
        };
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, semesters);
        semAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semAdapter);

        // Session
        String[] sessions = { "Morning Session", "Evening Session" };
        ArrayAdapter<String> sessAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, sessions);
        sessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSession.setAdapter(sessAdapter);

        // ── FAKE DATA ── In prod, re-query timetable on selection change
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {}
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ── Timeline cards ────────────────────────────────────────────────────

    private void setupTimelineListeners() {
        if (cardLecture != null) {
            cardLecture.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Advanced Jurisprudence", Toast.LENGTH_SHORT).show());
        }
        if (cardSeminar != null) {
            cardSeminar.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "International Human Rights", Toast.LENGTH_SHORT).show());
        }
        if (cardEvening != null) {
            cardEvening.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Evening Reception (Optional)", Toast.LENGTH_SHORT).show());
        }
        // Faculty meeting card has no navigation target; it's informational only
    }

    // ── FAKE DATA ─────────────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Populates the preparation notes.
     * Replace with: Firestore "notes" collection filtered by teacher UID + date.
     */
    private void loadFakeSchedule() {
        if (tvNoteJurisprudence  != null) {
            tvNoteJurisprudence.setText("Review Case Study #402 for Jurisprudence lecture.");
        }
        if (tvNoteFacultyCouncil != null) {
            tvNoteFacultyCouncil.setText("Draft Faculty Council proposal on digital archives.");
        }
    }
}