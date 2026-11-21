package com.rather.ecosort.recognition;

import org.junit.Test;

import static org.junit.Assert.*;

public class WastePredictionTest {
    @Test
    public void ranksModelLabelsWithoutChangingTheirOrder() {
        float[] scores = {0.01f, 0.01f, 0.02f, 0.01f, 0.8f, 0.01f, 0.04f, 0.08f, 0.01f, 0.01f};
        WastePrediction result = new WastePrediction(scores);
        scores[4] = 0;
        assertEquals(WasteCategory.GLASS, result.topCategory());
        assertEquals(WasteCategory.PLASTIC, result.topThree().get(1));
        assertEquals(0.8f, result.score(WasteCategory.GLASS), 0.0001f);
        assertTrue(result.isConfident());
    }

    @Test
    public void ambiguousProbabilitiesRemainUncertain() {
        float[] scores = new float[10];
        scores[4] = 0.55f;
        scores[7] = 0.45f;
        assertFalse(new WastePrediction(scores).isConfident());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsIncorrectClassCount() {
        new WastePrediction(new float[6]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNanFromAnInvalidModel() {
        float[] scores = new float[10];
        scores[0] = Float.NaN;
        new WastePrediction(scores);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnnormalizedLogits() {
        new WastePrediction(new float[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    }
}
