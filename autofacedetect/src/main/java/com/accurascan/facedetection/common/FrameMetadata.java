package com.accurascan.facedetection.common;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Describing a frame info.
 */
public class FrameMetadata {

    private final int width;
    private final int height;
    private final int rotation;
    private final int cameraFacing;
    private Rect rect;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRotation() {
        return rotation;
    }

    public int getCameraFacing() {
        return cameraFacing;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    private FrameMetadata(int width, int height, int rotation, int facing) {
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        cameraFacing = facing;
    }

    /**
     * Builder of {@link FrameMetadata}.
     */
    public static class Builder {

        private int width;
        private int height;
        private int rotation;
        private int cameraFacing;

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setRotation(int rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder setCameraFacing(int facing) {
            cameraFacing = facing;
            return this;
        }

        public FrameMetadata build() {
            return new FrameMetadata(width, height, rotation, cameraFacing);
        }
    }
}
