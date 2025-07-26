package com.example.trackie;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private Button takePictureButton, uploadPdfButton, uploadScreenshotButton;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> pdfPickerLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    public ScanFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        takePictureButton = view.findViewById(R.id.takePictureButton);
        uploadPdfButton = view.findViewById(R.id.uploadPdfButton);
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

        uploadPdfButton.setOnClickListener(v -> {
            pdfPickerLauncher.launch("application/pdf");
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
        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Toast.makeText(getContext(), "PDF selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Toast.makeText(getContext(), "Screenshot selected!", Toast.LENGTH_SHORT).show();
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
                        String ocrText = resultText.toString();
                        //Stroing receipts in firestore
                        Map<String, Object> receiptData = new HashMap<>();
                        receiptData.put("rawText", ocrText);
                        receiptData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
                            return;
                        }
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


//receipt upload to firestore and storage of it
    private void uploadToStorageAndFirestore(Uri imageUri, Map<String, Object> receiptData) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                    Toast.makeText(getContext(), "Receipt saved!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Firestore save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }

}
