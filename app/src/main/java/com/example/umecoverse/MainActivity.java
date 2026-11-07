
package com.example.umecoverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty()) {
                etEmail.setError("Please enter your UM email");
                etEmail.requestFocus();

            } else if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {
                etEmail.setError("Please enter a valid email address");
                etEmail.requestFocus();

            } else if (password.isEmpty()) {
                etPassword.setError("Please enter your password");
                etPassword.requestFocus();

            } else {
                Toast.makeText(
                        MainActivity.this,
                        "Login successful (Demo)",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        MainActivity.this,
                        HomeActivity.class
                );

                startActivity(intent);
            }
        });
    }
}
