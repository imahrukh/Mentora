package com.fast.mentor;

import java.util.List;

public class Quiz {
    private String quizId;
    private String title;
    private List<Question> questions;
    private int totalPoints;
    private String associatedModuleId;

    public static class Question {
        private String questionText;
        private List<Option> options;
        private int correctOptionIndex;
        private String explanation;
        private int points;
        private int userAnswerIndex;

        public void setUserAnswerIndex(int selectedIndex) {
            this.userAnswerIndex = selectedIndex;

        }

        public static class Option {
            private String text;
            private boolean isCorrect;
            private String explanation;
            private int points;
            public Option() {
                this.text = "";
                this.isCorrect = false;
                this.explanation = "";
                this.points = 0;
            }
            public Option(String text, boolean isCorrect, String explanation, int points) {
                this.text = text;
                this.isCorrect = isCorrect;
                this.explanation = explanation;
                this.points = points;
            }
            public void setText(String text){
                this.text = text;
            }
            public void setCorrect(boolean isCorrect){
                this.isCorrect = isCorrect;
            }
            public void setExplanation(String explanation){
                this.explanation = explanation;
            }
            public void setPoints(int points){
                this.points = points;
            }
            public String getText(){
                return text;
            }
            public boolean isCorrect() {
                return isCorrect;
            }
            public String getExplanation() {
                return explanation;
            }
            public int getPoints() {
                return points;
            }

        }
        public Question() {
            this.questionText = "";
            this.options = List.of(new Option());
            this.correctOptionIndex = 0;
            this.explanation = "";
        }
        public Question(String questionText, List<Option> options, int correctOptionIndex, String explanation, int points) {
            this.questionText = questionText;
            this.options = options;
            this.correctOptionIndex = correctOptionIndex;
        }
        public void setQuestionText(String questionText){
            this.questionText = questionText;
        }
        public void setOptions(List<Option> options) {
            this.options = options;
        }
        public void setCorrectOptionIndex(int correctOptionIndex) {
            this.correctOptionIndex = correctOptionIndex;
        }
        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }
        public String getQuestionText() {
            return questionText;
        }
        public List<Option> getOptions() {
            return options;
        }
        public int getCorrectOptionIndex() {
            return correctOptionIndex;
        }
        public String getExplanation() {
            return explanation;
        }
    }
    public Quiz() {
        this.quizId = "";
        this.title = "";
        this.questions = List.of(new Question());
        this.totalPoints = 0;
        this.associatedModuleId = "";
    }
    public Quiz(String quizId, String title, List<Question> questions, int totalPoints, String associatedModuleId) {
        this.quizId = quizId;
        this.title = title;
        this.questions = questions;
        this.totalPoints = totalPoints;
        this.associatedModuleId = associatedModuleId;
    }
    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
    public void setAssociatedModuleId(String associatedModuleId) {
        this.associatedModuleId = associatedModuleId;
    }
    public String getQuizId() {
        return quizId;
    }
    public String getTitle() {
        return title;
    }
    public List<Question> getQuestions() {
        return questions;
    }
    public int getTotalPoints() {
        return totalPoints;
    }
    public String getAssociatedModuleId() {
        return associatedModuleId;
    }
    public void addQuestion(Question question) {
        questions.add(question);
    }
    public void removeQuestion(Question question) {
        questions.remove(question);
    }
    public String getModuleId(){
        return associatedModuleId;
    }
}
