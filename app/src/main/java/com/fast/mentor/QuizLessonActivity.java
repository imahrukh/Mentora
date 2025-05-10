package com.fast.mentor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.fast.mentor.R;
import com.fast.mentor.model.Quiz;
import com.fast.mentor.model.QuizQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Activity for quiz-based lessons.
 */
public class QuizLessonActivity extends LessonContentActivity {

    private TextView quizTitleTextView;
    private TextView quizDescriptionTextView;
    private TextView questionCountTextView;
    private TextView passingScoreTextView;
    private TextView timeLimitTextView;
    private TextView questionProgressTextView;
    private TextView timeRemainingTextView;
    private ProgressBar quizProgressBar;
    private ViewPager2 questionViewPager;
    private CardView quizResultCard;
    private ImageView resultIconImageView;
    private TextView resultTitleTextView;
    private TextView resultMessageTextView;
    private TextView scoreNumberTextView;
    private TextView correctAnswersTextView;
    private TextView timeSpentTextView;
    private Button reviewAnswersButton;
    private Button continueButton;
    
    private QuizQuestionAdapter questionAdapter;
    private CountDownTimer quizTimer;
    private long quizStartTime;
    private long timeRemaining;
    private long timerDuration;
    
    private Quiz quiz;
    private List<QuizQuestion> questions = new ArrayList<>();
    private boolean isQuizCompleted = false;
    private boolean isReviewMode = false;
    private int totalQuestions = 0;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int score = 0;
    private boolean isPassed = false;
    
    private static final int RESULT_PASS = 1;
    private static final int RESULT_FAIL = 2;

    /**
     * Create intent to launch this activity
     */
    public static Intent createIntent(Context context, String lessonId, String moduleId, 
                                     String courseId, boolean isFirstLesson, boolean isLastLesson,
                                     String previousLessonId, String nextLessonId) {
        Intent intent = new Intent(context, QuizLessonActivity.class);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        intent.putExtra(EXTRA_MODULE_ID, moduleId);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_IS_FIRST_LESSON, isFirstLesson);
        intent.putExtra(EXTRA_IS_LAST_LESSON, isLastLesson);
        intent.putExtra(EXTRA_PREVIOUS_LESSON_ID, previousLessonId);
        intent.putExtra(EXTRA_NEXT_LESSON_ID, nextLessonId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inflate quiz content layout into content container
        LayoutInflater.from(this).inflate(R.layout.content_quiz_lesson, contentContainer, true);
        
        // Initialize quiz-specific views
        initializeQuizViews();
        
        // Restore state if needed
        if (savedInstanceState != null) {
            isQuizCompleted = savedInstanceState.getBoolean("is_quiz_completed", false);
            isReviewMode = savedInstanceState.getBoolean("is_review_mode", false);
            currentQuestionIndex = savedInstanceState.getInt("current_question_index", 0);
            correctAnswers = savedInstanceState.getInt("correct_answers", 0);
            score = savedInstanceState.getInt("score", 0);
            isPassed = savedInstanceState.getBoolean("is_passed", false);
            timeRemaining = savedInstanceState.getLong("time_remaining", 0);
        }
    }

