package com.fast.mentor;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fast.mentor.R;
import com.fast.mentor.QuizQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for quiz questions in ViewPager
 */
public class QuizQuestionAdapter extends RecyclerView.Adapter<QuizQuestionAdapter.QuestionViewHolder> {

    private final Context context;
    private final List<QuizQuestion> questions = new ArrayList<>();
    private final Map<Integer, String> userAnswers = new HashMap<>();
    private boolean isReviewMode = false;

    /**
     * Constructor
     */
    public QuizQuestionAdapter(Context context) {
        this.context = context;
    }

    /**
     * Set questions list
     */
    public void setQuestions(List<QuizQuestion> questions, boolean isReviewMode) {
        this.questions.clear();
        if (questions != null) {
            this.questions.addAll(questions);
        }
        this.isReviewMode = isReviewMode;
        notifyDataSetChanged();
    }

    /**
     * Set review mode
     */
    public void setReviewMode(boolean reviewMode) {
        if (this.isReviewMode != reviewMode) {
            this.isReviewMode = reviewMode;
            notifyDataSetChanged();
        }
    }

    /**
     * Get user answer for a specific question
     */
    @Nullable
    public String getUserAnswer(int position) {
        return userAnswers.get(position);
    }

    /**
     * Get all user answers
     */
    public Map<Integer, String> getAllUserAnswers() {
        return new HashMap<>(userAnswers);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.bind(questions.get(position), position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    /**
     * ViewHolder for quiz questions
     */
    class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView questionNumberTextView;
        private final TextView questionTextView;
        private final ImageView questionImageView;
        private final RadioGroup optionsRadioGroup;
        private final RadioButton option1RadioButton;
        private final RadioButton option2RadioButton;
        private final RadioButton option3RadioButton;
        private final RadioButton option4RadioButton;
        private final LinearLayout feedbackContainer;
        private final TextView feedbackHeaderTextView;
        private final TextView feedbackTextView;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumberTextView = itemView.findViewById(R.id.questionNumberTextView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            questionImageView = itemView.findViewById(R.id.questionImageView);
            optionsRadioGroup = itemView.findViewById(R.id.optionsRadioGroup);
            option1RadioButton = itemView.findViewById(R.id.option1RadioButton);
            option2RadioButton = itemView.findViewById(R.id.option2RadioButton);
            option3RadioButton = itemView.findViewById(R.id.option3RadioButton);
            option4RadioButton = itemView.findViewById(R.id.option4RadioButton);
            feedbackContainer = itemView.findViewById(R.id.feedbackContainer);
            feedbackHeaderTextView = itemView.findViewById(R.id.feedbackHeaderTextView);
            feedbackTextView = itemView.findViewById(R.id.feedbackTextView);
        }

        /**
         * Bind question data to view
         */
        void bind(QuizQuestion question, int position) {
            // Set question number
            questionNumberTextView.setText(String.valueOf(position + 1));
            
            // Set question text
            questionTextView.setText(question.getText());
            
            // Set question image if available
            if (!TextUtils.isEmpty(question.getImageUrl())) {
                questionImageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(question.getImageUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_image)
                                .error(R.drawable.ic_error))
                        .into(questionImageView);
            } else {
                questionImageView.setVisibility(View.GONE);
            }
            
            // Clear previous options
            optionsRadioGroup.clearCheck();
            
            // Set options
            List<String> options = question.getOptions();
            List<RadioButton> optionButtons = new ArrayList<>();
            optionButtons.add(option1RadioButton);
            optionButtons.add(option2RadioButton);
            optionButtons.add(option3RadioButton);
            optionButtons.add(option4RadioButton);
            
            // Hide all option buttons first
            for (RadioButton button : optionButtons) {
                button.setVisibility(View.GONE);
            }
            
            // Show and set text for available options
            for (int i = 0; i < options.size() && i < optionButtons.size(); i++) {
                RadioButton button = optionButtons.get(i);
                button.setVisibility(View.VISIBLE);
                button.setText(options.get(i));
                
                final int optionIndex = i;
                button.setOnClickListener(v -> {
                    // Save user's answer
                    userAnswers.put(position, options.get(optionIndex));
                });
            }
            
            // Restore previous answer if available
            String userAnswer = userAnswers.get(position);
            if (userAnswer != null) {
                int index = options.indexOf(userAnswer);
                if (index >= 0 && index < optionButtons.size()) {
                    optionButtons.get(index).setChecked(true);
                }
            }
            
            // In review mode, show feedback and highlight correct answer
            if (isReviewMode) {
                // Disable radio buttons
                for (RadioButton button : optionButtons) {
                    button.setEnabled(false);
                }
                
                // Show feedback
                feedbackContainer.setVisibility(View.VISIBLE);
                
                // Get user's answer
                String selectedAnswer = userAnswers.get(position);
                boolean isCorrect = selectedAnswer != null && 
                        selectedAnswer.equals(question.getCorrectAnswer());
                
                // Set feedback header
                if (isCorrect) {
                    feedbackHeaderTextView.setText(R.string.correct);
                    feedbackHeaderTextView.setTextColor(Color.parseColor("#3DD598"));
                } else {
                    feedbackHeaderTextView.setText(R.string.incorrect);
                    feedbackHeaderTextView.setTextColor(Color.parseColor("#FF3D71"));
                }
                
                // Set feedback text
                feedbackTextView.setText(question.getFeedback());
                
                // Highlight correct answer
                String correctAnswer = question.getCorrectAnswer();
                int correctIndex = options.indexOf(correctAnswer);
                if (correctIndex >= 0 && correctIndex < optionButtons.size()) {
                    // Use different visual cue for correct answer
                    optionButtons.get(correctIndex).setTextColor(Color.parseColor("#3DD598"));
                }
                
                // If user selected wrong answer, highlight it
                if (!isCorrect && selectedAnswer != null) {
                    int wrongIndex = options.indexOf(selectedAnswer);
                    if (wrongIndex >= 0 && wrongIndex < optionButtons.size()) {
                        optionButtons.get(wrongIndex).setTextColor(Color.parseColor("#FF3D71"));
                    }
                }
            } else {
                // In quiz mode, no feedback
                feedbackContainer.setVisibility(View.GONE);
                
                // Enable radio buttons
                for (RadioButton button : optionButtons) {
                    button.setEnabled(true);
                    button.setTextColor(context.getResources().getColor(R.color.white));
                }
            }
        }
    }
}