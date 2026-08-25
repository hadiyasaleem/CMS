const { Paragraph, TextRun, HeadingLevel, table, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "7.2 Traceability Matrix", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("Every requirement from 2.3 traced through to the component that implements it and, where one exists, the test case that exercises it. Several rows say \"Not yet tested\" rather than inventing a Test ID — Chapter 5 only specifies nine test cases, not twenty-two, and that gap is real (see 5.5, 7.4)."),
  tableCaption("Table 7-2  Traceability Matrix"),
  table(
    ["Req.", "Design Specification", "Code", "Test ID"],
    [
      ["FR-1", "DepartmentRepository", "DepartmentRepositoryImpl.kt", "Not yet tested"],
      ["FR-2", "AcademicSessionRepository", "AcademicSessionRepositoryImpl.kt", "Not yet tested"],
      ["FR-3", "CurriculumRepository", "AcademicStructureRepositoryImpls.kt", "Not yet tested"],
      ["FR-4", "SessionTimetableRepository + DB trigger", "SessionTimetableRepositoryImpl.kt", "Not yet tested"],
      ["FR-5", "SessionAttendanceRepository", "SessionAttendanceRepositoryImpl.kt", "Not yet tested"],
      ["FR-6", "SessionMarksRepository + MarksEntryViewModel", "MarksEntryViewModel.kt, MarksEntryScreen.kt", "UT-1, UT-2, FT-1"],
      ["FR-7", "MarkEditRequestRepository", "MarkEditRequestRepositoryImpl.kt, MarksEntryScreen.kt", "FT-2, IT-1"],
      ["FR-8", "MarkEditRequestRepository.approveRequest", "MarkEditRequestsScreen.kt", "FT-2"],
      ["FR-9", "record_semester_result RPC", "SemesterResultsScreen.kt", "Not yet tested"],
      ["FR-10", "SessionFeeRepository", "SessionFeesScreen.kt", "Not yet tested"],
      ["FR-11", "SessionFeeRepository", "FeeChallanScreen.kt", "Not yet tested"],
      ["FR-12", "FineRepository", "StudentProfileScreen.kt", "Not yet tested"],
      ["FR-13", "FineRepository", "ProfileScreen.kt (student)", "Not yet tested"],
      ["FR-14", "CalendarRepository", "CalendarScreen.kt", "Not yet tested"],
      ["FR-15", "DatesheetRepository", "DatesheetsScreen.kt (admin)", "FT-3"],
      ["FR-16", "DatesheetRepository", "DatesheetsScreen.kt (student/teacher)", "FT-3"],
      ["FR-17", "DocumentRepository", "DocumentsScreen.kt (admin)", "Not yet tested"],
      ["FR-18", "DocumentRepository", "DocumentsScreen.kt (student/teacher)", "Not yet tested"],
      ["FR-19", "StudentLinkRequestRepository", "LinkRequestScreen.kt (student)", "IT-2"],
      ["FR-20", "StudentLinkRequestRepository.approveRequest", "LinkRequestsScreen.kt (admin)", "IT-2"],
      ["FR-21", "TeacherRepository.setStatus", "TeachersScreen.kt (admin)", "Not yet tested"],
      ["FR-22", "InsightsRepository + security_invoker views", "InsightsScreen.kt (admin/teacher)", "PT-2"],
    ],
    [900, 2900, 3660, 1900],
  ),
];

module.exports = { content };
