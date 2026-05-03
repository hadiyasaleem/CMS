package com.mbd.cmsadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mbd.cmsadmin.R;
import com.mbd.cmsadmin.fragments.ExamsFragment;

/**
 * ExamReportActivity — Exams Status Report (Screen 2)
 *
 * Displays the full ledger of subjects with their mid-term exam and
 * sessional-marks submission statuses for a given department + semester.
 *
 * Receives via Intent extras:
 *   - {@link ExamFragment#EXTRA_DEPARTMENT} — e.g. "Computer Science"
 *   - {@link ExamFragment#EXTRA_SEMESTER}   — e.g. 6
 *
 * Layout: activity_exam_report.xml
 *
 * Sections:
 *   1. Top bar (back arrow + brand title)
 *   2. Hero header with department chip, semester chip, active session badge
 *   3. Left aside — Submission summary card + Important dates
 *   4. Right section — Subject ledger cards (4 subjects shown for CS Sem 6)
 *   5. Bottom CTA — "Missing an entry? → Inquiry Portal"
 *
 * In a real implementation, the subject list would be fetched from Firestore
 * via a repository/ViewModel. For now it is static demo data matching the
 * HTML design exactly (CS601–CS604).
 */
public class ExamsReportActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────
    private TextView tvDepartmentChip;
    private TextView tvSemesterChip;
    private TextView tvTotalSubjects;
    private TextView tvSubmitted;
    private TextView tvPending;

    // ── Data received from ExamFragment ──────────────────────────────────
    private String department;
    private int    semester;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams_report);

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.examReportRoot), (v, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        // Read extras
        Intent intent = getIntent();
        department = intent.getStringExtra(ExamsFragment.EXTRA_DEPARTMENT);
        semester   = intent.getIntExtra(ExamsFragment.EXTRA_SEMESTER, 1);

        if (department == null) department = "Computer Science";

        bindViews();
        populateHeader();
        setListeners();
    }

    // ─────────────────────────────────────────────────────────────────────
    private void bindViews() {
        tvDepartmentChip = findViewById(R.id.tvDepartmentChip);
        tvSemesterChip   = findViewById(R.id.tvSemesterChip);
        tvTotalSubjects  = findViewById(R.id.tvTotalSubjects);
        tvSubmitted      = findViewById(R.id.tvExamsSubmitted);
        tvPending        = findViewById(R.id.tvPendingSubmissions);
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Fill the hero chips and summary card from the intent data.
     * In production these numbers come from Firestore.
     */
    private void populateHeader() {
        if (tvDepartmentChip != null) {
            tvDepartmentChip.setText(department.toUpperCase());
        }
        if (tvSemesterChip != null) {
            tvSemesterChip.setText("SEMESTER " + semester);
        }

        // Static summary (would be dynamic in production)
        if (tvTotalSubjects != null)  tvTotalSubjects.setText("06");
        if (tvSubmitted     != null)  tvSubmitted.setText("04");
        if (tvPending       != null)  tvPending.setText("02");
    }

    // ─────────────────────────────────────────────────────────────────────
    private void setListeners() {
        // Back / up navigation
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Inquiry portal CTA
        View btnInquiry = findViewById(R.id.btnInquiryPortal);
        if (btnInquiry != null) {
            btnInquiry.setOnClickListener(v -> {
                // TODO: navigate to InquiryPortalActivity or open a form
                Toast.makeText(this,
                        "Inquiry portal coming soon.",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // Download buttons (CS601)
        View dl601 = findViewById(R.id.btnDownload601);
        if (dl601 != null) {
            dl601.setOnClickListener(v -> onDownloadClicked("CS601"));
        }

        // Download buttons (CS602)
        View dl602 = findViewById(R.id.btnDownload602);
        if (dl602 != null) {
            dl602.setOnClickListener(v -> onDownloadClicked("CS602"));
        }

        // Download buttons (CS604)
        View dl604 = findViewById(R.id.btnDownload604);
        if (dl604 != null) {
            dl604.setOnClickListener(v -> onDownloadClicked("CS604"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    /**
     * Handle download button tap for a subject paper.
     * Replace with real Firestore Storage download in production.
     */
    private void onDownloadClicked(String subjectCode) {
        Toast.makeText(this,
                "Downloading " + subjectCode + " mid-term paper…",
                Toast.LENGTH_SHORT).show();
    }

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }
}