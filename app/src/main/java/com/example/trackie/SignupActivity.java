package com.example.trackie;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    TextInputEditText firstNameEditText, lastNameEditText, emailEditText, dobEditText, passwordEditText, confirmPasswordEditText;
    Button signUpButton;
    TextView loginPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        signUpButton = findViewById(R.id.signUpButton);
        loginPrompt = findViewById(R.id.loginPrompt);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.signUpEmailEditText);
        dobEditText = findViewById(R.id.dobEditText);
        passwordEditText = findViewById(R.id.signUpPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SignupActivity.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Month is 0-based
                            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            dobEditText.setText(date);
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });


        loginPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

    }

    private void registerUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                dob.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Prepare user data
                        Map<String, Object> user = new HashMap<>();
                        user.put("firstName", firstName);
                        user.put("lastName", lastName);
                        user.put("email", email);
                        user.put("dob", dob);

                        // Save to Firestore
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("SignupActivity", "User saved with ID: " + uid);
                                    Toast.makeText(SignupActivity.this, "User registered", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(SignupActivity.this, HostActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignupActivity.this, "Firestore write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e("SignupActivity", "Error writing to Firestore", e);
                                });

                    } else {
                        Toast.makeText(SignupActivity.this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("SignupActivity", "Auth failure: ", task.getException());
                    }
                });
    }
}