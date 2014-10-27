package net.thirtythreeforty.pikyak;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * A Picasso Transformation that resizes an image to a certain width, preserving the aspect ratio.
 */
public class KeepRatioTransformation implements Transformation {
    private final int targetWidth;

    public KeepRatioTransformation(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
        int targetHeight = (int) (targetWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "KeepRatioTransformation" + targetWidth;
    }
}
