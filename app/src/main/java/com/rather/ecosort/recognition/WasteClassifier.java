package com.rather.ecosort.recognition;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class WasteClassifier implements Closeable {
    private static final int IMAGE_SIZE = 224;
    private static final String MODEL_ASSET = "models/waste_mobilenet_v2_fp16.tflite";
    private final Interpreter interpreter;
    private final ByteBuffer input = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3 * 4)
            .order(ByteOrder.nativeOrder());

    public WasteClassifier(AssetManager assets) throws IOException {
        try (BufferedReader labels = new BufferedReader(new InputStreamReader(
                assets.open("models/waste_labels.txt"), StandardCharsets.UTF_8))) {
            for (WasteCategory category : WasteCategory.values()) {
                if (!category.modelLabel.equals(labels.readLine())) {
                    throw new IOException("Model label order mismatch");
                }
            }
            if (labels.readLine() != null) {
                throw new IOException("Unexpected model labels");
            }
        }
        try (AssetFileDescriptor file = assets.openFd(MODEL_ASSET);
             FileInputStream stream = new FileInputStream(file.getFileDescriptor())) {
            ByteBuffer model = stream.getChannel().map(FileChannel.MapMode.READ_ONLY,
                    file.getStartOffset(), file.getDeclaredLength());
            interpreter = new Interpreter(model, new Interpreter.Options().setNumThreads(2));
        }
        if (!Arrays.equals(interpreter.getInputTensor(0).shape(), new int[]{1, 224, 224, 3})
                || !Arrays.equals(interpreter.getOutputTensor(0).shape(), new int[]{1, 10})
                || interpreter.getInputTensor(0).dataType() != DataType.FLOAT32
                || interpreter.getOutputTensor(0).dataType() != DataType.FLOAT32) {
            interpreter.close();
            throw new IOException("Unsupported waste model tensors");
        }
    }

    public WastePrediction classify(Bitmap photo) throws UnclearImageException {
        Bitmap resized = Bitmap.createScaledBitmap(photo, IMAGE_SIZE, IMAGE_SIZE, true);
        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        resized.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);
        if (resized != photo) {
            resized.recycle();
        }
        input.rewind();
        double sum = 0;
        double squaredSum = 0;
        for (int pixel : pixels) {
            int red = (pixel >> 16) & 0xff;
            int green = (pixel >> 8) & 0xff;
            int blue = pixel & 0xff;
            input.putFloat(red / 127.5f - 1);
            input.putFloat(green / 127.5f - 1);
            input.putFloat(blue / 127.5f - 1);
            double brightness = 0.299 * red + 0.587 * green + 0.114 * blue;
            sum += brightness;
            squaredSum += brightness * brightness;
        }
        double mean = sum / pixels.length;
        if (squaredSum / pixels.length - mean * mean < 25) {
            throw new UnclearImageException();
        }
        input.rewind();
        float[][] output = new float[1][WasteCategory.values().length];
        interpreter.run(input, output);
        return new WastePrediction(output[0]);
    }

    @Override
    public void close() {
        interpreter.close();
    }

    public static final class UnclearImageException extends Exception {
    }
}
