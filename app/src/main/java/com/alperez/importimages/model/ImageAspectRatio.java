package com.alperez.importimages.model;

/**
 * Created by stanislav.perchenko on 10.09.2020 at 11:50.
 */
public enum ImageAspectRatio {
    ASPECT_SQUARE(true, 1, 1), ASPECT_3_4(true, 3, 4), ASPECT_16_9(true, 16, 9), ASPECT_ANY(false, 1, 1);

    private final boolean fixedAspectRatio;
    private final float aspectValue;
    private final int width, height;

    ImageAspectRatio(boolean fixedAspectRatio, int width, int height) {
        this.fixedAspectRatio = fixedAspectRatio;
        this.width = width;
        this.height = height;
        aspectValue = 1f*width / height;
    }

    public float getAspectValue() {
        return aspectValue;
    }

    public boolean isFixedAspectRatio() {
        return fixedAspectRatio;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

