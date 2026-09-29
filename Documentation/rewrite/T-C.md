# Appendix C — Application Prototype

The screenshots in this appendix show the working system across its six apps — three on Android (admin, teacher, student, built with Jetpack Compose) and three on Windows (the same roles, built with Compose Multiplatform). All six share one visual language, the Modernist theme defined in `Theme.kt`, so the same palette, type, and component shapes carry across phone and desktop; what changes between them is layout density, not identity. The figures below are chosen to cover the work each role actually does rather than every screen — one or two representative captures per role, plus the desktop fee-challan output.

The captures should be taken from the current builds. Each figure lists what it should show and a recapture note, because the images themselves cannot be regenerated in this pass.

**Figure C-1 — Admin dashboard (Admin Mobile).** The administrator's home: live summary figures for departments, sessions, teachers, and students, drawn straight from the records.
*Fix-note: REPLACE screenshot with a current capture of the dashboard/home screen from Admin Mobile.*

**Figure C-2 — Session detail and fees (Admin Desktop).** A session opened in the wider desktop layout, showing its students and the fee structure area.
*Fix-note: REPLACE screenshot with a current capture of a session's detail/fees screen from Admin Desktop.*

**Figure C-3 — Mark class attendance (Teacher Mobile).** A class register with each student marked Present, Absent, or Leave and the late flag where it applies, ready to submit.
*Fix-note: REPLACE screenshot with a current capture of the attendance screen from Teacher Mobile.*

**Figure C-4 — Marks entry (Teacher Desktop).** The roster for one subject and assessment, with scores entered within their maxima (midterm out of 25, sessional out of 15) before submission locks them.
*Fix-note: REPLACE screenshot with a current capture of the marks-entry screen from Teacher Desktop.*

**Figure C-5 — Student academic overview (Student Mobile).** The student home bringing together attendance, next class, marks, results, and notices for the linked record.
*Fix-note: REPLACE screenshot with a current capture of the academic-overview/home screen from Student Mobile.*

**Figure C-6 — Fee challan (Admin/Student Desktop).** The generated three-copy challan PDF (student, college, and clerk copies) opened in the viewer before printing.
*Fix-note: REPLACE screenshot with a current capture of the generated fee-challan PDF from the desktop app.*

There is no documents screen in this set — the document-publishing feature was removed — and no screen should be captured for it.