    /**
     * Initialize quiz-specific views
     */
    private void initializeQuizViews() {
        quizTitleTextView = findViewById(R.id.quizTitleTextView);
        quizDescriptionTextView = findViewById(R.id.quizDescriptionTextView);
        questionCountTextView = findViewById(R.id.questionCountTextView);
        passingScoreTextView = findViewById(R.id.passingScoreTextView);
        timeLimitTextView = findViewById(R.id.timeLimitTextView);
        questionProgressTextView = findViewById(R.id.questionProgressTextView);
        timeRemainingTextView = findViewById(R.id.timeRemainingTextView);
        quizProgressBar = findViewById(R.id.quizProgressBar);
        questionViewPager = findViewById(R.id.questionViewPager);
        quizResultCard = findViewById(R.id.quizResultCard);
        resultIconImageView = findViewById(R.id.resultIconImageView);
        resultTitleTextView = findViewById(R.id.resultTitleTextView);
        resultMessageTextView = findViewById(R.id.resultMessageTextView);
        scoreNumberTextView = findViewById(R.id.scoreNumberTextView);
        correctAnswersTextView = findViewById(R.id.correctAnswersTextView);
        timeSpentTextView = findViewById(R.id.timeSpentTextView);
        reviewAnswersButton = findViewById(R.id.reviewAnswersButton);
        continueButton = findViewById(R.id.continueButton);
        
        // Setup ViewPager for questions
        questionAdapter = new QuizQuestionAdapter(this);
        questionViewPager.setAdapter(questionAdapter);
        
        // Disable swipe in review mode
        questionViewPager.setUserInputEnabled(true);
        
        // Add page change callback
        questionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentQuestionIndex = position;
                updateQuestionProgress();
            }
        });
        
        // Setup result screen buttons
        reviewAnswersButton.setOnClickListener(v -> showReviewMode());
        continueButton.setOnClickListener(v -> {
            if (isPassed) {
                // Mark lesson as complete and proceed
                markLessonComplete();
            } else {
                // Just go back to course
                finish();
            }
        });
    }

    @Override
    protected void initializeLessonContent() {
        // Set lesson title
        setTitle(lesson.getTitle());
        
        // Load quiz
        loadQuiz();
    }

    /**
     * Load quiz data from Firestore
     */
    private void loadQuiz() {
        showLoading();
        
        // Load quiz
        courseService.getQuiz(lessonId, loadedQuiz -> {
            quiz = loadedQuiz;
            
            // Set quiz info
            setupQuizInfo();
            
            // Load quiz questions
            loadQuizQuestions();
        }, e -> {
            showError("Failed to load quiz: " + e.getMessage());
        });
    }

    /**
     * Load quiz questions
     */
    private void loadQuizQuestions() {
        courseService.getQuizQuestions(quiz.getId(), loadedQuestions -> {
            questions = loadedQuestions;
            totalQuestions = questions.size();
            
            // Set up quiz questions
            setupQuizQuestions();
            
            // Hide loading
            hideLoading();
            
            // Start quiz if not already completed
            if (!isQuizCompleted) {
                startQuiz();
            } else if (isReviewMode) {
                showReviewMode();
            } else {
                showQuizResults(isPassed ? RESULT_PASS : RESULT_FAIL);
            }
        }, e -> {
            showError("Failed to load quiz questions: " + e.getMessage());
        });
    }

    /**
     * Setup quiz info in UI
     */
    private void setupQuizInfo() {
        quizTitleTextView.setText(quiz.getTitle());
        quizDescriptionTextView.setText(quiz.getDescription());
        
        // Set question count
        int questionCount = quiz.getQuestionCount();
        questionCountTextView.setText(getResources().getQuantityString(
                R.plurals.question_count, questionCount, questionCount));
        
        // Set passing score
        passingScoreTextView.setText(getString(R.string.passing_score_percent, quiz.getPassingScore()));
        
        // Set time limit
        int timeLimit = quiz.getTimeLimit();
        timeLimitTextView.setText(getString(R.string.time_limit_minutes, timeLimit));
        
        // Calculate timer duration in milliseconds
        timerDuration = TimeUnit.MINUTES.toMillis(timeLimit);
        timeRemaining = timerDuration;
    }

    /**
     * Setup quiz questions
     */
    private void setupQuizQuestions() {
        questionAdapter.setQuestions(questions, isReviewMode);
        updateQuestionProgress();
    }

    /**
     * Start the quiz
     */
    private void startQuiz() {
        // Hide results if shown
        quizResultCard.setVisibility(View.GONE);
        
        // Reset state
        isQuizCompleted = false;
        isReviewMode = false;
        correctAnswers = 0;
        score = 0;
        
        // Show first question
        questionViewPager.setCurrentItem(0, false);
        
        // Enable swiping between questions
        questionViewPager.setUserInputEnabled(true);
        
        // Record start time
        quizStartTime = System.currentTimeMillis();
        
        // Start timer
        startQuizTimer();
        
        // Record quiz attempt in Firestore
        recordQuizAttempt();
    }

    /**
     * Record quiz attempt in Firestore
     */
    private void recordQuizAttempt() {
        String userId = getCurrentUserId();
        
        courseService.startQuizAttempt(userId, quiz.getId(), 
            attempt -> {
                // Successfully recorded attempt
            },
            e -> {
                // Failed to record attempt (continue anyway)
                Toast.makeText(this, R.string.failed_to_record_attempt, Toast.LENGTH_SHORT).show();
            }
        );
    }

    /**
     * Start the quiz countdown timer
     */
    private void startQuizTimer() {
        if (quizTimer != null) {
            quizTimer.cancel();
        }
        
        quizTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateTimeRemaining();
            }
            
            @Override
            public void onFinish() {
                timeRemaining = 0;
                updateTimeRemaining();
                
                // Time's up, submit quiz
                submitQuiz();
            }
        }.start();
    }

    /**
     * Update time remaining display
     */
    private void updateTimeRemaining() {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeRemaining),
                TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60);
        
        timeRemainingTextView.setText(getString(R.string.time_remaining, formattedTime));
    }

    /**
     * Update question progress display
     */
    private void updateQuestionProgress() {
        if (totalQuestions > 0) {
            // Update text
            questionProgressTextView.setText(getString(R.string.question_progress, 
                    currentQuestionIndex + 1, totalQuestions));
            
            // Update progress bar
            int progress = (int) (((float) (currentQuestionIndex + 1) / totalQuestions) * 100);
            quizProgressBar.setProgress(progress);
        }
    }

    /**
     * Submit quiz answers
     */
    private void submitQuiz() {
        // Stop timer
        if (quizTimer != null) {
            quizTimer.cancel();
        }
        
        // Calculate score
        calculateScore();
        
        // Record completion time
        long completionTime = System.currentTimeMillis() - quizStartTime;
        
        // Quiz is now completed
        isQuizCompleted = true;
        
        // Check if passed
        isPassed = score >= quiz.getPassingScore();
        
        // Show results
        showQuizResults(isPassed ? RESULT_PASS : RESULT_FAIL);
        
        // Submit results to Firestore
        submitQuizResults(completionTime);
    }

    /**
     * Calculate quiz score
     */
    private void calculateScore() {
        correctAnswers = 0;
        
        // Count correct answers
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            String userAnswer = questionAdapter.getUserAnswer(i);
            
            if (userAnswer != null && userAnswer.equals(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }
        
        // Calculate percentage score
        score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
    }

    /**
     * Submit quiz results to Firestore
     */
    private void submitQuizResults(long completionTime) {
        String userId = getCurrentUserId();
        
        // Create answer map
        Map<String, String> userAnswers = new HashMap<>();
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            String userAnswer = questionAdapter.getUserAnswer(i);
            userAnswers.put(question.getId(), userAnswer != null ? userAnswer : "");
        }
        
        // Submit attempt
        courseService.submitQuizAttempt(
            userId, 
            quiz.getId(), 
            score,
            correctAnswers,
            TimeUnit.MILLISECONDS.toSeconds(completionTime),
            userAnswers,
            isPassed,
            attempt -> {
                // Successfully recorded results
                
                // If passed, mark lesson as complete
                if (isPassed) {
                    // Don't actually complete yet, let user review first
                    actionButton.setText(R.string.continue_lesson);
                    actionButton.setEnabled(true);
                } else {
                    // Not passed, allow retry
                    actionButton.setText(R.string.retry_quiz);
                    actionButton.setEnabled(true);
                }
            },
            e -> {
                // Failed to record results
                Toast.makeText(this, R.string.failed_to_submit_results, Toast.LENGTH_SHORT).show();
            }
        );
    }

    /**
     * Show quiz results screen
     */
    private void showQuizResults(int resultType) {
        // Disable question swiping
        questionViewPager.setUserInputEnabled(false);
        
        // Hide questions, show results
        questionViewPager.setVisibility(View.GONE);
        quizResultCard.setVisibility(View.VISIBLE);
        
        // Set result info based on type
        if (resultType == RESULT_PASS) {
            resultIconImageView.setImageResource(R.drawable.ic_success);
            resultTitleTextView.setText(R.string.quiz_passed);
            resultMessageTextView.setText(getString(R.string.quiz_passed_message, score));
            continueButton.setText(R.string.continue_learning);
        } else {
            resultIconImageView.setImageResource(R.drawable.ic_failure);
            resultTitleTextView.setText(R.string.quiz_failed);
            resultMessageTextView.setText(getString(R.string.quiz_failed_message, score, quiz.getPassingScore()));
            continueButton.setText(R.string.back_to_course);
        }
        
        // Set score details
        scoreNumberTextView.setText(getString(R.string.percent_value, score));
        correctAnswersTextView.setText(getString(R.string.fraction, correctAnswers, totalQuestions));
        
        // Calculate time spent
        long timeSpent = timerDuration - timeRemaining;
        String formattedTimeSpent = String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeSpent),
                TimeUnit.MILLISECONDS.toSeconds(timeSpent) % 60);
        timeSpentTextView.setText(formattedTimeSpent);
    }

    /**
     * Show review mode
     */
    private void showReviewMode() {
        isReviewMode = true;
        
        // Hide result card
        quizResultCard.setVisibility(View.GONE);
        
        // Show questions in review mode
        questionViewPager.setVisibility(View.VISIBLE);
        
        // Update adapter for review mode
        questionAdapter.setReviewMode(true);
        
        // Enable swiping between questions
        questionViewPager.setUserInputEnabled(true);
        
        // Show first question
        questionViewPager.setCurrentItem(0, false);
    }

    @Override
    protected void onActionButtonClicked() {
        if (isQuizCompleted) {
            if (isPassed) {
                // Already passed, navigate to next lesson
                navigateToNextLesson();
            } else {
                // Failed, restart quiz
                startQuiz();
            }
        } else {
            // Quiz in progress, submit answers
            submitQuiz();
        }
    }

    @Override
    protected void updateResourcesView() {
        // Quiz doesn't show resources
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save quiz state
        outState.putBoolean("is_quiz_completed", isQuizCompleted);
        outState.putBoolean("is_review_mode", isReviewMode);
        outState.putInt("current_question_index", currentQuestionIndex);
        outState.putInt("correct_answers", correctAnswers);
        outState.putInt("score", score);
        outState.putBoolean("is_passed", isPassed);
        outState.putLong("time_remaining", timeRemaining);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Pause timer if quiz is in progress
        if (!isQuizCompleted && quizTimer != null) {
            quizTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Resume timer if quiz is in progress
        if (!isQuizCompleted && timeRemaining > 0) {
            startQuizTimer();
        }
    }

    @Override
    protected void onDestroy() {
        // Cancel timer
        if (quizTimer != null) {
            quizTimer.cancel();
        }
        
        super.onDestroy();
    }
}