package com.mbd.cmsadmin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.activities.TimeTableManagementActivity;

/**
 * OthersFragment — Administrative Utilities Hub
 *
 * Displays the admin control grid with 10 utility tiles:
 *   1.  Approve Student Requests
 *   2.  Manage Admins
 *   3.  Manage Teachers
 *   4.  Manage Time Table        → launches TimeTableManagementActivity
 *   5.  Manage Notifications
 *   6.  Add Downloads
 *   7.  Manage Departments
 *   8.  Audit Logs
 *   9.  Leave Management
 *   10. System Settings
 *
 * Layout: fragment_others.xml
 *
 * Navigation pattern mirrors the HTML design:
 * each tile is a full-width or grid card; "Manage Time Table" routes
 * to the dedicated TimeTableManagementActivity.
 */
public class OthersFragment extends Fragment {

    // ── Views ─────────────────────────────────────────────────────────────────
    private LinearLayout tileApproveRequests;
    private LinearLayout tileManageAdmins;
    private LinearLayout tileManageTeachers;
    private LinearLayout tileManageTimetable;
    private LinearLayout tileManageNotifications;
    private LinearLayout tileAddDownloads;
    private LinearLayout tileManageDepartments;
    private LinearLayout tileAuditLogs;
    private LinearLayout tileLeaveManagement;
    private LinearLayout tileSystemSettings;

    // ── Factory ───────────────────────────────────────────────────────────────
    public static OthersFragment newInstance() {
        return new OthersFragment();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_others, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setListeners();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void bindViews(View root) {
        tileApproveRequests      = root.findViewById(R.id.tile_approve_requests);
        tileManageAdmins         = root.findViewById(R.id.tile_manage_admins);
        tileManageTeachers       = root.findViewById(R.id.tile_manage_teachers);
        tileManageTimetable      = root.findViewById(R.id.tile_manage_timetable);
        tileManageNotifications  = root.findViewById(R.id.tile_manage_notifications);
        tileAddDownloads         = root.findViewById(R.id.tile_add_downloads);
        tileManageDepartments    = root.findViewById(R.id.tile_manage_departments);
        tileAuditLogs            = root.findViewById(R.id.tile_audit_logs);
        tileLeaveManagement      = root.findViewById(R.id.tile_leave_management);
        tileSystemSettings       = root.findViewById(R.id.tile_system_settings);
    }

    private void setListeners() {

        tileApproveRequests.setOnClickListener(v ->
                showToast("Approve Student Requests — coming soon"));

        tileManageAdmins.setOnClickListener(v ->
                showToast("Manage Admins — coming soon"));

        tileManageTeachers.setOnClickListener(v ->
                showToast("Manage Teachers — coming soon"));

        // ── Manage Time Table → dedicated activity ────────────────────────
        tileManageTimetable.setOnClickListener(v -> {
            if (getActivity() == null) return;
            Intent intent = new Intent(getActivity(), TimeTableManagementActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
        });

        tileManageNotifications.setOnClickListener(v ->
                showToast("Manage Notifications — coming soon"));

        tileAddDownloads.setOnClickListener(v ->
                showToast("Add Downloads — coming soon"));

        tileManageDepartments.setOnClickListener(v ->
                showToast("Manage Departments — coming soon"));

        tileAuditLogs.setOnClickListener(v ->
                showToast("Audit Logs — coming soon"));

        tileLeaveManagement.setOnClickListener(v ->
                showToast("Leave Management — coming soon"));

        tileSystemSettings.setOnClickListener(v ->
                showToast("System Settings — coming soon"));
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}