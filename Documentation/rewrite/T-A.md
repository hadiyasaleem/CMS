# Appendix A — Fully Dressed Use Case Descriptions

These are the twenty principal use cases in fully dressed form. They are the same set summarised in Section 2.2.3 and are kept identical to it — Appendix A is the detailed reference, not a second, diverging version. Each names the responsible actor, the applications that support it, the requirements it traces to, the normal sequence, the exceptions worth planning for, and the rules that keep the records trustworthy. There is no document-publishing or document-download use case: that feature (FR-25/FR-26) was withdrawn, so publishing (UC-07) and reading announcements (UC-18) cover notices, calendar events, and datesheets only.

---

**UC-01 — Sign In and Open the Correct Application Area** *(Table A-1)*

| Field | Detail |
|---|---|
| Requirement | FR-1 |
| Purpose | Let a registered user sign in and land in the area that matches their role. |
| Priority | High |
| Applications | All six |
| Actors | Administrator, Teacher, Student |
| Description | A registered user signs in and is taken to the correct role-based home. A still-valid session is reused when the app reopens. |
| Trigger | The user opens an app or submits the sign-in form. |
| Preconditions | The user has a registered account; admin and teacher accounts are active. |
| Postconditions | The right home area is shown and only that role's actions are available. |
| Normal Flow | 1. The user opens the app. 2. It checks for a valid session. 3. If needed, the user enters email and password. 4. The account and role are verified. 5. The role's home opens. |
| Exceptions | Wrong details give a clear message; a disabled account is refused; a role mismatch routes the user to the right app; a connection failure shows a short retry note without exposing service addresses. |
| Business Rules | A user reaches only the role and records their account is assigned. |
| Includes / Extends | Included by every protected use case. |

**UC-02 — Manage Administrator Accounts** *(Table A-2)*

| Field | Detail |
|---|---|
| Requirement | FR-2 |
| Purpose | Let an existing administrator add and maintain other authorised administrators. |
| Priority | High |
| Applications | Admin Mobile, Admin Desktop |
| Actors | Administrator |
| Description | An administrator views the administrator directory, creates an account, and maintains its college identity and status. |
| Trigger | The administrator opens People → Administrators. |
| Preconditions | Signed in as an active administrator. |
| Postconditions | The directory reflects the addition or update. |
| Normal Flow | 1. Open the directory. 2. Existing administrators are listed. 3. Select Add Administrator. 4. Enter identity and contact details. 5. The system validates and creates the account. 6. The directory refreshes. |
| Exceptions | Missing, invalid, or duplicate details are flagged by the field; the last usable administrative access cannot be removed by accident; a service failure leaves the directory unchanged. |
| Business Rules | Only an administrator may create or maintain administrator accounts; email and identity are unique. |
| Includes / Extends | Includes UC-01. |

**UC-03 — Manage Academic Structure** *(Table A-3)*

| Field | Detail |
|---|---|
| Requirement | FR-3, FR-4, FR-5 |
| Purpose | Maintain departments, academic sessions, semesters, and subjects — the foundation every academic record hangs off. |
| Priority | High |
| Applications | Admin Mobile, Admin Desktop |
| Actors | Administrator |
| Description | The administrator creates and reviews departments, sessions, semester progress, and the subjects taught in each semester. |
| Trigger | The administrator opens Academics. |
| Preconditions | Signed in. A department must exist before a session, and a session before its subjects. |
| Postconditions | The structure is available to timetable, attendance, marks, fee, and reporting work. |
| Normal Flow | 1. Select or create a department. 2. Create or open a session. 3. Review semester and session detail. 4. Add subjects to the right semester. 5. The saved structure is confirmed. |
| Exceptions | Duplicate identifiers, invalid years, missing names, or incomplete semester detail are rejected with field-level guidance; deleting something other records depend on is blocked or confirmed. |
| Business Rules | Every session belongs to one department; every subject belongs to a defined session and semester. |
| Includes / Extends | Includes UC-01. |

**UC-04 — Manage Teachers and Students** *(Table A-4)*

