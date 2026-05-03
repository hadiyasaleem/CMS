package com.mbd.cmsadmin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.adapters.DepartmentsAdapter;
import com.mbd.cmsadmin.models.DepartmentItem;

import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceFragment
 * Screen 1 of the Attendance flow — Department Selection.
 *
 * Tapping a department expands its semester grid.
 * Tapping a semester tile → AttendanceReportActivity.
 *
 * ── FAKE DATA ── Replace loadFakeDepartments() with a Firestore query when ready.
 */
public class AttendanceFragment extends Fragment {

    private RecyclerView       rvDepartments;
    private DepartmentsAdapter departmentsAdapter;

    public static AttendanceFragment newInstance() { return new AttendanceFragment(); }

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

        rvDepartments = view.findViewById(R.id.rv_departments);

        // ── FAKE DATA ── swap with Firestore ───────────────────────────────
        List<DepartmentItem> depts = buildFakeDepartments();
        // ───────────────────────────────────────────────────────────────────

        departmentsAdapter = new DepartmentsAdapter(depts);
        rvDepartments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDepartments.setAdapter(departmentsAdapter);
        rvDepartments.setNestedScrollingEnabled(false);

        view.findViewById(R.id.btn_view_faculty_standards).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Faculty Standards", Toast.LENGTH_SHORT).show());
    }

    private List<DepartmentItem> buildFakeDepartments() {
        List<DepartmentItem> list = new ArrayList<>();
        list.add(new DepartmentItem("cs",   "Computer Science",    "Faculty of Engineering & Technology", R.drawable.ic_computer));
        list.add(new DepartmentItem("math", "Mathematics",         "Faculty of Pure Sciences",            R.drawable.ic_functions));
        list.add(new DepartmentItem("eng",  "English Literature",  "Faculty of Arts & Humanities",        R.drawable.ic_exams));
        list.add(new DepartmentItem("phy",  "Physics",             "Faculty of Pure Sciences",            R.drawable.ic_experiment));
        return list;
    }
}