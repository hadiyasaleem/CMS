package com.mbd.cmsteacher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsteacher.R;
import com.mbd.cmsteacher.adapters.ActionCardsAdapter;
import com.mbd.cmsteacher.adapters.DeadlinesAdapter;
import com.mbd.cmsteacher.models.ActionCardItem;
import com.mbd.cmsteacher.models.DeadlineItem;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment — Teacher Dashboard
 * ─────────────────────────────────────────────────────────────────────────
 * Sections (matching the HTML reference):
 *   § 1  Top app bar (avatar + title + notification bell)
 *   § 2  Hero greeting: "Good Morning, Dr. Sterling."
 *   § 3  Active Lecture card (current session with "Mark Attendance" CTA)
 *   § 4  Faculty Dashboard — 2-col grid (ActionCardsAdapter)
 *   § 5  Academic Deadlines — vertical list (DeadlinesAdapter)
 *
 * All data is static fake data.
 * Replace loadFakeActionCards() / loadFakeDeadlines() with Firestore
 * queries when the backend is ready.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class HomeFragment extends Fragment {

    // ── Views ─────────────────────────────────────────────────────────────
    private TextView            tvGreeting;
    private TextView            tvSessionLabel;
    private TextView            tvCurrentSubject;
    private TextView            tvCurrentLocation;
    private TextView            tvCurrentTime;
    private View                btnMarkAttendanceCta;

    private RecyclerView        rvActionCards;
    private RecyclerView        rvDeadlines;

    private ActionCardsAdapter  actionCardsAdapter;
    private DeadlinesAdapter    deadlinesAdapter;

    // ── Factory ───────────────────────────────────────────────────────────
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupActionCardsRecyclerView();
        setupDeadlinesRecyclerView();
        setListeners(view);

        // ── FAKE DATA ── replace with Firestore when ready ─────────────
        loadFakeGreeting();
        loadFakeCurrentSession();
        loadFakeActionCards();
        loadFakeDeadlines();
        // ───────────────────────────────────────────────────────────────
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void bindViews(View root) {
        tvGreeting          = root.findViewById(R.id.tv_greeting);
        tvSessionLabel      = root.findViewById(R.id.tv_session_label);
        tvCurrentSubject    = root.findViewById(R.id.tv_current_subject);
        tvCurrentLocation   = root.findViewById(R.id.tv_current_location);
        tvCurrentTime       = root.findViewById(R.id.tv_current_time);
        btnMarkAttendanceCta = root.findViewById(R.id.btn_mark_attendance_cta);
        rvActionCards       = root.findViewById(R.id.rv_action_cards);
        rvDeadlines         = root.findViewById(R.id.rv_deadlines);
    }

    private void setupActionCardsRecyclerView() {
        actionCardsAdapter = new ActionCardsAdapter(new ArrayList<>());

        // 2-column grid matching the HTML layout
        GridLayoutManager gridLayout =
                new GridLayoutManager(requireContext(), 2);
        rvActionCards.setLayoutManager(gridLayout);
        rvActionCards.setAdapter(actionCardsAdapter);
        rvActionCards.setNestedScrollingEnabled(false);

        actionCardsAdapter.setOnCardClickListener((item, position) -> {
            // TODO: navigate to the respective feature activity/fragment
            Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDeadlinesRecyclerView() {
        deadlinesAdapter = new DeadlinesAdapter(new ArrayList<>());
        rvDeadlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDeadlines.setAdapter(deadlinesAdapter);
        rvDeadlines.setNestedScrollingEnabled(false);
    }

    private void setListeners(View root) {
        if (btnMarkAttendanceCta != null) {
            btnMarkAttendanceCta.setOnClickListener(v ->
                    // TODO: navigate to AttendanceFragment or MarkAttendanceActivity
                    Toast.makeText(requireContext(),
                            "Mark Attendance", Toast.LENGTH_SHORT).show());
        }

        // Notification bell
        View btnNotifications = root.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    Toast.makeText(requireContext(),
                            "Notifications", Toast.LENGTH_SHORT).show());
        }
    }

    // ── FAKE DATA methods ─────────────────────────────────────────────────

    /**
     * ── FAKE DATA ──
     * Replace with: fetch the authenticated teacher's name from Firestore
     * and set tvGreeting.setText("Good Morning,\n" + teacher.getFullName() + ".");
     */
    private void loadFakeGreeting() {
        if (tvGreeting != null) {
            tvGreeting.setText("Good Morning,\nDr. Sterling.");
        }
    }

    /**
     * ── FAKE DATA ──
     * Replace with: query the current timetable slot for this teacher
     * from Firestore (TimeTableSlot model) and populate the active lecture card.
     */
    private void loadFakeCurrentSession() {
        if (tvCurrentSubject  != null) tvCurrentSubject.setText("Advanced Architecture");
        if (tvCurrentLocation != null) tvCurrentLocation.setText("Lecture Hall B");
        if (tvCurrentTime     != null) tvCurrentTime.setText("10:00 AM – 11:30 AM");
        if (tvSessionLabel    != null) tvSessionLabel.setText("CURRENT SESSION");
    }

    /**
     * ── FAKE DATA ──
     * Four faculty dashboard action cards matching the HTML reference.
     */
    private void loadFakeActionCards() {
        List<ActionCardItem> cards = new ArrayList<>();

        cards.add(new ActionCardItem(
                "Mark Attendance",
                "Update daily session logs",
                R.drawable.ic_attendance));

        cards.add(new ActionCardItem(
                "Manage Grades",
                "Post assessment marks",
                R.drawable.ic_grades));

        cards.add(new ActionCardItem(
                "View Schedule",
                "Weekly timetable & events",
                R.drawable.ic_calendar));

        cards.add(new ActionCardItem(
                "Student Registry",
                "Profiles & performance",
                R.drawable.ic_group));

        actionCardsAdapter = new ActionCardsAdapter(cards);
        actionCardsAdapter.setOnCardClickListener((item, position) ->
                Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show());

        rvActionCards.setAdapter(actionCardsAdapter);
    }

    /**
     * ── FAKE DATA ──
     * Three deadline items matching the HTML reference.
     * Replace with: query Firestore "deadlines" collection filtered by teacher UID.
     */
    private void loadFakeDeadlines() {
        List<DeadlineItem> deadlines = new ArrayList<>();

        deadlines.add(new DeadlineItem(
                "Submit Sessional Marks",
                "Batch 2024 • Architecture",
                "DUE IN 4 HOURS",
                DeadlineItem.URGENCY_HIGH,
                R.drawable.ic_priority_high));

        deadlines.add(new DeadlineItem(
                "Faculty Meeting",
                "Main Hall • 2:00 PM",
                "TODAY",
                DeadlineItem.URGENCY_MED,
                R.drawable.ic_group));

        deadlines.add(new DeadlineItem(
                "Upload Course Plan",
                "Urban Design Elective",
                "OCT 26",
                DeadlineItem.URGENCY_LOW,
                R.drawable.ic_exams));

        deadlinesAdapter.updateItems(deadlines);
    }
}