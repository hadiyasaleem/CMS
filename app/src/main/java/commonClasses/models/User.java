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
