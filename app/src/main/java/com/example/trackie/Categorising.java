package com.example.trackie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Categorising {

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put("Food", Arrays.asList(
                "aldi", "tesco", "lidl", "waitrose", "wholefoods",
                "groceries", "fruit", "vegetables", "eggs", "milk",
                "bread", "cheese"
        ));
        CATEGORY_KEYWORDS.put("Eating Out", Arrays.asList(
                "mcdonald's", "kfc", "pizza", "starbucks", "nando's",
                "cafe", "restaurant", "meal", "coffee", "dinner",
                "lunch", "takeaway"
        ));
        CATEGORY_KEYWORDS.put("Health and Beauty", Arrays.asList(
                "boots", "superdrug", "sephora", "lush", "thebodyshop",
                "skincare", "haircut", "makeup", "nails", "spa", "massage"
        ));
        CATEGORY_KEYWORDS.put("Shopping", Arrays.asList(
                "zara", "h&m", "primark", "topshop", "amazon", "ebay",
                "apple", "purchase", "electronics", "clothes",
                "deichmann", "shoes", "footwear","crocs","newlook"
        ));
        CATEGORY_KEYWORDS.put("Pets", Arrays.asList(
                "animal", "treats"
        ));
        CATEGORY_KEYWORDS.put("Other", Arrays.asList(
                "charity", "donation", "gift", "subscription", "membership",
                "fee", "refund"
        ));
    }

    public static String classify(String text) {
        String lowerText = text.toLowerCase();

// go through each category and its key words and return  category found
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
// if no words match it will be classed as other 
        return "Other";
    }
}
