package commonclasses.models;

import java.util.HashMap;

public class Student {

    // Attributes
    private String rollNo;
    private String fullName;
    private String fatherName;
    private String department;
    private String session;
    private String dateOfBirth;
    private String phoneNo;
    private String parentPhoneNo;
    private String address;
    private String degreeType;
    private String section;
    private String semester;
    private boolean enrolled;
    private String joinDate;
    private boolean graduated;
    private String graduationDate;
    private boolean left;
    private String leftDate;
    private String remarks;
    private String photoBase64;
    private boolean banned;
    private String bannedDate;
    private String bloodGroup;
    private String createdAt;
    private String updatedAt;

    // Empty Constructor
    public Student() {
    }

    // Parameterized Constructor
    public Student(String rollNo, String fullName, String fatherName, String department,
                   String session, String dateOfBirth, String phoneNo, String parentPhoneNo,
                   String address, String degreeType, String section, String semester,
                   boolean enrolled, String joinDate, boolean graduated, String graduationDate,
                   boolean left, String leftDate, String remarks, String photoBase64,
                   boolean banned, String bannedDate, String bloodGroup,
                   String createdAt, String updatedAt) {

        this.rollNo = rollNo;
        this.fullName = fullName;
        this.fatherName = fatherName;
        this.department = department;
        this.session = session;
        this.dateOfBirth = dateOfBirth;
        this.phoneNo = phoneNo;
        this.parentPhoneNo = parentPhoneNo;
        this.address = address;
        this.degreeType = degreeType;
        this.section = section;
        this.semester = semester;
        this.enrolled = enrolled;
        this.joinDate = joinDate;
        this.graduated = graduated;
        this.graduationDate = graduationDate;
        this.left = left;
        this.leftDate = leftDate;
        this.remarks = remarks;
        this.photoBase64 = photoBase64;
        this.banned = banned;
        this.bannedDate = bannedDate;
        this.bloodGroup = bloodGroup;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getParentPhoneNo() { return parentPhoneNo; }
    public void setParentPhoneNo(String parentPhoneNo) { this.parentPhoneNo = parentPhoneNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDegreeType() { return degreeType; }
    public void setDegreeType(String degreeType) { this.degreeType = degreeType; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public boolean isEnrolled() { return enrolled; }
    public void setEnrolled(boolean enrolled) { this.enrolled = enrolled; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public boolean isGraduated() { return graduated; }
    public void setGraduated(boolean graduated) { this.graduated = graduated; }

    public String getGraduationDate() { return graduationDate; }
    public void setGraduationDate(String graduationDate) { this.graduationDate = graduationDate; }

    public boolean isLeft() { return left; }
    public void setLeft(boolean left) { this.left = left; }

    public String getLeftDate() { return leftDate; }
    public void setLeftDate(String leftDate) { this.leftDate = leftDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    public boolean isBanned() { return banned; }
    public void setBanned(boolean banned) { this.banned = banned; }

    public String getBannedDate() { return bannedDate; }
    public void setBannedDate(String bannedDate) { this.bannedDate = bannedDate; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Convert to HashMap
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("rollNo", rollNo);
        map.put("fullName", fullName);
        map.put("fatherName", fatherName);
        map.put("department", department);
        map.put("session", session);
        map.put("dateOfBirth", dateOfBirth);
        map.put("phoneNo", phoneNo);
        map.put("parentPhoneNo", parentPhoneNo);
        map.put("address", address);
        map.put("degreeType", degreeType);
        map.put("section", section);
        map.put("semester", semester);
        map.put("enrolled", enrolled);
        map.put("joinDate", joinDate);
        map.put("graduated", graduated);
        map.put("graduationDate", graduationDate);
        map.put("left", left);
        map.put("leftDate", leftDate);
        map.put("remarks", remarks);
        map.put("photoBase64", photoBase64);
        map.put("banned", banned);
        map.put("bannedDate", bannedDate);
        map.put("bloodGroup", bloodGroup);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);

        return map;
    }

    // Convert from HashMap to Object
    public static Student fromMap(HashMap<String, Object> map) {
        Student s = new Student();

        s.setRollNo((String) map.get("rollNo"));
        s.setFullName((String) map.get("fullName"));
        s.setFatherName((String) map.get("fatherName"));
        s.setDepartment((String) map.get("department"));
        s.setSession((String) map.get("session"));
        s.setDateOfBirth((String) map.get("dateOfBirth"));
        s.setPhoneNo((String) map.get("phoneNo"));
        s.setParentPhoneNo((String) map.get("parentPhoneNo"));
        s.setAddress((String) map.get("address"));
        s.setDegreeType((String) map.get("degreeType"));
        s.setSection((String) map.get("section"));
        s.setSemester((String) map.get("semester"));
        s.setEnrolled((Boolean) map.get("enrolled"));
        s.setJoinDate((String) map.get("joinDate"));
        s.setGraduated((Boolean) map.get("graduated"));
        s.setGraduationDate((String) map.get("graduationDate"));
        s.setLeft((Boolean) map.get("left"));
        s.setLeftDate((String) map.get("leftDate"));
        s.setRemarks((String) map.get("remarks"));
        s.setPhotoBase64((String) map.get("photoBase64"));
        s.setBanned((Boolean) map.get("banned"));
        s.setBannedDate((String) map.get("bannedDate"));
        s.setBloodGroup((String) map.get("bloodGroup"));
        s.setCreatedAt((String) map.get("createdAt"));
        s.setUpdatedAt((String) map.get("updatedAt"));

        return s;
    }
}