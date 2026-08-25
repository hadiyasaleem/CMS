// Master assembler: requires each completed section module in template order
// and packs them into the final report docx. Re-run after adding each new
// section file. Sections not yet written are simply not required yet.
const { Document, Packer, fs } = require("./generate_report.js");

const sections = [];

// --- Front matter (task #1) ---
sections.push(require("./sections/s01_frontmatter.js").content);
// --- Prelims (tasks #2-4) ---
sections.push(require("./sections/s02_execsummary.js").content);
sections.push(require("./sections/s03_acknowledgement.js").content);
sections.push(require("./sections/s04_abbreviations.js").content);
// --- TOC / List of Figures / List of Tables (final pass, task #31) ---
sections.push(require("./sections/s04b_toc_lof_lot.js").content);
// --- Chapter 1 (complete) ---
sections.push(require("./sections/s05_ch1_intro_and_1_1.js").content);
sections.push(require("./sections/s06_1_2_problem_solution.js").content);
sections.push(require("./sections/s07_1_3_objectives.js").content);
sections.push(require("./sections/s08_1_4_scope.js").content);
sections.push(require("./sections/s09_1_5_admin.js").content);
sections.push(require("./sections/s10_1_5_teacher.js").content);
sections.push(require("./sections/s11_1_5_student.js").content);
sections.push(require("./sections/s12_1_5_shared.js").content);
sections.push(require("./sections/s13_1_6_related_systems.js").content);
sections.push(require("./sections/s14_1_7_vision.js").content);
sections.push(require("./sections/s15_1_8_limitations.js").content);
sections.push(require("./sections/s16_1_9_tools.js").content);
sections.push(require("./sections/s17_1_10_deliverables.js").content);
sections.push(require("./sections/s18_1_11_gantt.js").content);
sections.push(require("./sections/s19_1_12_summary.js").content);
// --- Chapter 2 (complete) ---
sections.push(require("./sections/s20_ch2_intro_and_2_1.js").content);
sections.push(require("./sections/s21_2_2_requirement_technique.js").content);
sections.push(require("./sections/s22_2_3_intro_and_fr1.js").content);
sections.push(require("./sections/s23_fr2_sessions.js").content);
sections.push(require("./sections/s24_fr3_curriculum.js").content);
sections.push(require("./sections/s25_fr4_timetable.js").content);
sections.push(require("./sections/s26_fr5_attendance.js").content);
sections.push(require("./sections/s27_fr6_enter_marks.js").content);
sections.push(require("./sections/s28_fr7_request_mark_edit.js").content);
sections.push(require("./sections/s29_fr8_approve_mark_edit.js").content);
sections.push(require("./sections/s30_fr9_semester_result.js").content);
sections.push(require("./sections/s31_fr10_session_fees.js").content);
sections.push(require("./sections/s32_fr11_view_fee_challan.js").content);
sections.push(require("./sections/s33_fr12_issue_fine.js").content);
sections.push(require("./sections/s34_fr13_view_fines.js").content);
sections.push(require("./sections/s35_fr14_calendar.js").content);
sections.push(require("./sections/s36_fr15_datesheet_build.js").content);
sections.push(require("./sections/s37_fr16_datesheet_view.js").content);
sections.push(require("./sections/s38_fr17_document_upload.js").content);
sections.push(require("./sections/s39_fr18_document_view.js").content);
sections.push(require("./sections/s40_fr19_submit_link_request.js").content);
sections.push(require("./sections/s41_fr20_approve_link_request.js").content);
sections.push(require("./sections/s42_fr21_teacher_status.js").content);
sections.push(require("./sections/s43_fr22_insights.js").content);
sections.push(require("./sections/s44_2_4_1_reliability.js").content);
sections.push(require("./sections/s45_2_4_2_usability.js").content);
sections.push(require("./sections/s46_2_4_3_performance.js").content);
sections.push(require("./sections/s47_2_4_4_security.js").content);
sections.push(require("./sections/s48_2_5_1_ui_interfaces.js").content);
sections.push(require("./sections/s49_2_5_2_software_interfaces.js").content);
sections.push(require("./sections/s50_2_5_3_hardware_interfaces.js").content);
sections.push(require("./sections/s51_2_5_4_comms_interfaces.js").content);
sections.push(require("./sections/s52_2_6_summary.js").content);
// --- Chapter 3 (in progress) ---
sections.push(require("./sections/s53_ch3_intro_and_3_1.js").content);
sections.push(require("./sections/s54_3_2_design_models.js").content);
sections.push(require("./sections/s55_3_3_architecture.js").content);
sections.push(require("./sections/s56_3_4_data_design_intro.js").content);
sections.push(require("./sections/s57_dd_identity.js").content);
sections.push(require("./sections/s58_dd_academic.js").content);
sections.push(require("./sections/s59_dd_financial.js").content);
sections.push(require("./sections/s60_dd_notices.js").content);
sections.push(require("./sections/s61_dd_analytics.js").content);
sections.push(require("./sections/s61b_3_5_ui_design.js").content);
sections.push(require("./sections/s62_3_6_behavioural_intro.js").content);
sections.push(require("./sections/s63_seq_login.js").content);
sections.push(require("./sections/s64_seq_marks.js").content);
sections.push(require("./sections/s65_seq_attendance.js").content);
sections.push(require("./sections/s66_seq_documents.js").content);
sections.push(require("./sections/s67_3_7_design_decisions.js").content);
sections.push(require("./sections/s68_3_8_summary.js").content);
// --- Chapter 4 (complete) ---
sections.push(require("./sections/s69_ch4_intro_and_4_1.js").content);
sections.push(require("./sections/s70_4_2_apis.js").content);
sections.push(require("./sections/s71_4_3_repo.js").content);
sections.push(require("./sections/s72_4_4_summary.js").content);
// --- Chapter 5 (complete) ---
sections.push(require("./sections/s73_ch5_intro_and_5_1.js").content);
sections.push(require("./sections/s74_5_2_functional.js").content);
sections.push(require("./sections/s75_5_3_integration.js").content);
sections.push(require("./sections/s76_5_4_performance.js").content);
sections.push(require("./sections/s77_5_5_summary.js").content);
// --- Chapter 6 (complete) ---
sections.push(require("./sections/s78_ch6_intro_and_6_1.js").content);
sections.push(require("./sections/s79_6_2_deployment.js").content);
sections.push(require("./sections/s80_6_2_1_data_conversion.js").content);
sections.push(require("./sections/s81_6_2_2_training.js").content);
sections.push(require("./sections/s82_6_3_post_deployment.js").content);
sections.push(require("./sections/s83_6_4_challenges.js").content);
sections.push(require("./sections/s84_6_5_summary.js").content);
// --- Chapter 7 (complete) ---
sections.push(require("./sections/s85_ch7_intro_and_7_1.js").content);
sections.push(require("./sections/s86_7_2_traceability.js").content);
sections.push(require("./sections/s87_7_3_conclusion.js").content);
sections.push(require("./sections/s88_7_4_future_work.js").content);
// --- References (complete) ---
sections.push(require("./sections/s89_references.js").content);
// --- Appendix A (complete) ---
sections.push(require("./sections/s90_appendixA_intro_and_uc1.js").content);
sections.push(require("./sections/s91_uc2_enter_marks.js").content);
sections.push(require("./sections/s92_uc3_approve_mark_edit.js").content);
sections.push(require("./sections/s93_uc4_view_fee_challan.js").content);
sections.push(require("./sections/s94_uc5_build_datesheet.js").content);
sections.push(require("./sections/s95_uc6_upload_document.js").content);
// --- Appendix B (complete) ---
sections.push(require("./sections/s96_appendixB.js").content);
// --- Appendix C (complete) ---
sections.push(require("./sections/s97_appendixC.js").content);

// Add further sections here as they're completed.

const allContent = sections.flat();

const doc = new Document({
  sections: [{ properties: {}, children: allContent }],
});

Packer.toBuffer(doc).then((buf) => {
  const finalPath = __dirname + "/../CMS_FYP_Report.docx";
  const stagePath = __dirname + "/../CMS_FYP_Report.staging.docx";
  fs.writeFileSync(stagePath, buf);
  try {
    fs.copyFileSync(stagePath, finalPath);
    fs.unlinkSync(stagePath);
    console.log("Written " + finalPath);
  } catch (e) {
    console.log("Final path locked (" + e.code + ") — wrote staging copy: " + stagePath);
  }
});
