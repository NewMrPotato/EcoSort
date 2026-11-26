package com.rather.ecosort.recognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class WastePrediction {
    private final float[] scores;
    private final List<WasteCategory> ranking;

    public WastePrediction(float[] scores) {
        if (scores.length != WasteCategory.values().length) {
            throw new IllegalArgumentException("Unexpected model output length");
        }
        double total = 0;
        for (float score : scores) {
            if (Float.isNaN(score) || Float.isInfinite(score) || score < 0 || score > 1) {
                throw new IllegalArgumentException("Invalid model probability");
            }
            total += score;
        }
        if (Math.abs(total - 1) > 0.01) {
            throw new IllegalArgumentException("Model output is not a probability distribution");
        }
        this.scores = scores.clone();
        List<WasteCategory> ordered = new ArrayList<>(Arrays.asList(WasteCategory.values()));
        Collections.sort(ordered, (first, second) ->
                Float.compare(this.scores[second.ordinal()], this.scores[first.ordinal()]));
        ranking = Collections.unmodifiableList(ordered);
    }

    public WasteCategory topCategory() {
        return ranking.get(0);
    }

    public List<WasteCategory> topThree() {
        return ranking.subList(0, 3);
    }

    public float score(WasteCategory category) {
        return scores[category.ordinal()];
    }

    public boolean isConfident() {
        float topScore = score(ranking.get(0));
        return topScore >= 0.7f && topScore - score(ranking.get(1)) >= 0.2f;
    }
}
