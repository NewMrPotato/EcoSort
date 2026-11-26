package com.rather.ecosort.recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WasteClassifierTest {
    @Test
    public void bundledModelRunsOnSixRepresentativeMaterials() throws Exception {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context tests = InstrumentationRegistry.getInstrumentation().getContext();
        byte[] data;
        try (InputStream file = tests.getAssets().open("waste/fixtures.json");
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = file.read(buffer)) != -1) {
                bytes.write(buffer, 0, length);
            }
            data = bytes.toByteArray();
        }
        JSONArray fixtures = new JSONArray(new String(data, StandardCharsets.UTF_8));
        try (WasteClassifier classifier = new WasteClassifier(target.getAssets())) {
            for (int index = 0; index < fixtures.length(); index++) {
                JSONObject fixture = fixtures.getJSONObject(index);
                try (InputStream image = tests.getAssets().open("waste/" + fixture.getString("file"))) {
                    Bitmap photo = BitmapFactory.decodeStream(image);
                    try {
                        WastePrediction result = classifier.classify(photo);
                        assertEquals(fixture.getString("file"),
                                fixture.getString("label"), result.topCategory().modelLabel);
                        assertTrue(fixture.getString("file"), result.isConfident());
                    } finally {
                        photo.recycle();
                    }
                }
            }
        }
    }

    @Test
    public void featurelessPhotoIsRejected() throws Exception {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap photo = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
        photo.eraseColor(Color.WHITE);
        try (WasteClassifier classifier = new WasteClassifier(target.getAssets())) {
            try {
                classifier.classify(photo);
                fail("A blank photo must not get a confident material label");
            } catch (WasteClassifier.UnclearImageException expected) {
                assertNotNull(expected);
            }
        } finally {
            photo.recycle();
        }
    }
}
