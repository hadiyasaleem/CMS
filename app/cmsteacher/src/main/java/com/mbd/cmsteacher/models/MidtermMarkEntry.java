package com.mbd.cmsteacher.models;

/**
 * MidtermMarkEntry.java
 * Data model for a single student's midterm mark row.
 *
 * Single score field: marks out of 100.
 * -1 sentinel = not yet entered (field shown as blank/hint).
 */
public class MidtermMarkEntry {

    private static final int NOT_ENTERED = -1;

    private final String studentName;
    private final String rollNumber;
    private final String yearAndDept;   // e.g. "Senior Year • CS"
    private final int    avatarResId;

    private int marks;                  // 0–100, or NOT_ENTERED

    // ── Constructor — no mark entered yet ────────────────────────────────
    public MidtermMarkEntry(String studentName, String rollNumber,
                            String yearAndDept, int avatarResId) {
        this.studentName = studentName;
        this.rollNumber  = rollNumber;
        this.yearAndDept = yearAndDept;
        this.avatarResId = avatarResId;
        this.marks       = NOT_ENTERED;
    }

    // ── Constructor — mark already entered ───────────────────────────────
    public MidtermMarkEntry(String studentName, String rollNumber,
                            String yearAndDept, int avatarResId, int marks) {
        this.studentName = studentName;
        this.rollNumber  = rollNumber;
        this.yearAndDept = yearAndDept;
        this.avatarResId = avatarResId;
        this.marks       = marks;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    public boolean isEntered()           { return marks != NOT_ENTERED; }
    public static int notEnteredValue()  { return NOT_ENTERED; }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public String getStudentName() { return studentName; }
    public String getRollNumber()  { return rollNumber;  }
    public String getYearAndDept() { return yearAndDept; }
    public int    getAvatarResId() { return avatarResId; }
    public int    getMarks()       { return marks;       }
    public void   setMarks(int v)  { marks = v;          }
}