package com.example.trackie;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScanFragment extends Fragment {

    private Button takePictureButton, uploadScreenshotButton;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    public ScanFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        takePictureButton = view.findViewById(R.id.takePictureButton);
        uploadScreenshotButton = view.findViewById(R.id.uploadScreenshotButton);
        setupLaunchers();
        takePictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        uploadScreenshotButton.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });
        return view;
    }

    private void setupLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoUri != null) {
                            Toast.makeText(getContext(), "Image saved at: " + photoUri.getPath(), Toast.LENGTH_SHORT).show();
                            runMLKitOCR(photoUri);
                        }
                    }
                }
        );

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Toast.makeText(getContext(), "Photo selected!", Toast.LENGTH_SHORT).show();
                        photoUri = uri;
                        runMLKitOCR(photoUri);
                    }
                }
        );
    }

    private Uri photoUri;

    private void openCamera() {
        File photoFile = new File(requireContext().getExternalFilesDir("Pictures"),
                "photo_" + System.currentTimeMillis() + ".jpg");
        photoUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                photoFile
        );
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cameraLauncher.launch(cameraIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void runMLKitOCR(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(requireContext(), imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        StringBuilder resultText = new StringBuilder();

                        for (Text.TextBlock block : result.getTextBlocks()) {
                            resultText.append(block.getText()).append("\n");
                        }
                        //building OCR text
                        String ocrText = resultText.toString();
                        Log.d("ScanFragment", "OCR Text extracted:\n" + ocrText);

                        //extract category
                        String extractedCategory = Categorising.classify(ocrText);
                        String category = (extractedCategory != null && !extractedCategory.isEmpty()) ? extractedCategory : "Other";
                        Log.d("ScanFragment", " Category: " + category);
                        Toast.makeText(getContext(), "Category: " + category, Toast.LENGTH_SHORT).show();

                        //extract data and time
                        Map<String, String> extracted = ReceiptAnalysis2.analyseText(ocrText);
                        String date  = extracted.get("date");
                        String total = extracted.get("total");

                        Map<String, Object> receiptData = new HashMap<>();
                        receiptData.put("rawText", ocrText);
                        receiptData.put("timestamp", FieldValue.serverTimestamp());
                        receiptData.put("category", category);

                        if (date  != null) receiptData.put("date",  date);
                        if (total != null) receiptData.put("total", total);

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("ScanFragment", "Receipt data: " + receiptData.toString());
                        uploadToStorageAndFirestore(imageUri, receiptData);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "OCR Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToStorageAndFirestore(Uri imageUri, Map<String, Object> receiptData) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (receiptData == null || receiptData.isEmpty()) {
            Log.e("ScanFragment", "receiptData is empty. Aborting upload.");
            if (getContext() != null) {
            Toast.makeText(getContext(), "No text found to save with the receipt.", Toast.LENGTH_LONG).show();}
            return;
        }

        // Add timestamp
        receiptData.put("timestamp", FieldValue.serverTimestamp());

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("receipts/" + userId + "/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        receiptData.put("imageUrl", downloadUri.toString());

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .collection("receipts")
                                .add(receiptData)
                                .addOnSuccessListener(docRef -> {
                                    Log.d("ScanFragment", "Receipt saved with ID: " + docRef.getId());
                                    if (getContext() != null) {
                                    Toast.makeText(getContext(), "Receipt saved!", Toast.LENGTH_SHORT).show();}
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ScanFragment", "Firestore save failed", e);
                                    if (getContext() != null) {
                                    Toast.makeText(getContext(), "Firestore save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();}
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ScanFragment", "Image upload failed", e);
                    if (getContext() != null) {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();}
                });
    }
}