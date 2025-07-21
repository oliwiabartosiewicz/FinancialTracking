package com.example.trackie;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView welcomeTextView;
    private TextInputEditText emailEditText, fullNameEditText, dobEditText;
    private Button editProfileButton, saveChangesButton, changePasswordButton, logoutButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        emailEditText = view.findViewById(R.id.emailEditText);
        fullNameEditText = view.findViewById(R.id.fullNameEditText);
        dobEditText = view.findViewById(R.id.dobEditText);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        saveChangesButton = view.findViewById(R.id.saveChangesButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Disable input fields
        fullNameEditText.setEnabled(false);
        emailEditText.setEnabled(false);
        dobEditText.setEnabled(false);
        saveChangesButton.setVisibility(View.GONE);

        loadUserData();

        editProfileButton.setOnClickListener(v -> {
            fullNameEditText.setEnabled(true);
            saveChangesButton.setVisibility(View.VISIBLE);
        });
        saveChangesButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            if (fullName.isEmpty() || !fullName.contains(" ")) {
                fullNameEditText.setError("Please enter first and last name");
                return;
            }

            String[] nameParts = fullName.split(" ", 2);
            String firstName = nameParts[0];
            String lastName = nameParts[1];

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("firstName", firstName);
                updates.put("lastName", lastName);

                db.collection("users").document(currentUser.getUid())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                            fullNameEditText.setEnabled(false);
                            saveChangesButton.setVisibility(View.GONE);
                            welcomeTextView.setText("Welcome, " + fullName + "!");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show()
                        );
            }
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            requireActivity().finish();
            startActivity(new android.content.Intent(getContext(), MainActivity.class));
        });
//change password

        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String email = documentSnapshot.getString("email");
                            String dob = documentSnapshot.getString("dob");

                            String fullName = firstName + " " + lastName;

                            welcomeTextView.setText("Welcome, " + fullName + "!");
                            fullNameEditText.setText(fullName);
                            emailEditText.setText(email);
                            dobEditText.setText(dob);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
