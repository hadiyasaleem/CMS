package com.mbd.cmsadmin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.adapters.AlertsAdapter;
import com.mbd.cmsadmin.models.AlertItem;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment
 * ─────────────────────────────────────────────────────────────────────────────
 * Displays the Admin Dashboard:
 *   § 1  Welcome header
 *   § 2  Stats bento cards (total students / active faculty / pending requests)
 *   § 3  Recent Academic Alerts  (RecyclerView → AlertsAdapter)
 *   § 4  Command Center action tiles
 *   § 5  Academic Integrity Notice banner
 *
 * All list data is currently hard-coded (fake).
 * Search for "── FAKE DATA ──" and replace with Firestore calls when ready.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class HomeFragment extends Fragment {

    // ── Views ─────────────────────────────────────────────────────────────────
    private RecyclerView    rvAlerts;
    private AlertsAdapter   alertsAdapter;

    private LinearLayout    actionAddStudent;
    private LinearLayout    actionAttendanceReport;
    private LinearLayout    actionManageExams;
    private LinearLayout    actionSystemUtilities;

    // ── Factory ───────────────────────────────────────────────────────────────
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

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
        setupAlertsRecyclerView();
        setupCommandCenterListeners();

        // ── FAKE DATA ── replace with Firestore calls below ────────────────
        loadFakeAlerts();
        // ───────────────────────────────────────────────────────────────────
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void bindViews(View root) {
        rvAlerts               = root.findViewById(R.id.rv_alerts);
        actionAddStudent       = root.findViewById(R.id.action_add_student);
        actionAttendanceReport = root.findViewById(R.id.action_attendance_report);
        actionManageExams      = root.findViewById(R.id.action_manage_exams);
        actionSystemUtilities  = root.findViewById(R.id.action_system_utilities);
    }

    private void setupAlertsRecyclerView() {
        alertsAdapter = new AlertsAdapter(new ArrayList<>());
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAlerts.setAdapter(alertsAdapter);
        rvAlerts.setNestedScrollingEnabled(false);  // parent ScrollView handles scroll

        alertsAdapter.setOnAlertClickListener((item, position) ->
                // TODO: open alert detail
                Toast.makeText(requireContext(), item.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * ── FAKE DATA ──
     * Replace this method body with a Firestore query, e.g.:
     *
     *   FirebaseFirestore.getInstance()
     *       .collection("alerts")
     *       .orderBy("timestamp", Query.Direction.DESCENDING)
     *       .limit(5)
     *       .get()
     *       .addOnSuccessListener(snapshot -> {
     *           List<AlertItem> items = new ArrayList<>();
     *           for (DocumentSnapshot doc : snapshot.getDocuments()) {
     *               items.add(new AlertItem(
     *                       doc.getString("title"),
     *                       doc.getString("body"),
     *                       doc.getString("meta")));
     *           }
     *           alertsAdapter.updateAlerts(items);
     *       });
     */
    private void loadFakeAlerts() {
        List<AlertItem> fake = new ArrayList<>();

        fake.add(new AlertItem(
                "Semester Exams Schedule Published",
                "The comprehensive schedule for Spring 2024 finals has been released to all "
                        + "departmental heads and portal mirrors.",
                "POSTED 2 HOURS AGO  •  REGISTRAR OFFICE"
        ));
        fake.add(new AlertItem(
                "New Faculty Recruitment Drive",
                "Applications are now open for senior lecturer positions in the Department of "
                        + "Classical Literature and Social Sciences.",
                "POSTED YESTERDAY  •  HR DEPARTMENT"
        ));
        fake.add(new AlertItem(
                "Annual Budget Review Meeting",
                "Scheduled for Friday, 10:00 AM in the Board Room. Attendance is mandatory for "
                        + "all executive administrators.",
                "POSTED 2 DAYS AGO  •  FINANCE WING"
        ));

        alertsAdapter.updateAlerts(fake);
    }

    private void setupCommandCenterListeners() {
        actionAddStudent.setOnClickListener(v ->
                // TODO: navigate to AddStudentFragment / Activity
                Toast.makeText(requireContext(), "Add New Student", Toast.LENGTH_SHORT).show());

        actionAttendanceReport.setOnClickListener(v ->
                // TODO: navigate to AttendanceReportFragment
                Toast.makeText(requireContext(), "View Attendance Report", Toast.LENGTH_SHORT).show());

        actionManageExams.setOnClickListener(v ->
                // TODO: navigate to ManageExamsFragment
                Toast.makeText(requireContext(), "Manage Exams", Toast.LENGTH_SHORT).show());

        actionSystemUtilities.setOnClickListener(v ->
                // TODO: navigate to SystemUtilitiesFragment
                Toast.makeText(requireContext(), "System Utilities", Toast.LENGTH_SHORT).show());
    }
}