package commonClasses.models;

import java.util.HashMap;

public class User {
    private String email;
    private String password;
    private boolean isVerified;
    private String linkedTo;
    private String userRole;
    private String createdAt;
    private String updatedAt;
    private boolean disabled;
    public User() {
    }
    public User(String email, String password, boolean isVerified, String linkedTo,
                String userRole, String createdAt, String updatedAt, boolean disabled) {
        this.email = email;
        this.password = password;
        this.isVerified = isVerified;
        this.linkedTo = linkedTo;
        this.userRole = userRole;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.disabled = disabled;

}
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }


    public String getLinkedTo() {
        return linkedTo;
    }

    public void setLinkedTo(String linkedTo) {
        this.linkedTo = linkedTo;
    }


    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }


    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }


    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    // Convert Object to HashMap
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);
        map.put("isVerified", isVerified);
        map.put("linkedTo", linkedTo);
        map.put("userRole", userRole);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("disabled", disabled);
        return map;
    }

    // Convert HashMap to User Object
    public static User fromMap(HashMap<String, Object> map) {
        User user = new User();

        user.setEmail((String) map.get("email"));
        user.setPassword((String) map.get("password"));
        user.setVerified((Boolean) map.get("isVerified"));
        user.setLinkedTo((String) map.get("linkedTo"));
        user.setUserRole((String) map.get("userRole"));
        user.setCreatedAt((String) map.get("createdAt"));
        user.setUpdatedAt((String) map.get("updatedAt"));
        user.setDisabled((Boolean) map.get("disabled"));

        return user;
    }
}
🔥 Important Notes (samajh lo ye)

toMap() → object ko database me bhejne ke liye

fromMap() → database se data lekar object banane ke liye

Empty constructor → Firebase / APIs ke liye zaroori hota hai

boolean ke getters → isVerified() aur isDisabled() hotay hain

Agar chaho to main tumhe next step bhi sikha deta hoon:
        👉 Is User class ko Firebase / API ke sath kaise use karna hai (real project use)

ya
👉 Teacher module ke liye aur models + relationships (ERD level)

ab ak student ki class ha os k attributes ha rollno fullname fathernamedepartment session date of birth phone no parentphone noadress degree type sectonsemester enroled joind date graudated graudation dateleft leftdate remarks photebase64 banned banneddate bloddgroup createdat updated at and methods are tomap frommap

        Great 👍 tumhara Student model kaafi detailed hai — main tumhare liye complete clean Java class bana deta hoon with:

All attributes

Getters / Setters

toMap()

fromMap()

✅ Student.java
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





