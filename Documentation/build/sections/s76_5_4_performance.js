const { Paragraph, TextRun, HeadingLevel, testCaseTable, tableCaption } = require("../generate_report.js");

function para(text) {
  return new Paragraph({ children: [new TextRun({ text })], spacing: { after: 160 } });
}

const content = [
  new Paragraph({ text: "5.4 Performance Testing (PT)", heading: HeadingLevel.HEADING_2, spacing: { after: 120 } }),
  para("No performance testing has been run yet — there's no load on the Supabase project beyond development traffic, so a real number here would be meaningless. The two test cases below are specified so they're ready to run once the app is on real devices with a realistic dataset; the intended tool is Android Studio's built-in Profiler for on-device timing, since nothing beyond that is needed at this scale."),
  tableCaption("Table 5-8  Testcase PT-1"),
  testCaseTable({
    id: "PT-1",
    requirementId: "FR-5",
    title: "Attendance screen load time for a full class",
    description: "Time from opening Mark Attendance to the full roster being interactive.",
    objective: "Confirm the single most frequent screen in the app (used once per class, per day) loads fast enough not to eat into class time.",
    precondition: "A session with a full roster (up to the 50-student cap) already cached in Room.",
    steps: "1. Cold-start the teacher app. 2. Navigate to Mark Attendance. 3. Select a class. 4. Measure time to the roster rendering as interactive.",
    input: "A 50-student roster, cold app start.",
    expected: "Target: under 1 second from selecting the class to the roster being tappable, since it's read from the local Room cache, not the network.",
  }),
  new Paragraph({ spacing: { before: 200 } }),
  tableCaption("Table 5-9  Testcase PT-2"),
  testCaseTable({
    id: "PT-2",
    requirementId: "FR-22",
    title: "Insights screen load time (direct Postgrest reads)",
    description: "Time from opening Insights to all three tabs (Sessions, At-Risk, Exams) finishing their loads.",
    objective: "Confirm the analytics views, which read directly from Postgrest with no local cache, still return in an acceptable time on a typical mobile connection.",
    precondition: "A college-scale dataset (multiple departments, sessions, and a semester's worth of marks/attendance).",
    steps: "1. Open the admin Insights screen. 2. Measure time until all three tabs' data has loaded.",
    input: "College-scale dataset, typical mobile data connection.",
    expected: "Target: under 3 seconds per tab; if it's meaningfully slower, that's a real signal the free-tier Supabase plan's connection limits are becoming a bottleneck (see 2.4.3).",
  }),
];

module.exports = { content };
