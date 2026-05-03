package com.mbd.cmsadmin.models;

/**
 * DepartmentItem.java
 * Represents a single academic department shown in AttendanceFragment.
 *
 * Replace fake constructors in AttendanceFragment with Firestore:
 *   db.collection("departments").get()
 *     → map each doc to DepartmentItem(id, name, faculty, iconResId)
 */
public class DepartmentItem {

    private final String id;          // Firestore document ID
    private final String name;        // "Computer Science"
    private final String faculty;     // "Faculty of Engineering & Technology"
    private final int    iconResId;   // R.drawable.ic_computer, etc.
    private       int    semesterCount; // default 8

    public DepartmentItem(String id, String name, String faculty, int iconResId) {
        this.id            = id;
        this.name          = name;
        this.faculty       = faculty;
        this.iconResId     = iconResId;
        this.semesterCount = 8;
    }

    public String getId()           { return id;            }
    public String getName()         { return name;          }
    public String getFaculty()      { return faculty;       }
    public int    getIconResId()    { return iconResId;     }
    public int    getSemesterCount(){ return semesterCount; }

    public void setSemesterCount(int n) { this.semesterCount = n; }
}