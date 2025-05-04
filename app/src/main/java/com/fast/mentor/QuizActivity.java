package com.fast.mentor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private Quiz quiz;
    private List<Quiz.Question> questionViews = new ArrayList<>();
    private int totalPoints = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quiz = getIntent().getParcelableExtra("quiz");
        setupQuizUI();
        setupSubmitButton();
    }

    private void setupQuizUI() {
        LinearLayout container = findViewById(R.id.quizContainer);
        for (Quiz.Question question : quiz.getQuestions()) {
            View questionView = LayoutInflater.from(this).inflate(R.layout.item_question, container, false);
            setupQuestionView(questionView, question);
            container.addView(questionView);
        }
    }

    private void setupQuestionView(View questionView, Quiz.Question question) {
        TextView tvQuestion = questionView.findViewById(R.id.tvQuestionText);
        RadioGroup optionsGroup = questionView.findViewById(R.id.optionsGroup);
        TextView tvExplanation = questionView.findViewById(R.id.tvExplanation);

        tvQuestion.setText(question.getQuestionText());

        for (int i = 0; i < question.getOptions().size(); i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(question.getOptions().get(i));
            rb.setTag(i);
            optionsGroup.addView(rb);
        }

        optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = group.findViewById(checkedId);
            int selectedIndex = (int) selected.getTag();
            question.setUserAnswerIndex(selectedIndex);
            tvExplanation.setVisibility(View.VISIBLE);
            tvExplanation.setText(question.getExplanation());
        });
    }

    private void setupSubmitButton() {
        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            calculateScore();
            showResultDialog();
            updateProgress();
        });
    }

    private void calculateScore() {
        totalPoints = 0;
        for (Quiz.Question question : quiz.getQuestions()) {
            if (question.getUserAnswerIndex() == question.getCorrectOptionIndex()) {
                totalPoints += question.getPoints();
            }
        }
    }

    private void showResultDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Quiz Result")
                .setMessage("You scored " + totalPoints + "/" + quiz.getTotalPoints())
                .setPositiveButton("OK", null)
                .show();
    }

    private void updateProgress() {
        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("enrolledCourses").document(courseId)
                .update("progress." + quiz.getAssociatedModuleId() + ".quizScore", totalPoints);
    }
}