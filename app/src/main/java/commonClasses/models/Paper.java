package commonclasses.models;

import java.util.HashMap;

public class Attendance {

    // Attributes
    private String attendanceId;
    private String studentRollNo;
    private String date;
    private String status;
    private String subject;

    // Empty Constructor
    public Attendance() {
    }

    // Parameterized Constructor
    public Attendance(String attendanceId, String studentRollNo,
                      String date, String status, String subject) {
        this.attendanceId = attendanceId;
        this.studentRollNo = studentRollNo;
        this.date = date;
        this.status = status;
        this.subject = subject;
    }

    // Getters and Setters

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }


    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // Convert to HashMap
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("attendanceId", attendanceId);
        map.put("studentRollNo", studentRollNo);
        map.put("date", date);
        map.put("status", status);
        map.put("subject", subject);

        return map;
    }

    // Convert from HashMap to Object
    public static Attendance fromMap(HashMap<String, Object> map) {
        Attendance a = new Attendance();

        a.setAttendanceId((String) map.get("attendanceId"));
        a.setStudentRollNo((String) map.get("studentRollNo"));
        a.setDate((String) map.get("date"));
        a.setStatus((String) map.get("status"));
        a.setSubject((String) map.get("subject"));

        return a;
    }
}
