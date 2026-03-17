package commonclasses.models;

import java.util.HashMap;

public class Teacher {

    // Attributes
    private String fullName;
    private String department;
    private String designation;
    private String workload;

    // Empty Constructor
    public Teacher() {
    }

    // Parameterized Constructor
    public Teacher(String fullName, String department, String designation, String workload) {
        this.fullName = fullName;
        this.department = department;
        this.designation = designation;
        this.workload = workload;
    }

    // Getters and Setters

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }


    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    // Convert to HashMap
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("fullName", fullName);
        map.put("department", department);
        map.put("designation", designation);
        map.put("workload", workload);

        return map;
    }

    // Convert from HashMap to Object
    public static Teacher fromMap(HashMap<String, Object> map) {
        Teacher t = new Teacher();

        t.setFullName((String) map.get("fullName"));
        t.setDepartment((String) map.get("department"));
        t.setDesignation((String) map.get("designation"));
        t.setWorkload((String) map.get("workload"));

        return t;
    }
}
