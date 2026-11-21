package com.rather.ecosort.recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.exifinterface.media.ExifInterface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PhotoReaderTest {
    @Test
    public void handlesAllEightExifOrientations() throws Exception {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File source = File.createTempFile("exif-test-", ".jpg", target.getCacheDir());
        Bitmap original = Bitmap.createBitmap(80, 60, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < 60; y++) {
            for (int x = 0; x < 80; x++) {
                original.setPixel(x, y, 0xff000000 | (x * 3 << 16) | (y * 4 << 8));
            }
        }
        try (FileOutputStream file = new FileOutputStream(source)) {
            original.compress(Bitmap.CompressFormat.JPEG, 100, file);
        }
        original.recycle();
        Bitmap decoded = BitmapFactory.decodeFile(source.getPath());
        try {
            int x = 17;
            int y = 23;
            int[][] locations = {{x, y}, {79 - x, y}, {79 - x, 59 - y}, {x, 59 - y},
                    {y, x}, {59 - y, x}, {59 - y, 79 - x}, {y, 79 - x}};
            for (int orientation = 1; orientation <= 8; orientation++) {
                ExifInterface exif = new ExifInterface(source);
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation));
                exif.saveAttributes();
                Bitmap actual = PhotoReader.decode(source);
                try {
                    assertEquals(orientation >= 5 ? 60 : 80, actual.getWidth());
                    assertEquals(orientation >= 5 ? 80 : 60, actual.getHeight());
                    assertEquals("EXIF " + orientation, decoded.getPixel(x, y),
                            actual.getPixel(locations[orientation - 1][0], locations[orientation - 1][1]));
                } finally {
                    actual.recycle();
                }
            }
        } finally {
            decoded.recycle();
            source.delete();
        }
    }

    @Test
    public void samplesLargePhotosToBoundMemoryUse() throws Exception {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File source = File.createTempFile("large-test-", ".jpg", target.getCacheDir());
        Bitmap original = Bitmap.createBitmap(2049, 1600, Bitmap.Config.ARGB_8888);
        try (FileOutputStream file = new FileOutputStream(source)) {
            original.compress(Bitmap.CompressFormat.JPEG, 90, file);
        }
        original.recycle();
        try {
            Bitmap decoded = PhotoReader.decode(source);
            assertTrue(Math.max(decoded.getWidth(), decoded.getHeight()) <= 1024);
            decoded.recycle();
        } finally {
            source.delete();
        }
    }
}
