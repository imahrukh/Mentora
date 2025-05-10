package com.fast.mentor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fast.mentor.R;
import com.fast.mentor.database.DatabaseHelper;
import com.fast.mentor.model.QuizQuestion;
import com.fast.mentor.tracking.ContentTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for taking quizzes with automatic completion tracking
 */
public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    
    private TextView titleTextView;
    private TextView descriptionTextView;
    private RecyclerView questionsRecyclerView;
    private Button submitButton;
    private ImageButton backButton;
    
    private int contentId;
    private int userId;
    private int quizId;
    private String quizTitle;
    private String quizDescription;
    private boolean isQuizComplete = false;
    
    private ContentTracker contentTracker;
    private DatabaseHelper dbHelper;
    private List<QuizQuestion> questions;
    private Map<Integer, Integer> userAnswers; // Question ID -> Selected answer index
    
    private long startTimeMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        
        // Initialize UI components
        titleTextView = findViewById(R.id.quiz_title);
        descriptionTextView = findViewById(R.id.quiz_description);
        questionsRecyclerView = findViewById(R.id.questions_recyclerview);
        submitButton = findViewById(R.id.submit_button);
        backButton = findViewById(R.id.back_button);
        
        // Get data from intent
        Intent intent = getIntent();
        contentId = intent.getIntExtra("content_id", -1);
        userId = intent.getIntExtra("user_id", -1); // Get from login session in real app
        quizId = intent.getIntExtra("quiz_id", -1);
        quizTitle = intent.getStringExtra("quiz_title");
        quizDescription = intent.getStringExtra("quiz_description");
        
        if (contentId == -1 || userId == -1 || quizId == -1) {
            Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Set title and description
        titleTextView.setText(quizTitle != null ? quizTitle : "Quiz");
        descriptionTextView.setText(quizDescription != null ? quizDescription : "Test your knowledge");
        
        // Initialize helpers
        contentTracker = ContentTracker.getInstance(this);
        dbHelper = DatabaseHelper.getInstance(this);
        userAnswers = new HashMap<>();
        
        // Setup RecyclerView
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup button listeners
        backButton.setOnClickListener(v -> showExitConfirmation());
        submitButton.setOnClickListener(v -> submitQuiz());
        
        // Load quiz questions
        loadQuizQuestions();
        
        // Start tracking content consumption
        contentTracker.startTracking(userId, contentId, "quiz");
        startTimeMs = System.currentTimeMillis();
    }
    
    /**
     * Load quiz questions from database
     */
    private void loadQuizQuestions() {
        // In a real app, this would query the database for questions
        // For this example, we'll create some sample questions
        questions = new ArrayList<>();
        
        // Sample questions (in a real app, these would come from the database)
        QuizQuestion q1 = new QuizQuestion();
        q1.setId(1);
        q1.setQuizId(quizId);
        q1.setQuestion("What is the standard video completion threshold in Mentora?");
        q1.setOptions(new String[] {
            "5 seconds from the end",
            "10 seconds from the end",
            "15 seconds from the end",
            "20 seconds from the end"
        });
        q1.setCorrectOptionIndex(1); // 10 seconds from the end
        q1.setPoints(10);
        
        QuizQuestion q2 = new QuizQuestion();
        q2.setId(2);
        q2.setQuizId(quizId);
        q2.setQuestion("How does Mentora determine if a PDF is completed?");
        q2.setOptions(new String[] {
            "User reaches the first page",
            "User scrolls through all pages quickly",
            "User reaches the last page and stays for a few seconds",
            "PDF is always marked complete after opening"
        });
        q2.setCorrectOptionIndex(2); // User reaches the last page
        q2.setPoints(10);
        
        QuizQuestion q3 = new QuizQuestion();
        q3.setId(3);
        q3.setQuizId(quizId);
        q3.setQuestion("What learning pace category is assigned to users who complete content much faster than expected?");
        q3.setOptions(new String[] {
            "Medium",
            "Fast",
            "Efficient",
            "Expert"
        });
        q3.setCorrectOptionIndex(1); // Fast
        q3.setPoints(10);
        
        questions.add(q1);
        questions.add(q2);
        questions.add(q3);
        
        // Set up the adapter
        QuestionAdapter adapter = new QuestionAdapter(questions, this::onAnswerSelected);
        questionsRecyclerView.setAdapter(adapter);
    }
    
    /**
     * Handle answer selection
     */
    private void onAnswerSelected(int questionId, int selectedOptionIndex) {
        userAnswers.put(questionId, selectedOptionIndex);
        
        // Log the selection for debugging
        Log.d(TAG, "Question " + questionId + ": selected option " + selectedOptionIndex);
        
        // Enable submit button if all questions are answered
        if (userAnswers.size() == questions.size()) {
            submitButton.setEnabled(true);
        }
    }
    
    /**
     * Submit the quiz for grading
     */
    private void submitQuiz() {
        // Check if all questions are answered
        if (userAnswers.size() < questions.size()) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calculate score
        int totalPoints = 0;
        int earnedPoints = 0;
        
        for (QuizQuestion question : questions) {
            totalPoints += question.getPoints();
            
            Integer selectedOption = userAnswers.get(question.getId());
            if (selectedOption != null && selectedOption == question.getCorrectOptionIndex()) {
                earnedPoints += question.getPoints();
            }
        }
        
        final int score = (totalPoints > 0) ? (earnedPoints * 100 / totalPoints) : 0;
        final boolean passed = score >= 70; // Passing score is 70%
        
        // Calculate time spent
        long timeSpentMs = System.currentTimeMillis() - startTimeMs;
        int timeSpentSeconds = (int) (timeSpentMs / 1000);
        
        // Show results
        showResults(score, passed, earnedPoints, totalPoints);
        
        // Mark as completed if passed
        if (passed) {
            contentTracker.markAsCompleted(userId, contentId);
            isQuizComplete = true;
        } else {
            // Update tracker with attempt but not mark as complete
            contentTracker.updatePosition(userId, contentId, userAnswers.size(), questions.size());
        }
        
        // In a real app, save the quiz attempt in the database
        saveQuizAttempt(score, passed, timeSpentSeconds);
    }
    
    /**
     * Save quiz attempt to database
     */
    private void saveQuizAttempt(int score, boolean passed, int timeSpentSeconds) {
        // In a real app, this would save the attempt to the database
        Log.d(TAG, "Quiz attempt saved: score=" + score + ", passed=" + passed + 
                ", time=" + timeSpentSeconds + "s");
    }
    
    /**
     * Show quiz results dialog
     */
    private void showResults(int score, boolean passed, int earnedPoints, int totalPoints) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(passed ? "Quiz Passed!" : "Quiz Failed");
        
        String message = "Score: " + score + "%\n" +
                         "Points: " + earnedPoints + " of " + totalPoints + "\n\n";
        
        if (passed) {
            message += "Congratulations! You've successfully completed this quiz.";
        } else {
            message += "You need 70% to pass. Review the material and try again.";
        }
        
        builder.setMessage(message);
        
        builder.setPositiveButton("Review Answers", (dialog, which) -> {
            // In a real app, this would show the correct answers
            Toast.makeText(this, "Review feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton(passed ? "Continue" : "Try Again", (dialog, which) -> {
            if (passed) {
                finish(); // Return to course view
            } else {
                // Reset the quiz
                userAnswers.clear();
                QuestionAdapter adapter = new QuestionAdapter(questions, this::onAnswerSelected);
                questionsRecyclerView.setAdapter(adapter);
                submitButton.setEnabled(false);
                startTimeMs = System.currentTimeMillis();
            }
        });
        
        builder.setCancelable(false);
        builder.show();
    }
    
    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmation() {
        if (userAnswers.isEmpty() || isQuizComplete) {
            finish();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Quiz");
        builder.setMessage("Your progress will not be saved. Are you sure you want to exit?");
        
        builder.setPositiveButton("Exit", (dialog, which) -> finish());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        
        builder.show();
    }
    
    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }
    
    @Override
    protected void onDestroy() {
        // Stop tracking if not completed
        if (!isQuizComplete) {
            contentTracker.stopTracking(userId, contentId);
        }
        
        super.onDestroy();
    }
    
    /**
     * Adapter for quiz questions
     */
    private static class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
        private final List<QuizQuestion> questions;
        private final OnAnswerSelectedListener listener;
        
        public interface OnAnswerSelectedListener {
            void onAnswerSelected(int questionId, int selectedOptionIndex);
        }
        
        public QuestionAdapter(List<QuizQuestion> questions, OnAnswerSelectedListener listener) {
            this.questions = questions;
            this.listener = listener;
        }
        
        @Override
        public QuestionViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_quiz_question, parent, false);
            return new QuestionViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(QuestionViewHolder holder, int position) {
            QuizQuestion question = questions.get(position);
            holder.bind(question, listener);
        }
        
        @Override
        public int getItemCount() {
            return questions.size();
        }
        
        static class QuestionViewHolder extends RecyclerView.ViewHolder {
            private final TextView questionTextView;
            private final RadioGroup optionsRadioGroup;
            private final RadioButton[] optionButtons;
            
            public QuestionViewHolder(View itemView) {
                super(itemView);
                questionTextView = itemView.findViewById(R.id.question_text);
                optionsRadioGroup = itemView.findViewById(R.id.options_radio_group);
                
                // Assuming we have 4 radio buttons in the layout
                optionButtons = new RadioButton[4];
                optionButtons[0] = itemView.findViewById(R.id.option1);
                optionButtons[1] = itemView.findViewById(R.id.option2);
                optionButtons[2] = itemView.findViewById(R.id.option3);
                optionButtons[3] = itemView.findViewById(R.id.option4);
            }
            
            public void bind(final QuizQuestion question, final OnAnswerSelectedListener listener) {
                // Set question text
                questionTextView.setText(question.getQuestion());
                
                // Set options
                String[] options = question.getOptions();
                for (int i = 0; i < optionButtons.length; i++) {
                    if (i < options.length) {
                        optionButtons[i].setText(options[i]);
                        optionButtons[i].setVisibility(View.VISIBLE);
                    } else {
                        optionButtons[i].setVisibility(View.GONE);
                    }
                }
                
                // Clear previous selection
                optionsRadioGroup.clearCheck();
                
                // Set click listener
                optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    int selectedOptionIndex = -1;
                    
                    if (checkedId == R.id.option1) selectedOptionIndex = 0;
                    else if (checkedId == R.id.option2) selectedOptionIndex = 1;
                    else if (checkedId == R.id.option3) selectedOptionIndex = 2;
                    else if (checkedId == R.id.option4) selectedOptionIndex = 3;
                    
                    if (selectedOptionIndex != -1) {
                        listener.onAnswerSelected(question.getId(), selectedOptionIndex);
                    }
                });
            }
        }
    }
}