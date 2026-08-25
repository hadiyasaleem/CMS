/**
 * One-off utility: seeds sample Firestore data for the CMS Admin/Teacher/Student apps,
 * and bootstraps the users/{uid} RBAC doc for the first Admin account (which the app
 * itself can never self-provision, by design — see the architecture doc §4).
 *
 * Usage:
 *   npm install
 *   node seed.js [path-to-service-account.json] [admin-uid] [admin-email]
 *
 * Defaults match the admin account already created in Firebase Auth for this project.
 * Safe to re-run — every write is a `.set()` on a deterministic doc id, so re-running
 * overwrites with the same sample data rather than duplicating anything.
 */
const path = require("path");
const admin = require("firebase-admin");

const serviceAccountPath = process.argv[2]
  ? path.resolve(process.argv[2])
  : path.resolve(__dirname, "../../cms-mbd-firebase-adminsdk-fbsvc-200fb1bbe1.json");
const ADMIN_UID = process.argv[3] || "kgg5L2P82KQ5xN32YnMwOa5DfJP2";
const ADMIN_EMAIL = process.argv[4] || "admin@example.com";

admin.initializeApp({
  credential: admin.credential.cert(require(serviceAccountPath)),
});

const db = admin.firestore();
const auth = admin.auth();
const now = () => admin.firestore.Timestamp.now();

async function ensureTeacherAccount(email, password) {
  try {
    const existing = await auth.getUserByEmail(email);
    console.log(`  teacher auth account already exists: ${email} (${existing.uid})`);
    return existing.uid;
  } catch (e) {
    const created = await auth.createUser({ email, password, emailVerified: true });
    console.log(`  created teacher auth account: ${email} (${created.uid})`);
    return created.uid;
  }
}

