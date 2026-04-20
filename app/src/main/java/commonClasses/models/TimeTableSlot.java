package commonClasses.models;

public class TimeTableSlot {

        private String slotId;
        private String dayOfWeek;
        private String section;
        private String subject;
        private String department;
        private String teacher;
        private String from;
        private String to;
        private String room;

        // Default Constructor
        public TimeTableSlot() {
        }

        // Parameterized Constructor
        public TimeTableSlot(String slotId, String dayOfWeek, String section, String subject,
                             String department, String teacher, String from, String to, String room) {
            this.slotId = slotId;
            this.dayOfWeek = dayOfWeek;
            this.section = section;
            this.subject = subject;
            this.department = department;
            this.teacher = teacher;
            this.from = from;
            this.to = to;
            this.room = room;
        }

        // Getters and Setters

        public String getSlotId() {
            return slotId;
        }

        public void setSlotId(String slotId) {
            this.slotId = slotId;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getTeacher() {
            return teacher;
        }

        public void setTeacher(String teacher) {
            this.teacher = teacher;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }
    }

