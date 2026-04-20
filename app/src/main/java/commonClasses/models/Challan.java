package commonclasses.models;

import java.util.HashMap;

public class Challan {

    // Attributes
    private String challanId;
    private String studentRollNo;
    private double feeAmount;
    private String issueDate;
    private String lastDate;
    private String status;
    private Student student; // reference to Student class

    // Empty Constructor
    public Challan() {
    }

    // Parameterized Constructor
    public Challan(String challanId, String studentRollNo, double feeAmount,
                   String issueDate, String lastDate, String status, Student student) {
        this.challanId = challanId;
        this.studentRollNo = studentRollNo;
        this.feeAmount = feeAmount;
        this.issueDate = issueDate;
        this.lastDate = lastDate;
        this.status = status;
        this.student = student;
    }

    // Getters and Setters

    public String getChallanId() {
        return challanId;
    }

    public void setChallanId(String challanId) {
        this.challanId = challanId;
    }


    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }


    public double getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(double feeAmount) {
        this.feeAmount = feeAmount;
    }


    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }


    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    // Convert to HashMap
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("challanId", challanId);
        map.put("studentRollNo", studentRollNo);
        map.put("feeAmount", feeAmount);
        map.put("issueDate", issueDate);
        map.put("lastDate", lastDate);
        map.put("status", status);

        // Nested object (Student)
        if (student != null) {
            map.put("student", student.toMap());
        }

        return map;
    }

    // Convert from HashMap to Object
    public static Challan fromMap(HashMap<String, Object> map) {
        Challan c = new Challan();

        c.setChallanId((String) map.get("challanId"));
        c.setStudentRollNo((String) map.get("studentRollNo"));
        c.setFeeAmount((Double) map.get("feeAmount"));
        c.setIssueDate((String) map.get("issueDate"));
        c.setLastDate((String) map.get("lastDate"));
        c.setStatus((String) map.get("status"));

        // Nested Student object
        if (map.get("student") != null) {
            c.setStudent(Student.fromMap((HashMap<String, Object>) map.get("student")));
        }

        return c;
    }
}
