package com.mbd.cmsteacher.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.mbd.cmsteacher.LoginActivity;
import com.mbd.cmsteacher.R;

/**
 * MenuFragment — Faculty Portal Hub
 * ─────────────────────────────────────────────────────────────────────────
 * Matches the HTML "Faculty Portal - CMS Teacher Hub" reference.
 *
 * Sections:
 *   § 1  Top app bar — rotated navy badge, "Faculty Portal", bell, avatar
 *   § 2  Administrative Actions — "Approve Student Requests" card
 *   § 3  Institutional Resources — 4 navigable rows
 *   § 4  Support & Account — Help & Support / Account Settings
 *   § 5  Sign Out + version footer
 *
 * Navigation stubs (marked TODO) are wired to Toasts for now.
 * Replace each Toast with the appropriate Intent or FragmentTransaction
 * once the target screens exist.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class MenuFragment extends Fragment {

    // ── Resource row data ─────────────────────────────────────────────────
    private static final int[] ROW_ICONS = {
            R.drawable.ic_badge,
            R.drawable.ic_campaign,
            R.drawable.ic_library_books,
            R.drawable.ic_menu_book
    };

    private static final int[] ROW_LABELS = {
            R.string.menu_faculty_directory,
            R.string.menu_departmental_news,
            R.string.menu_resource_library,
            R.string.menu_college_handbook
    };

    private static final int[] ROW_IDS = {
            R.id.row_faculty_directory,
            R.id.row_departmental_news,
            R.id.row_resource_library,
            R.id.row_college_handbook
    };

    // ── Firebase ──────────────────────────────────────────────────────────
    private FirebaseAuth mAuth;

    // ─────────────────────────────────────────────────────────────────────
    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    // ─────────────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setupResourceRows(view);
        setupAdminCard(view);
        setupSupportRows(view);
        setupSignOut(view);
        setupNotificationBell(view);
    }

    // ── § 2  Administrative action card ──────────────────────────────────

    private void setupAdminCard(View root) {
        View card = root.findViewById(R.id.card_approve_requests);
        MaterialButton btnReview = root.findViewById(R.id.btn_review_now);

        View.OnClickListener openRequests = v -> {
            // TODO: navigate to StudentRequestsActivity / ApprovalFragment
            Toast.makeText(requireContext(),
                    "Opening pending student requests…", Toast.LENGTH_SHORT).show();
        };

        card.setOnClickListener(openRequests);
        btnReview.setOnClickListener(openRequests);
    }

    // ── § 3  Institutional resource rows ─────────────────────────────────

    /**
     * Binds icon + label for each included resource row and attaches click listeners.
     * Because <include> doesn't support per-instance child attribute overrides,
     * we resolve each root LinearLayout, then find the child views by id.
     */
    private void setupResourceRows(View root) {
        for (int i = 0; i < ROW_IDS.length; i++) {
            LinearLayout row = root.findViewById(ROW_IDS[i]);
            if (row == null) continue;

            // Set icon & label
            ImageView icon = row.findViewById(R.id.iv_resource_icon);
            TextView  label = row.findViewById(R.id.tv_resource_label);

            if (icon  != null) icon.setImageResource(ROW_ICONS[i]);
            if (label != null) label.setText(ROW_LABELS[i]);

            // Attach navigation stub
            final int index = i;
            row.setOnClickListener(v -> onResourceRowClicked(index));
        }
    }

    private void onResourceRowClicked(int index) {
        switch (index) {
            case 0:
                // TODO: open FacultyDirectoryActivity
                Toast.makeText(requireContext(), "Faculty Directory", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                // TODO: open DepartmentalNewsActivity / WebViewActivity
                Toast.makeText(requireContext(), "Departmental News", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                // TODO: open ResourceLibraryActivity
                Toast.makeText(requireContext(), "Resource Library", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                // TODO: open CollegeHandbookActivity / PDF viewer
                Toast.makeText(requireContext(), "College Handbook", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // ── § 4  Support & Account rows ───────────────────────────────────────

    private void setupSupportRows(View root) {
        LinearLayout rowHelp     = root.findViewById(R.id.row_help_support);
        LinearLayout rowSettings = root.findViewById(R.id.row_account_settings);

        if (rowHelp != null) {
            rowHelp.setOnClickListener(v -> {
                // TODO: open HelpActivity or launch support URL in browser
                Toast.makeText(requireContext(), "Help & Support", Toast.LENGTH_SHORT).show();
            });
        }

        if (rowSettings != null) {
            rowSettings.setOnClickListener(v -> {
                // TODO: open AccountSettingsActivity
                Toast.makeText(requireContext(), "Account Settings", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ── § 5  Sign Out ─────────────────────────────────────────────────────

    private void setupSignOut(View root) {
        MaterialButton btnSignOut = root.findViewById(R.id.btn_sign_out);
        if (btnSignOut == null) return;

        btnSignOut.setOnClickListener(v -> confirmSignOut());
    }

    /**
     * Shows a confirmation dialog before signing the teacher out,
     * to prevent accidental tap-outs.
     */
    private void confirmSignOut() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of the Faculty Portal?")
                .setPositiveButton("Sign Out", (dialog, which) -> performSignOut())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSignOut() {
        mAuth.signOut();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    // ── § 1  Notification bell ────────────────────────────────────────────

    private void setupNotificationBell(View root) {
        View bell = root.findViewById(R.id.btn_notifications);
        if (bell != null) {
            bell.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show());
        }
    }
}