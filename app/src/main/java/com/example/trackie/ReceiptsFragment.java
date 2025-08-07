package com.example.trackie;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


public class ReceiptsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReceiptAdapter adapter;
    private List<Receipt> receiptList;

    public ReceiptsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_receipts, container, false);

        recyclerView = view.findViewById(R.id.receiptRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        receiptList = new ArrayList<>();
        adapter = new ReceiptAdapter(getContext(), receiptList);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .collection("receipts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String date = doc.getString("date");
                            String total = doc.getString("total");
                            String imageUrl = doc.getString("imageUrl");
                            String category = doc.getString("category");


                            Log.d("ReceiptsFragment", "Firestore doc imageUrl: " + imageUrl);

                            if (date != null && total != null && imageUrl != null && category != null) {
                                receiptList.add(new Receipt(date, total, imageUrl, category));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load receipts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }
}
