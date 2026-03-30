package commonClasses.models;

public class Notification {

        private String notificationId;
        private String title;
        private String description;
        private String image;
        private boolean containsFormatting;
        private String createdAt;
        private String sentBy;
        private String type;
        private boolean showingStatusBar;
        private String targetAudience;

        // Default Constructor
        public Notification() {
        }

        // Parameterized Constructor
        public Notification(String notificationId, String title, String description, String image,
                            boolean containsFormatting, String createdAt, String sentBy,
                            String type, boolean showingStatusBar, String targetAudience) {
            this.notificationId = notificationId;
            this.title = title;
            this.description = description;
            this.image = image;
            this.containsFormatting = containsFormatting;
            this.createdAt = createdAt;
            this.sentBy = sentBy;
            this.type = type;
            this.showingStatusBar = showingStatusBar;
            this.targetAudience = targetAudience;
        }

        // Getters and Setters

        public String getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(String notificationId) {
            this.notificationId = notificationId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public boolean isContainsFormatting() {
            return containsFormatting;
        }

        public void setContainsFormatting(boolean containsFormatting) {
            this.containsFormatting = containsFormatting;
        }

        public String getPaginatedAt() {
            return createdAt;
        }

        public void setPaginatedAt(String paginatedAt) {
            this.createdAt = paginatedAt;
        }

        public String getSentBy() {
            return sentBy;
        }

        public void setSentBy(String sentBy) {
            this.sentBy = sentBy;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isShowingStatusBar() {
            return showingStatusBar;
        }

        public void setShowingStatusBar(boolean showingStatusBar) {
            this.showingStatusBar = showingStatusBar;
        }

        public String getTargetAudience() {
            return targetAudience;
        }

        public void setTargetAudience(String targetAudience) {
            this.targetAudience = targetAudience;
        }
    }