async function main() {
  console.log(`Seeding project from service account: ${serviceAccountPath}`);
  console.log(`Admin uid: ${ADMIN_UID}, email: ${ADMIN_EMAIL}`);

  // 1. Bootstrap the Admin RBAC doc — required before the Admin app will let this
  //    account past the "not an Admin account" gate.
  await db.collection("users").doc(ADMIN_UID).set({
    role: "ADMIN",
    teacherId: null,
    linkedStudentId: null,
    createdBy: "system-seed",
    createdAt: now(),
    updatedBy: "system-seed",
    updatedAt: now(),
  });
  console.log("Seeded users/" + ADMIN_UID + " (role=ADMIN)");

  // 2. Academic term (active)
  const termId = "Fall2025";
  await db.collection("academicTerms").doc(termId).set({
    termId,
    label: "Fall 2025",
    academicYear: 2025,
    termType: "FALL",
    startDate: admin.firestore.Timestamp.fromDate(new Date("2025-09-01")),
    endDate: admin.firestore.Timestamp.fromDate(new Date("2026-01-15")),
    isActive: true,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  console.log("Seeded academicTerms/" + termId + " (active)");

  // 3. Departments
  const departments = [
    { deptId: "cs", name: "Computer Science", code: "CS" },
    { deptId: "math", name: "Mathematics", code: "MATH" },
  ];
  for (const d of departments) {
    await db.collection("departments").doc(d.deptId).set({
      ...d,
      isActive: true,
      archivedAt: null,
      createdBy: ADMIN_UID,
      createdAt: now(),
      updatedBy: ADMIN_UID,
      updatedAt: now(),
    });
  }
  console.log(`Seeded ${departments.length} departments`);

  // 4. Subjects (CS, semester 3)
  const subjects = [
    { subjectId: "cs_cs301", deptId: "cs", courseCode: "CS301", name: "Data Structures", semester: 3, creditHours: 3, type: "THEORY" },
    { subjectId: "cs_cs302", deptId: "cs", courseCode: "CS302", name: "Database Systems", semester: 3, creditHours: 3, type: "THEORY" },
    { subjectId: "cs_cs303l", deptId: "cs", courseCode: "CS303L", name: "Data Structures Lab", semester: 3, creditHours: 1, type: "LAB" },
  ];
  for (const s of subjects) {
    await db.collection("subjects").doc(s.subjectId).set({
      ...s,
      isActive: true,
      createdBy: ADMIN_UID,
      createdAt: now(),
      updatedBy: ADMIN_UID,
      updatedAt: now(),
    });
  }
  console.log(`Seeded ${subjects.length} subjects`);

  // 5. Class (abstract dept+semester+session slot)
  const classId = "cs_3_morning";
  await db.collection("classes").doc(classId).set({
    classId,
    deptId: "cs",
    semester: 3,
    session: "MORNING",
    name: "CS - Semester 3 - Morning",
    isActive: true,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  console.log("Seeded classes/" + classId);

  // 6. Class offering (per-term instantiation — the actual roster container)
  const offeringId = `${classId}_${termId}`;
  await db.collection("classOfferings").doc(offeringId).set({
    offeringId,
    classId,
    termId,
    deptId: "cs",
    semester: 3,
    session: "MORNING",
    name: "CS - Semester 3 - Morning — Fall 2025",
    isActive: true,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  console.log("Seeded classOfferings/" + offeringId);

  // 7. A sample Teacher (real Firebase Auth account + Firestore profile + RBAC doc)
  const teacherUid = await ensureTeacherAccount("teacher@example.com", "Teacher@12345");
  await db.collection("teachers").doc(teacherUid).set({
    teacherId: teacherUid,
    name: "Ayesha Khan",
    email: "teacher@example.com",
    phone: null,
    deptId: "cs",
    designation: "Lecturer",
    permissions: {
      canApproveLinkRequests: false,
      canEditTimetable: false,
      canSendNotifications: false,
    },
    isActive: true,
    archivedAt: null,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  await db.collection("users").doc(teacherUid).set({
    role: "TEACHER",
    teacherId: teacherUid,
    linkedStudentId: null,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  console.log("Seeded teachers/" + teacherUid + " and users/" + teacherUid + " (login: teacher@example.com / Teacher@12345)");

  // 8. Assign the teacher to CS301 for this offering
  const assignmentId = `${offeringId}_cs_cs301`;
  await db.collection("classSubjectAssignments").doc(assignmentId).set({
    assignmentId,
    offeringId,
    subjectId: "cs_cs301",
    teacherId: teacherUid,
    termId,
    isActive: true,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  console.log("Seeded classSubjectAssignments/" + assignmentId);

  // 9. Sample students in this offering (not yet linked to any Student-app account)
  const students = [
    { studentId: "cs2024015", rollNumber: "CS-2024-015", name: "Ali Raza" },
    { studentId: "cs2024016", rollNumber: "CS-2024-016", name: "Sara Ahmed" },
    { studentId: "cs2024017", rollNumber: "CS-2024-017", name: "Bilal Hussain" },
    { studentId: "cs2024018", rollNumber: "CS-2024-018", name: "Hina Malik" },
    { studentId: "cs2024019", rollNumber: "CS-2024-019", name: "Usman Tariq" },
  ];
  for (const s of students) {
    await db.collection("students").doc(s.studentId).set({
      ...s,
      offeringId,
      deptId: "cs",
      currentSemester: 3,
      linkedAuthUid: null,
      isActive: true,
      archivedAt: null,
      createdBy: ADMIN_UID,
      createdAt: now(),
      updatedBy: ADMIN_UID,
      updatedAt: now(),
    });
  }
  console.log(`Seeded ${students.length} students into ${offeringId}`);

  // 10. Fee structure for this offering (itemized heads, semester type)
  const feeStructureId = `${offeringId}_SEMESTER`;
  const heads = [
    { label: "Tuition Fee", amount: 15000.0 },
    { label: "Exam Fee", amount: 2000.0 },
    { label: "Fund", amount: 500.0 },
  ];
  await db.collection("feeStructures").doc(feeStructureId).set({
    feeStructureId,
    offeringId,
    feeType: "SEMESTER",
    heads,
    totalAmount: heads.reduce((sum, h) => sum + h.amount, 0),
    isActive: true,
    createdBy: ADMIN_UID,
    createdAt: now(),
    updatedBy: ADMIN_UID,
    updatedAt: now(),
  });
  console.log("Seeded feeStructures/" + feeStructureId);

  // 11. A welcome notification so the Notifications screen isn't empty
  const notifRef = db.collection("notifications").doc();
  await notifRef.set({
    notificationId: notifRef.id,
    title: "Welcome to CMS",
    body: "Sample data has been seeded: 1 term, 2 departments, 3 subjects, 1 class offering, 5 students, 1 teacher, and a fee structure.",
    targetRole: "ALL",
    targetOfferingId: null,
    createdByUid: ADMIN_UID,
    createdAt: now(),
  });
  console.log("Seeded a welcome notification");

  console.log("\nDone. Log into the Admin app with the existing admin@example.com account.");
  console.log("Log into the Teacher app with teacher@example.com / Teacher@12345.");
}

main()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("Seed failed:", err);
    process.exit(1);
  });
