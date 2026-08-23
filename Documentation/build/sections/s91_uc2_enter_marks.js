const { Paragraph, HeadingLevel, useCaseTable, tableCaption } = require("../generate_report.js");

const content = [
  new Paragraph({ text: "UC-2  Enter Marks & Request Edit", heading: HeadingLevel.HEADING_2, spacing: { after: 100 } }),
  tableCaption("Table A-2  Use Case Description — Enter Marks & Request Edit"),
  useCaseTable({
    id: "UC-2",
    name: "Enter Marks & Request Edit",
    actors: "Teacher",
    description: "A teacher enters midterm/sessional scores for a class, and later requests a correction to a score already locked.",
    trigger: "Teacher opens Marks Entry for one of their assigned classes.",
    preconditions: "The teacher is assigned to the session/course on the timetable; the exam type (midterm/sessional) is selected.",
    postconditions: "New scores are saved and immediately locked; a correction to an existing score exists as a PENDING request, not yet applied.",
    normalFlow: "1. Teacher selects a class and exam type. 2. For each student without a saved score, teacher types a value; the field validates live (rejects negative/over-max). 3. Teacher taps Save — only the new, valid entries are written. 4. Saved fields become read-only with an edit-pencil icon.",
    alternativeFlows: "2a. Typed value is invalid: an inline error shows and that student's entry is excluded from the save. 4a. Teacher taps the edit-pencil on an already-locked score: a dialog opens for a new score + optional reason; submitting creates a mark_edit_requests row instead of writing the score directly, and the field shows \"Pending review\" until an admin acts (UC-3).",
    businessRules: "The database's session_marks UPDATE policy is admin-only, so a locked score literally cannot be overwritten by a direct write — this is enforced independently of the UI.",
    assumptions: "The class roster and any already-saved scores are already synced to the device (Room cache) before the screen opens.",
  }),
];

module.exports = { content };
