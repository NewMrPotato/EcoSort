package com.rather.ecosort.recognition;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class PhotoReader {
    private static final long MAX_PHOTO_BYTES = 20 * 1024 * 1024;

    private PhotoReader() {
    }

    public static File copy(ContentResolver resolver, Uri uri, File directory) throws IOException {
        File copy = File.createTempFile("recognition-", ".image", directory);
        try (InputStream source = resolver.openInputStream(uri);
             FileOutputStream target = new FileOutputStream(copy)) {
            if (source == null) {
                throw new IOException("Photo is unavailable");
            }
            byte[] buffer = new byte[8192];
            long total = 0;
            int count;
            while ((count = source.read(buffer)) != -1) {
                total += count;
                if (total > MAX_PHOTO_BYTES) {
                    throw new IOException("Photo exceeds the size limit");
                }
                target.write(buffer, 0, count);
            }
        } catch (IOException | RuntimeException exception) {
            copy.delete();
            throw exception;
        }
        return copy;
    }

    public static Bitmap decode(File file) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        if (options.outWidth < 1 || options.outHeight < 1) {
            throw new IOException("Unsupported or damaged photo");
        }
        options.inSampleSize = 1;
        while (Math.max(options.outWidth, options.outHeight) > 1024L * options.inSampleSize) {
            options.inSampleSize *= 2;
        }
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
        if (bitmap == null) {
            throw new IOException("Photo could not be decoded");
        }
        try {
            ExifInterface exif = new ExifInterface(file);
            Matrix orientation = new Matrix();
            if (exif.isFlipped()) {
                orientation.postScale(-1, 1);
            }
            orientation.postRotate(exif.getRotationDegrees());
            Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), orientation, true);
            if (oriented != bitmap) {
                bitmap.recycle();
            }
            return oriented;
        } catch (IOException | RuntimeException exception) {
            bitmap.recycle();
            throw exception;
        }
    }
}
