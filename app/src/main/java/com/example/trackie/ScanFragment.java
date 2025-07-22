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
import androidx.fragment.app.Fragment;

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
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Toast.makeText(getContext(), "Picture taken!", Toast.LENGTH_SHORT).show();

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

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
}
