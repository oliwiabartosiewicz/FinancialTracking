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
}
