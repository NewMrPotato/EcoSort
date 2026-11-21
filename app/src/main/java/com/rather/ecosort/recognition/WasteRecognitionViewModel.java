package com.rather.ecosort.recognition;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.rather.ecosort.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WasteRecognitionViewModel extends AndroidViewModel {
    private static final String PHOTO_PATH = "recognition_photo_path";
    private final SavedStateHandle savedState;
    private final MutableLiveData<State> state = new MutableLiveData<>(
            new State(null, null, false, 0));
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private WasteClassifier classifier;
    private File currentPhoto;
    private volatile boolean cleared;

    public WasteRecognitionViewModel(@NonNull Application application, SavedStateHandle savedState) {
        super(application);
        this.savedState = savedState;
        String path = savedState.get(PHOTO_PATH);
        if (path != null) {
            File restored = new File(path);
            if (application.getCacheDir().equals(restored.getParentFile()) && restored.isFile()) {
                currentPhoto = restored;
                start(null);
            } else {
                savedState.remove(PHOTO_PATH);
                state.setValue(new State(null, null, false, R.string.recognition_image_error));
            }
        }
    }

    public LiveData<State> state() {
        return state;
    }

    public boolean hasPhoto() {
        return currentPhoto != null || state.getValue().loading;
    }

    public void analyze(Uri source) {
        if (source != null && !state.getValue().loading) {
            start(source);
        }
    }

    private void start(Uri source) {
        Bitmap previousPhoto = state.getValue().photo;
        state.setValue(new State(previousPhoto, null, true, 0));
        worker.execute(() -> {
            File photoFile = source == null ? currentPhoto : null;
            Bitmap photo = null;
            WastePrediction prediction = null;
            int error = 0;
            try {
                if (source != null) {
                    photoFile = PhotoReader.copy(getApplication().getContentResolver(), source,
                            getApplication().getCacheDir());
                }
                photo = PhotoReader.decode(photoFile);
                if (classifier == null) {
                    classifier = new WasteClassifier(getApplication().getAssets());
                }
                prediction = classifier.classify(photo);
            } catch (WasteClassifier.UnclearImageException exception) {
                error = R.string.recognition_low_detail;
            } catch (IOException | RuntimeException | LinkageError exception) {
                error = photo == null ? R.string.recognition_image_error : R.string.recognition_model_error;
                Log.e("WasteRecognition", "Photo recognition failed", exception);
            }
            File completedFile = photoFile;
            Bitmap completedPhoto = photo;
            State completedState = new State(photo == null ? previousPhoto : photo,
                    prediction, false, error);
            mainThread.post(() -> {
                if (cleared) {
                    if (completedPhoto != null) {
                        completedPhoto.recycle();
                    }
                    if (source != null && completedFile != null) {
                        completedFile.delete();
                    }
                    return;
                }
                if (completedPhoto != null) {
                    if (currentPhoto != null && !currentPhoto.equals(completedFile)) {
                        currentPhoto.delete();
                    }
                    currentPhoto = completedFile;
                    savedState.set(PHOTO_PATH, currentPhoto.getPath());
                } else if (source != null && completedFile != null) {
                    completedFile.delete();
                }
                state.setValue(completedState);
            });
        });
    }

    @Override
    protected void onCleared() {
        cleared = true;
        worker.execute(() -> {
            if (classifier != null) {
                classifier.close();
            }
            if (currentPhoto != null) {
                currentPhoto.delete();
            }
        });
        worker.shutdown();
    }

    public static final class State {
        public final Bitmap photo;
        public final WastePrediction prediction;
        public final boolean loading;
        public final int errorResource;

        private State(Bitmap photo, WastePrediction prediction, boolean loading, int errorResource) {
            this.photo = photo;
            this.prediction = prediction;
            this.loading = loading;
            this.errorResource = errorResource;
        }
    }
}
