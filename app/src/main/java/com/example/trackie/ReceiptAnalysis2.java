package com.example.trackie;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptAnalysis2 {

    public static Map<String, String> analyseText(String ocrText) {
        Log.d("ReceiptAnalysis", "analyseText() called with text:\n" + ocrText);

        Map<String, String> extractedData = new HashMap<>();

        String date = extractDate(ocrText);
        String total = extractTotal(ocrText);
        if (total == null) {
            total = extractTotalFromText(ocrText);
        }


        if (date != null) {
            extractedData.put("date", date);
            Log.d("ReceiptAnalysis", "Extracted date: " + date);
        }

        if (total != null) {
            extractedData.put("total", total);
            Log.d("ReceiptAnalysis", "Extracted total: " + total);
        }

        return extractedData;
    }

    private static String extractDate(String text) {
        String datePattern = "\\b(\\d{1,2}[\\/\\-. ]\\d{1,2}[\\/\\-. ]\\d{2,4}" +
                "|\\d{4}[\\/\\-. ]\\d{1,2}[\\/\\-. ]\\d{1,2}" +
                "|\\d{1,2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[a-z]* \\d{2,4}" +
                "|(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[a-z]* \\d{1,2}, \\d{2,4})\\b";

        Pattern pattern = Pattern.compile(datePattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private static String extractTotal(String text) {
        String totalPattern = "(?i)(total|amount|grand total)[:\\s]*[£$€]?[\\s]*([0-9]+\\.[0-9]{2})";
        Pattern pattern = Pattern.compile(totalPattern);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String extractTotalFromText(String text) {
        if (text == null || text.isEmpty()) return null;

        Pattern pattern = Pattern.compile("£\\s?(\\d+[\\.\\,]\\d{2})");
        Matcher matcher = pattern.matcher(text);

        double highestValue = -1;
        String bestMatch = null;

        while (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", ".");
            try {
                double value = Double.parseDouble(amountStr);
                if (value > highestValue) {
                    highestValue = value;
                    bestMatch = "£" + amountStr;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return bestMatch;
    }

}
