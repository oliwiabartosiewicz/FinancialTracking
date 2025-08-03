
package com.example.trackie;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class NLPCategory {
    private DocumentCategorizerME categoriser;
    private TokenizerME tokenizer;

    public NLPCategory(Context context) {
        try {
            AssetManager assetManager = context.getAssets();

            //token
            InputStream tokenModelIn = assetManager.open("en-token.bin");
            TokenizerModel tokenModel = new TokenizerModel(tokenModelIn);
            tokenizer = new TokenizerME(tokenModel);
            Log.d("NLPCategory", "Tokenizer model loaded");

            //trained model- "en-doccat.bin"the below  has been created by ChatGPT
            InputStream modelIn = context.getAssets().open("en-doccat.bin");
            DoccatModel model = new DoccatModel(modelIn);
            categoriser = new DocumentCategorizerME(model);
            Log.d("NLPCategory", "Doccat model loaded");
        } catch (IOException e) {
            Log.e("NlpCategory", "Categorisation model not loaded", e);
        }
    }

    public String classify(String text) {
        if (tokenizer == null || categoriser == null || text == null || text.isEmpty()) {
            Log.d("NLPCategory", "Tokenizer or categoriser not ready, or text empty");
            return "uncategorised";
        }

        String[] tokens = tokenizer.tokenize(text);
        Log.d("NLPCategory", "Tokens: " + Arrays.toString(tokens));

        double[] outcomes = categoriser.categorize(tokens);

        for (int i = 0; i < categoriser.getNumberOfCategories(); i++) {
            String cat = categoriser.getCategory(i);
            double score = outcomes[i];
            Log.d("NLPCategory", "Category: " + cat + ", Score: " + score);
        }

        String bestCategory = categoriser.getBestCategory(outcomes);
        Log.d("NLPCategory", "Best category: " + bestCategory);

        return bestCategory;
    }

}



