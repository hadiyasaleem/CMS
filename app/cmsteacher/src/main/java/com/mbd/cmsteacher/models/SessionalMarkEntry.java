package com.mbd.cmsteacher.models;

/**
 * SessionalMarkEntry.java
 * Data model for a single student's sessional mark row.
 *
 * Components:
 *   attendance   → max 10
 *   assignments  → max 20
 *   quizzes      → max 20
 *   total        → computed: attendance + assignments + quizzes  (max 50)
 */
public class SessionalMarkEntry {

    private final String studentName;
    private final String rollNumber;
    private final int    avatarResId;   // placeholder drawable; swap for Glide URL in prod

    // Marks — -1 means "not yet entered" (shown as blank / placeholder)
    private int attendance;   // 0–10
    private int assignments;  // 0–20
    private int quizzes;      // 0–20

    private static final int NOT_ENTERED = -1;

    // ── Constructor (no marks entered yet) ───────────────────────────────
    public SessionalMarkEntry(String studentName, String rollNumber, int avatarResId) {
        this.studentName = studentName;
        this.rollNumber  = rollNumber;
        this.avatarResId = avatarResId;
        this.attendance  = NOT_ENTERED;
        this.assignments = NOT_ENTERED;
        this.quizzes     = NOT_ENTERED;
    }

    // ── Constructor (marks already entered) ──────────────────────────────
    public SessionalMarkEntry(String studentName, String rollNumber, int avatarResId,
                              int attendance, int assignments, int quizzes) {
        this.studentName = studentName;
        this.rollNumber  = rollNumber;
        this.avatarResId = avatarResId;
        this.attendance  = attendance;
        this.assignments = assignments;
        this.quizzes     = quizzes;
    }

    // ── Computed total ────────────────────────────────────────────────────
    public int getTotal() {
        int a = attendance  == NOT_ENTERED ? 0 : attendance;
        int b = assignments == NOT_ENTERED ? 0 : assignments;
        int c = quizzes     == NOT_ENTERED ? 0 : quizzes;
        return a + b + c;
    }

    public boolean isFullyEntered() {
        return attendance != NOT_ENTERED
                && assignments != NOT_ENTERED
                && quizzes != NOT_ENTERED;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public String getStudentName() { return studentName; }
    public String getRollNumber()  { return rollNumber;  }
    public int    getAvatarResId() { return avatarResId; }

    public int  getAttendance()              { return attendance;  }
    public void setAttendance(int v)         { attendance  = v;    }

    public int  getAssignments()             { return assignments; }
    public void setAssignments(int v)        { assignments = v;    }

    public int  getQuizzes()                 { return quizzes;     }
    public void setQuizzes(int v)            { quizzes     = v;    }

    public boolean isNotEntered(int val)     { return val == NOT_ENTERED; }
    public static int notEnteredValue()      { return NOT_ENTERED; }
}