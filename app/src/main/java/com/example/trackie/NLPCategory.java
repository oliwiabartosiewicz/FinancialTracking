
package com.example.trackie;

import android.content.Context;
import android.util.Log;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import java.io.IOException;
import java.io.InputStream;

public class NLPCategory {
    private DocumentCategorizerME categoriser;

    public NLPCategory(Context context) {
        try {
            InputStream modelIn = context.getAssets().open("en-doccat.bin");
            DoccatModel model = new DoccatModel(modelIn);
            categoriser = new DocumentCategorizerME(model);
        } catch (IOException e) {
            Log.e("NlpCategory", "Failed to load categorisation model", e);
        }
    }

    public String classify(String text) {
        if (categoriser == null || text == null || text.isEmpty()) {
            return "uncategorised";
        }

        //split on one or more whitespace characters
        String[] tokens = text.split("\\s+");
        double[] outcomes = categoriser.categorize(tokens);
        return categoriser.getBestCategory(outcomes);
    }
}