| Field | Detail |
|---|---|
| Requirement | FR-6, FR-7 |
| Purpose | Maintain the people tied to each academic session and the responsibilities granted to teachers. |
| Priority | High |
| Applications | Admin Mobile, Admin Desktop |
| Actors | Administrator |
| Description | The administrator adds or imports students, reviews student profiles, creates teacher accounts, and manages teacher status and permissions. |
| Trigger | The administrator opens People, a session roster, or a student profile. |
| Preconditions | Signed in; the relevant department and session exist. |
| Postconditions | Valid teacher and student records are available to authorised workflows. |
| Normal Flow | 1. Open Teachers or a session's Students. 2. Existing records show. 3. Add, import, or update the person. 4. Identity and session information are validated. 5. The list refreshes with the saved record. |
| Exceptions | Duplicate roll numbers or emails are rejected; invalid import rows are reported without hiding the good ones; disabling a teacher is confirmed and keeps their history. |
| Business Rules | A roll number is unique within its session; teacher permissions are granted only by an administrator. |
| Includes / Extends | Includes UC-01; may include UC-19 on desktop. |

**UC-05 — Prepare and Maintain Timetables** *(Table A-5)*

| Field | Detail |
|---|---|
| Requirement | FR-8 |
| Purpose | Build a weekly schedule without booking the same teacher or room into overlapping periods. |
| Priority | High |
| Applications | Admin Mobile, Admin Desktop |
| Actors | Administrator, authorised Teacher |
| Description | An authorised user creates or updates class periods for sessions, subjects, teachers, rooms, days, and times. |
| Trigger | The user opens the Master Timetable or a session timetable. |
| Preconditions | Relevant sessions, subjects, teachers, and rooms exist; the user has timetable permission. |
| Postconditions | The timetable becomes available to the affected administrators, teachers, and students. |
| Normal Flow | 1. Open the timetable. 2. Pick a day and period. 3. Enter session, subject, teacher, room, and time. 4. The system checks for clashes. 5. The period is saved and the timetable refreshes. |
| Exceptions | A teacher, room, or session clash blocks the save and names the conflict; incomplete times or missing assignments are rejected; cancelling changes nothing. |
| Business Rules | A teacher, room, or session cannot hold two overlapping periods (enforced by the database's own no-double-booking constraint). |
| Includes / Extends | Includes UC-01. |

**UC-06 — Review Approval Requests** *(Table A-6)*

| Field | Detail |
|---|---|
| Requirement | FR-14, FR-28 |
| Purpose | Give authorised staff a controlled way to approve student account links and corrections to locked marks. |
| Priority | High |
| Applications | Admin Mobile, Admin Desktop; Teacher Mobile/Desktop for link requests when permitted |
| Actors | Administrator, authorised Teacher |
| Description | The reviewer opens a pending request, compares it with college records, and approves or rejects it with a recorded outcome. |
| Trigger | The reviewer opens a pending request from People or Requests. |
| Preconditions | A pending request exists and the reviewer has permission. |
| Postconditions | The request is approved or rejected; an approval is applied to the student link or mark. |
| Normal Flow | 1. Open the request queue. 2. Select a pending request. 3. Compare submitted and existing information. 4. Choose Approve or Reject. 5. Confirm; the decision is recorded. |
| Exceptions | The reviewer may leave without deciding; a decided request cannot be decided again; a rejection reason is required where asked; a failure leaves the request pending. |
| Business Rules | Whoever submitted a mark-correction request cannot approve it; each request gets exactly one final decision. |
| Includes / Extends | Includes UC-01; may extend UC-11 or UC-14. |

**UC-07 — Publish College Information** *(Table A-7)*

| Field | Detail |
|---|---|
| Requirement | FR-18, FR-20, FR-22, FR-24, FR-29 |
| Purpose | Create and publish datesheets, fees, fines, calendar events, and notifications for the intended users. |
| Priority | High |
| Applications | Admin Mobile, Admin Desktop; permitted Teacher apps for selected notices and datesheets |
| Actors | Administrator, authorised Teacher |
| Description | An authorised user prepares college information, chooses its audience or academic scope, reviews it, and publishes it to the relevant apps. |
| Trigger | The user selects an Add, Create, Issue, or Publish action. |
| Preconditions | Signed in with the required permission; any referenced session or student exists. |
| Postconditions | The item is stored and becomes visible to its intended users according to its publication status and audience. |
| Normal Flow | 1. Open the relevant records area. 2. Enter the content and dates. 3. Choose audience and academic scope. 4. The system validates. 5. The user confirms publication. 6. The published item appears in the list. |
| Exceptions | Missing dates, invalid amounts, or incomplete audience information prevent publication; a datesheet clash is refused by the database; the user may save or cancel where allowed. |
| Business Rules | Only authorised users publish; students see only what is addressed to them or their group; a fine must name a student and amount. |
| Includes / Extends | Includes UC-01; may include UC-19 on desktop. |

**UC-08 — Review Reports and Insights** *(Table A-8)*

| Field | Detail |
|---|---|
| Requirement | FR-10, FR-11, FR-12, FR-15, FR-16, FR-31 |
| Purpose | Help authorised staff review attendance, academic progress, and college summaries without stitching registers together by hand. |
| Priority | Medium |
| Applications | Admin Mobile, Admin Desktop, Teacher Mobile, Teacher Desktop |
| Actors | Administrator, Teacher |
| Description | The user opens a report or insight, applies the available academic filters, and reads totals, trends, or students needing attention — within their own authority. |
| Trigger | The user opens Attendance Records, Insights, or an academic summary. |
| Preconditions | Signed in; relevant academic records exist. |
| Postconditions | The report is displayed; viewing changes no records. |
| Normal Flow | 1. Open a reporting area. 2. Available scope and filters show. 3. Pick a department, session, semester, subject, or period where relevant. 4. The authorised summary is prepared. 5. The user reviews or exports it. |
| Exceptions | No matching records gives an informative empty state; invalid filters are ignored or corrected; a loading failure offers retry. |
| Business Rules | Administrators see college-wide information; teachers see only their assigned records — the same scoping the database enforces. |
| Includes / Extends | Includes UC-01; may include UC-19 on desktop. |

**UC-09 — Mark Class Attendance** *(Table A-9)*

| Field | Detail |
|---|---|
| Requirement | FR-10 |
| Purpose | Record one class meeting's attendance accurately for every enrolled student. |
| Priority | High |
| Applications | Teacher Mobile, Teacher Desktop |
| Actors | Teacher |
| Description | The teacher picks an assigned class and date, marks each student, checks the totals, and submits the register. |
| Trigger | The teacher selects Mark Attendance. |
| Preconditions | Signed in, assigned to the class, roster available. |
| Postconditions | One attendance record is stored per student and becomes visible to authorised users. |
| Normal Flow | 1. Select an assigned class. 2. The date and roster show. 3. Mark each student present, absent, or on leave, adding the late flag where needed. 4. Review the totals. 5. Submit; confirmation shows. |
| Exceptions | An incomplete register is caught before submission; a duplicate register opens for review rather than silently duplicating; a connection failure keeps the screen and offers retry. |
| Business Rules | A teacher marks only assigned classes; one final status per student per meeting, with late recorded as a flag on a present student. The admin can read attendance but never writes it. |
| Includes / Extends | Includes UC-01. |

**UC-10 — Enter Assessment Marks** *(Table A-10)*

| Field | Detail |
|---|---|
| Requirement | FR-12 |
| Purpose | Record valid marks for students in an assigned subject and assessment. |
| Priority | High |
| Applications | Teacher Mobile, Teacher Desktop |
| Actors | Teacher |
| Description | The teacher picks a class and assessment, enters each student's mark, clears any validation messages, and submits the list. |
| Trigger | The teacher selects Marks Entry. |
| Preconditions | Assigned to the subject; roster and the allowed maximum exist. |
| Postconditions | Valid marks are stored and become read-only to the teacher unless a correction is approved. |
| Normal Flow | 1. Select session, subject, semester, and assessment type. 2. The roster shows. 3. Enter marks. 4. The system checks values and completeness. 5. Submit. 6. Confirmation and locked status show. |
| Exceptions | Negative marks, values above the maximum, and missing entries are rejected; an already-submitted list opens locked; a failed submission keeps the entries for retry. |
| Business Rules | Marks stay within the permitted range (midterm out of 25, sessional out of 15); a submitted mark cannot be changed directly. |
| Includes / Extends | Includes UC-01; may be extended by UC-11. |

**UC-11 — Request a Mark Correction** *(Table A-11)*

| Field | Detail |
|---|---|
| Requirement | FR-13 |
| Purpose | Let a teacher request a justified correction instead of editing a locked mark directly. |
| Priority | High |
| Applications | Teacher Mobile, Teacher Desktop |
| Actors | Teacher |
| Description | The teacher selects a locked mark, enters the proposed value and a reason, and submits it for administrative review. |
| Trigger | The teacher selects the correction action beside a locked mark. |
| Preconditions | The teacher owns or is responsible for the mark, and it is locked. |
| Postconditions | A pending request reaches an administrator while the original mark stays unchanged. |
| Normal Flow | 1. Open the locked award list. 2. Select a student's mark. 3. Enter the proposed mark and reason. 4. The system validates the range and checks for another pending request. 5. Submit; pending status shows. |
| Exceptions | An invalid proposed mark or missing reason blocks submission; a second pending request for the same mark is refused; cancelling changes nothing. |
| Business Rules | Only an authorised reviewer applies the change, and never the teacher who requested it; the original value stays active until approval. |
| Includes / Extends | Extends UC-10; may lead to UC-06. |

**UC-12 — Submit Examination Paper and Semester Results** *(Table A-12)*

| Field | Detail |
|---|---|
| Requirement | FR-15, FR-17 |
| Purpose | Let a teacher submit required examination material and record final semester outcomes for assigned students. |
| Priority | High |
| Applications | Teacher Mobile, Teacher Desktop |
| Actors | Teacher |
| Description | The teacher sets the academic context, uploads an examination paper or records semester results (GPA/CGPA and supply subjects), and submits. |
| Trigger | The teacher opens Submit Exam Paper or Semester Results. |
| Preconditions | Assigned to the session or subject; the required students and semester information exist. |
| Postconditions | The paper or result is stored and available to authorised users according to its status. |
| Normal Flow | 1. Select the session and semester. 2. For a paper, pick the subject and file; for results, enter each student's result. 3. The system validates completeness. 4. Review and submit. 5. Confirmation shows. |
| Exceptions | Unsupported or missing files, incomplete results, and invalid academic values block submission; cancelling changes nothing; a transfer failure offers retry. |
| Business Rules | Teachers submit only for assigned work; result values follow the college's grading rules. |
| Includes / Extends | Includes UC-01; may include UC-19 on desktop. |

**UC-13 — View Teaching Schedule and Assigned Students** *(Table A-13)*

| Field | Detail |
|---|---|
| Requirement | FR-6, FR-9 |
| Purpose | Give a teacher a reliable view of assigned classes, periods, and students. |
| Priority | High |
| Applications | Teacher Mobile, Teacher Desktop |
| Actors | Teacher |
| Description | The teacher reviews the weekly schedule, the next class, assigned sessions, and the student rosters the timetable permits. |
| Trigger | The teacher opens Home, Schedule, or My Students. |
| Preconditions | Signed in with at least one assignment. |
| Postconditions | The schedule or roster is shown without change. |
| Normal Flow | 1. Open the area. 2. The teacher's assignments are identified. 3. Classes or students are grouped by schedule and session. 4. Select an item. 5. Details show. |
| Exceptions | No assignment gives a clear empty state; missing information offers refresh; the teacher cannot open an unrelated roster. |
| Business Rules | Teachers see only students and schedules tied to their assigned work. |
| Includes / Extends | Includes UC-01. |

**UC-14 — Register and Link a Student Account** *(Table A-14)*

| Field | Detail |
|---|---|
| Requirement | FR-27, FR-28 |
| Purpose | Connect a student's sign-in account to the correct college roll-number record, after review. |
| Priority | High |
| Applications | Student Mobile, Student Desktop; reviewed in Admin apps and permitted Teacher apps |
| Actors | Student, Administrator, authorised Teacher |
| Description | A student registers, enters identifying college information, submits a link request, and waits for a decision before their records open. |
| Trigger | A new or unlinked student selects Register or Link College Record. |
| Preconditions | A valid email and an existing college enrolment record. |
| Postconditions | A pending request is created; on approval the account links to one student record, on rejection the reason or status shows. |
| Normal Flow | 1. The student registers or signs in. 2. The account is recognised as unlinked. 3. The student enters roll number and identity details. 4. The form is validated and a request submitted. 5. A reviewer verifies it via UC-06. 6. The student sees the outcome and gains access on approval. |
| Exceptions | Invalid details, an unknown roll number, or an already-linked record block submission; a duplicate pending request is not created; rejected requests can be corrected and resubmitted where allowed. |
| Business Rules | One account links to one student record, and one student record has one approved account. |
| Includes / Extends | Includes UC-01 and UC-06. |

**UC-15 — View Personal Attendance and Timetable** *(Table A-15)*

| Field | Detail |
|---|---|
| Requirement | FR-9, FR-11 |
| Purpose | Give a linked student a current view of their class schedule and attendance history. |
| Priority | High |
| Applications | Student Mobile, Student Desktop |
| Actors | Student |
| Description | The student reviews the weekly timetable, next class, attendance totals, per-subject percentages, and available history for their linked record. |
| Trigger | The student opens Attendance or Timetable. |
| Preconditions | Signed in and linked to a college record. |
| Postconditions | The personal schedule or attendance is shown without change. |
| Normal Flow | 1. Open the area. 2. The linked session and roll number are identified. 3. Timetable or attendance loads. 4. Select a day, subject, or summary. 5. Details show. |
| Exceptions | No published timetable or attendance gives an informative empty state; a connection failure offers retry; the student cannot select another student's record. |
| Business Rules | A student sees only their own attendance and their linked session's timetable. |
| Includes / Extends | Includes UC-01. |

**UC-16 — View Marks, Results, and Datesheets** *(Table A-16)*

| Field | Detail |
|---|---|
| Requirement | FR-16, FR-19 |
| Purpose | Let a linked student review their assessment information and the published examination schedules. |
| Priority | High |
| Applications | Student Mobile, Student Desktop |
| Actors | Student |
| Description | The student views subject marks, semester results and progression, and datesheets published for their linked session. |
| Trigger | The student opens Exams, Marks, Results, or Datesheets. |
| Preconditions | Signed in and linked; the relevant records exist or are published. |
| Postconditions | The academic information is shown without allowing student changes. |
| Normal Flow | 1. Open Exams. 2. Select Marks, Results, or Datesheets. 3. Information loads for the linked record and session. 4. Available detail shows. 5. The student reviews it. |
| Exceptions | Unpublished or unavailable information gives a clear message; incomplete history shows only where valid; a loading failure offers retry. |
| Business Rules | A student sees only personal marks and results, and only datesheets published for the relevant audience. |
| Includes / Extends | Includes UC-01. |

**UC-17 — View Fee Challan and Fines** *(Table A-17)*

| Field | Detail |
|---|---|
| Requirement | FR-21, FR-23 |
| Purpose | Give a student current fee information without a routine trip to the accounts office. |
| Priority | High |
| Applications | Student Mobile, Student Desktop |
| Actors | Student |
| Description | The student opens the fee area to review the session fee structure, their challan, due dates, the payment note, and any recorded fines. |
| Trigger | The student selects Fee Challan or Fines. |
| Preconditions | Signed in and linked; a fee structure or fine record exists where applicable. |
| Postconditions | The fee and fine information is shown without recording any payment. |
| Normal Flow | 1. Open the fee area. 2. The linked session and roll number are identified. 3. Fee heads, totals, due information, and fines are prepared. 4. The student reviews them. 5. The challan can be opened, saved, or printed as a PDF. |
| Exceptions | No fee structure or fines gives an informative empty state; missing information is never replaced with an invented amount; a loading failure offers retry. |
| Business Rules | A student sees only their own fee and fine records; showing a challan does not mark it paid. |
| Includes / Extends | Includes UC-01; may include UC-19 on desktop. |

**UC-18 — View Notices, Events, and Datesheets** *(Table A-18)*

| Field | Detail |
|---|---|
| Requirement | FR-24, FR-30 |
| Purpose | Give users one place to read college announcements, calendar events, and published datesheets meant for them. |
| Priority | Medium |
| Applications | All six |
| Actors | Administrator, Teacher, Student |
| Description | The user opens notifications, the calendar, or datesheets and reads the items addressed to their role or academic group. |
| Trigger | The user selects a notice, event, or datesheet from the relevant area or a notification entry. |
| Preconditions | Signed in; the item has been published for the user's audience. |
| Postconditions | The item is shown and may be marked read where supported; the published content is unchanged. |
| Normal Flow | 1. Open the relevant area. 2. Authorised published items are listed. 3. Select an item. 4. Its detail is displayed. 5. The user returns to the list. |
| Exceptions | An expired, removed, or unavailable item gives a safe message; empty lists explain that nothing has been published. |
| Business Rules | Only items addressed to the user's role, session, or the general college audience are shown. |
| Includes / Extends | Includes UC-01. |

**UC-19 — Use Desktop File, Export, and Print Actions** *(Table A-19)*

| Field | Detail |
|---|---|
| Requirement | FR-33 |
| Purpose | Let desktop users complete role-appropriate file, export, and print actions through the familiar operating-system services. |
| Priority | Medium |
| Applications | Admin Desktop, Teacher Desktop, Student Desktop |
| Actors | Administrator, Teacher, Student |
| Description | A desktop user picks a save location, opens a saved file, prints available information, or exports from an existing workflow (for example, exporting a report or printing a challan). |
| Trigger | The user selects Open, Save, Export, Print, or Share where available. |
| Preconditions | Signed in, authorised for the underlying record, using a desktop app. |
| Postconditions | The chosen operating-system action completes, or returns a clear cancellation or failure to the app. |
| Normal Flow | 1. Open an authorised record or form. 2. Select a desktop action. 3. The app opens the matching system dialog (save, open, or print). 4. The user completes the system step. 5. The app confirms or returns to the screen. |
| Exceptions | The user may cancel without changing data; missing file access, an unsupported file type, or no printer gives a clear message; internal service details are never exposed. |
| Business Rules | Desktop actions never bypass the user's role or record permissions; native system windows may look different from the app. |
| Includes / Extends | Extends UC-04, UC-07, UC-08, UC-12, or UC-17. |

**UC-20 — View and Maintain Personal Profile** *(Table A-20)*

| Field | Detail |
|---|---|
| Requirement | FR-32 |
| Purpose | Let each signed-in user review their account identity, update the profile fields they are allowed to, and sign out safely. |
| Priority | Medium |
| Applications | All six |
| Actors | Administrator, Teacher, Student |
| Description | The user opens their profile, reviews account and college identity, edits allowed contact or profile details, and may sign out. |
| Trigger | The user selects Profile or Sign Out. |
| Preconditions | Signed in. |
| Postconditions | Valid permitted changes are stored, or the session ends and protected information is no longer reachable. |
| Normal Flow | 1. Open Profile. 2. Account and college identity show. 3. Edit an allowed field if needed. 4. The system validates and saves. 5. Return or sign out. 6. Sign-out returns to the authentication screen. |
| Exceptions | Invalid details are flagged without discarding valid ones; protected identity and role fields stay read-only; a failed save keeps the previous profile; cancelling sign-out keeps the session active. |
| Business Rules | Users change only fields permitted for their role; role, status, and protected college identity need an authorised administrative process. |
| Includes / Extends | Includes UC-01. |
