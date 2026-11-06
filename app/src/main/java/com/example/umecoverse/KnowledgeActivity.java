
package com.example.umecoverse;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ScrollView;

public class KnowledgeActivity extends AppCompatActivity {

    private TextView tvQuizStatus;
    private TextView tvKnowledgeBadge;
    private TextView tvChallengeAccess;

    private int questionIndex = 0;
    private int score = 0;

    private final String[] questions = {
            "Which action helps reduce plastic waste?",
            "What does SDG 13 focus on?",
            "Which item can commonly be recycled?"
    };

    private final String[][] answers = {
            {
                    "Using disposable plastic bottles",
                    "Using a reusable bottle",
                    "Throwing plastic into rivers"
            },
            {
                    "Climate Action",
                    "Quality Education",
                    "Zero Hunger"
            },
            {
                    "Food-contaminated tissue",
                    "Used chewing gum",
                    "Clean aluminium can"
            }
    };

    private final int[] correctAnswers = {1, 0, 2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        tvQuizStatus = findViewById(R.id.tvQuizStatus);
        tvKnowledgeBadge = findViewById(R.id.tvKnowledgeBadge);
        tvChallengeAccess = findViewById(R.id.tvChallengeAccess);

        findViewById(R.id.btnStartQuiz).setOnClickListener(v -> {
            questionIndex = 0;
            score = 0;
            showQuestion();
        });
        findViewById(R.id.btnReadSolar).setOnClickListener(v -> {
            showArticle(
                    "Campus Solar Energy",
                    "Solar energy is a renewable energy source that " +
                            "converts sunlight into electricity using solar panels.\n\n" +
                            "University campuses can use solar energy to reduce " +
                            "dependence on fossil fuels and lower carbon emissions.\n\n" +
                            "Students can support clean energy by switching off " +
                            "unused lights and electrical devices.\n\n" +
                            "Related SDG: SDG 7 - Affordable and Clean Energy."
            );
        });

        findViewById(R.id.btnReadWaste).setOnClickListener(v -> {
            showArticle(
                    "Zero-Waste Campus",
                    "A zero-waste campus aims to reduce the amount of " +
                            "waste sent to landfills.\n\n" +
                            "Students can contribute by bringing reusable bottles, " +
                            "avoiding single-use plastics and separating recyclable materials.\n\n" +
                            "Clean paper, aluminium cans and certain plastics " +
                            "can be recycled through suitable collection facilities.\n\n" +
                            "Related SDG: SDG 12 - Responsible Consumption and Production."
            );
        });
    }

    private void showQuestion() {

        if (questionIndex >= questions.length) {
            showResult();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Question " + (questionIndex + 1) + " of 3")
                .setMessage(questions[questionIndex])
                .setCancelable(false)
                .setItems(answers[questionIndex], (dialog, selected) -> {

                    if (selected == correctAnswers[questionIndex]) {
                        score++;
                    }

                    questionIndex++;
                    showQuestion();
                })
                .show();
    }

    private void showResult() {

        boolean passed = score >= 2;

        String message = "Your Score: " + score + "/3\n\n";

        if (passed) {
            message += "Congratulations! You passed the Eco Quiz.";

            tvQuizStatus.setText("Status: Completed");
            tvKnowledgeBadge.setText(
                    "🌍 Eco Knowledge Beginner (Unlocked)"
            );
            tvChallengeAccess.setText(
                    "🔓 High-tier challenges: Quiz requirement met"
            );

        } else {
            message += "Keep learning! Try again to unlock your badge.";

            tvQuizStatus.setText("Status: Try Again");
        }

        new AlertDialog.Builder(this)
                .setTitle(passed ? "Quiz Completed!" : "Quiz Result")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    private void showArticle(String title, String content) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("Close", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}
