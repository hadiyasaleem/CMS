package commonClasses.models;

public class Marksheet {

        private String name;
        private String rollNo;
        private double attendance;
        private double midsMarks;
        private double sessionalMarks;
        private String subject;
        private String createdAt;
        private String updatedAt;

        // Default Constructor
        public void MarkSheet() {
        }

        // Parameterized Constructor
        public void MarkSheet(String name, String rollNo, double attendance, double midsMarks,
                              double sessionalMarks, String subject, String createdAt, String updatedAt) {
            this.name = name;
            this.rollNo = rollNo;
            this.attendance = attendance;
            this.midsMarks = midsMarks;
            this.sessionalMarks = sessionalMarks;
            this.subject = subject;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters and Setters

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRollNo() {
            return rollNo;
        }

        public void setRollNo(String rollNo) {
            this.rollNo = rollNo;
        }

        public double getAttendance() {
            return attendance;
        }

        public void setAttendance(double attendance) {
            this.attendance = attendance;
        }

        public double getMidsMarks() {
            return midsMarks;
        }

        public void setMidsMarks(double midsMarks) {
            this.midsMarks = midsMarks;
        }

        public double getSessionalMarks() {
            return sessionalMarks;
        }

        public void setSessionalMarks(double sessionalMarks) {
            this.sessionalMarks = sessionalMarks;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
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

}
