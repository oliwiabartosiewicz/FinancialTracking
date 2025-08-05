package com.example.trackie;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {


    public HomeFragment() {
    }

    private PieChart pieChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        pieChart = view.findViewById(R.id.pieChart);

        setupPieChart();

        return view;
    }

    private void setupPieChart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("receipts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Float> categorySums = new HashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String category = doc.getString("category");
                        String totalStr = doc.getString("total");

                        //if no cat then will be skipped
                        if (category == null || totalStr == null) {
                            Log.w("HomeFragment", "Missing category or total in document: " + doc.getId());
                            continue;
                        }

                        // Remove currency symbol from total
                        totalStr = totalStr.replace("£", "").trim();
                        try {
                            float total = Float.parseFloat(totalStr);
                            float currentSum = categorySums.getOrDefault(category, 0f);
                            categorySums.put(category, currentSum + total);
                        } catch (NumberFormatException e) {
                            Log.w("HomeFragment", "Invalid total value in: " + doc.getId() + " - " + totalStr);
                        }
                    }
                    List<PieEntry> entries = new ArrayList<>(); //list to create each pie chart slice

                    // Loop through each category and its total spend
                    for (Map.Entry<String, Float> entry : categorySums.entrySet()) {
                        entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                                                                    //cat name
                    }

                    if (!entries.isEmpty()) {
                        PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
                        dataSet.setColors(new int[]{
                                ContextCompat.getColor(requireContext(), R.color.blue),
                                ContextCompat.getColor(requireContext(), R.color.pastel_blue),
                                ContextCompat.getColor(requireContext(), R.color.royal_blue),
                                ContextCompat.getColor(requireContext(), R.color.sky_blue),
                                ContextCompat.getColor(requireContext(), R.color.cobalt_blue),
                                ContextCompat.getColor(requireContext(), R.color.bright_blue),
                        });

                        pieChart.setNoDataText("Loading...");
                        dataSet.setValueTextSize(14f);
                        dataSet.setValueTextColor(Color.BLACK);

                        PieData pieData = new PieData(dataSet);
                        pieChart.setData(pieData);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.setEntryLabelColor(Color.BLACK);
                        pieChart.setEntryLabelTextSize(12f);
                        pieChart.setCenterText("Total by Category");
                        pieChart.setCenterTextSize(18f);
                        pieChart.animateY(1000);
                        pieChart.invalidate();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Failed to load receipts", e);
                });

                }
    }



