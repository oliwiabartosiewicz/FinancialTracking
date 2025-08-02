
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

            //trained model- the below "en-doccat.bin" has been created by ChatGPT
            InputStream modelIn = context.getAssets().open("en-doccat.bin");
            DoccatModel model = new DoccatModel(modelIn);
            categoriser = new DocumentCategorizerME(model);
        } catch (IOException e) {
            Log.e("NlpCategory", "Categorisation model not loaded", e);
        }
    }

    public String classify(String text) {
        if (categoriser == null || tokenizer == null || text == null || text.isEmpty()) {
            return "uncategorised";
        }


        String[] tokens = tokenizer.tokenize(text);
        double[] outcomes = categoriser.categorize(tokens);
        return categoriser.getBestCategory(outcomes);
    }
}



