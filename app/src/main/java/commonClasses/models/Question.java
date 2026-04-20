package commonClasses.models;

public class Question {

        private String questionId;
        private String paperId;
        private String question;
        private int marks;
        private String type;
        private boolean containsImage;
        private boolean containsSymbols;

        // Default Constructor
        public Question() {
        }

        // Parameterized Constructor
        public Question(String questionId, String paperId, String question, int marks,
                        String type, boolean containsImage, boolean containsSymbols) {
            this.questionId = questionId;
            this.paperId = paperId;
            this.question = question;
            this.marks = marks;
            this.type = type;
            this.containsImage = containsImage;
            this.containsSymbols = containsSymbols;
        }

        // Getters and Setters

        public String getQuestionId() {
            return questionId;
        }

        public void setQuestionId(String questionId) {
            this.questionId = questionId;
        }

        public String getPaperId() {
            return paperId;
        }

        public void setPaperId(String paperId) {
            this.paperId = paperId;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public int getMarks() {
            return marks;
        }

        public void setMarks(int marks) {
            this.marks = marks;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isContainsImage() {
            return containsImage;
        }

        public void setContainsImage(boolean containsImage) {
            this.containsImage = containsImage;
        }


        public boolean isContainsSymbols() {
            return containsSymbols;
        }

        public void setContainsSymbols(boolean containsSymbols) {
            this.containsSymbols = containsSymbols;
        }
    }


