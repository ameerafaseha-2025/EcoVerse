
package com.example.umecoverse;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateChallengeActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etTarget;
    private Button btnDeadline;
    private String selectedDeadline = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        etTitle = findViewById(R.id.etChallengeTitle);
        etDescription = findViewById(R.id.etChallengeDescription);
        etTarget = findViewById(R.id.etChallengeTarget);

        btnDeadline = findViewById(R.id.btnSelectDeadline);

        btnDeadline.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        selectedDeadline =
                                day + "/" + (month + 1) + "/" + year;

                        btnDeadline.setText(selectedDeadline);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePicker.getDatePicker().setMinDate(
                    System.currentTimeMillis()
            );

            datePicker.show();
        });

        findViewById(R.id.btnSubmitChallenge)
                .setOnClickListener(v -> submitChallenge());
    }

    private void submitChallenge() {

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String targetText = etTarget.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Please enter a challenge title");
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Please enter a description");
            return;
        }

        if (targetText.isEmpty()) {
            etTarget.setError("Please enter a target");
            return;
        }

        int target;

        try {
            target = Integer.parseInt(targetText);
        } catch (NumberFormatException e) {
            etTarget.setError("Enter a valid number");
            return;
        }

        if (target <= 0) {
            etTarget.setError("Target must be greater than zero");
            return;
        }

        if (selectedDeadline.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage("Please select a deadline.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Challenge Preview")
                .setMessage(
                        "Title: " + title +
                                "\n\nDescription: " + description +
                                "\n\nTarget: " + target + " actions" +
                                "\n\nDeadline: " + selectedDeadline +
                                "\n\nThis is a prototype. The challenge will not be published."
                )
                .setPositiveButton("Confirm Demo", (dialog, which) -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Demo Completed!")
                            .setMessage(
                                    "Challenge creation simulated successfully."
                            )
                            .setPositiveButton("OK", (d, w) -> finish())
                            .show();
                })
                .setNegativeButton("Edit", null)
                .show();
    }
}
