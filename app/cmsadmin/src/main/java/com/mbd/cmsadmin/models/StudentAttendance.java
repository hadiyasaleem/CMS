package com.mbd.cmsadmin.models;

import java.util.List;

/**
 * StudentAttendance.java
 * One row in the attendance ledger table.
 *
 * attendanceRecords: ordered list of "P" (Present) | "A" (Absent) | "L" (Leave)
 *                    matching the date columns in the header.
 */
public class StudentAttendance {

    private final String       studentName;
    private final String       rollNo;
    private final List<String> attendanceRecords;  // "P", "A", "L" per date
    private final float        summaryPercent;

    public StudentAttendance(String studentName,
                             String rollNo,
                             List<String> attendanceRecords,
                             float summaryPercent) {
        this.studentName       = studentName;
        this.rollNo            = rollNo;
        this.attendanceRecords = attendanceRecords;
        this.summaryPercent    = summaryPercent;
    }

    public String       getStudentName()       { return studentName;       }
    public String       getRollNo()            { return rollNo;            }
    public List<String> getAttendanceRecords() { return attendanceRecords; }
    public float        getSummaryPercent()    { return summaryPercent;    }
}