package com.rather.ecosort.recognition;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rather.ecosort.MainActivity;
import com.rather.ecosort.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RecognitionFlowTest {
    @Test
    public void resultSurvivesActivityRecreation() throws Exception {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context tests = InstrumentationRegistry.getInstrumentation().getContext();
        File source = File.createTempFile("flow-test-", ".jpg", target.getCacheDir());
        try (InputStream image = tests.getAssets().open("waste/cardboard1.jpg");
             FileOutputStream output = new FileOutputStream(source)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = image.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            WasteRecognitionViewModel.State state = awaitResult(scenario, Uri.fromFile(source));
            assertEquals(0, state.errorResource);
            assertEquals(WasteCategory.CARDBOARD, state.prediction.topCategory());
            scenario.recreate();
            scenario.onActivity(activity -> {
                TextView title = activity.findViewById(R.id.recognition_result_title);
                assertTrue(title.getText().toString().contains(activity.getString(R.string.waste_cardboard)));
                assertEquals(View.VISIBLE, activity.findViewById(R.id.recognition_result).getVisibility());
                assertTrue(activity.findViewById(R.id.recognition_choose_photo).isEnabled());
            });
        } finally {
            source.delete();
        }
    }

    @Test
    public void corruptPhotoShowsAnErrorAndAllowsAnotherPhoto() throws Exception {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File source = File.createTempFile("invalid-flow-", ".jpg", target.getCacheDir());
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            WasteRecognitionViewModel.State state = awaitResult(scenario, Uri.fromFile(source));
            assertNull(state.prediction);
            assertEquals(R.string.recognition_image_error, state.errorResource);
            scenario.onActivity(activity -> {
                assertEquals(View.GONE, activity.findViewById(R.id.recognition_result).getVisibility());
                assertTrue(activity.findViewById(R.id.recognition_choose_photo).isEnabled());
            });
        } finally {
            source.delete();
        }
    }

    private WasteRecognitionViewModel.State awaitResult(ActivityScenario<MainActivity> scenario,
                                                         Uri photo) throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<WasteRecognitionViewModel.State> result = new AtomicReference<>();
        scenario.onActivity(activity -> {
            activity.findViewById(R.id.main_trash_detection_button_constraint_layout).performClick();
            WasteRecognitionViewModel model = new ViewModelProvider(activity)
                    .get(WasteRecognitionViewModel.class);
            model.analyze(photo);
            model.state().observe(activity, state -> {
                if (!state.loading) {
                    result.set(state);
                    completed.countDown();
                }
            });
        });
        assertTrue("Recognition did not finish", completed.await(45, TimeUnit.SECONDS));
        return result.get();
    }
}
