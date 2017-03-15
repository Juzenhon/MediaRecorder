package com.ralph.recorder;

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author juzenhon
 */
final class CameraUtils {

    private static final int AREA_PER_1000 = 400;

    private static final double MAX_ASPECT_DISTORTION = 0.15;

    private static final int MIN_PREVIEW_PIXELS = 240 * 320; // normal screen

    public static boolean isSupportFrontCamera() {
        return Camera.getNumberOfCameras() > 1;
    }

    /**
     * 获取摄像头
     *
     * @param back 是否是后置摄像头
     * @return
     */
    public static Camera getCamera(boolean back) {

        Camera camera = null;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int id = back ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == id) {
                camera = Camera.open(i);
                break;
            }
        }
        return camera;

    }

    static void initCamera(Camera camera, Point screenSize) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.set("orientation", "portrait");

        setBestPreviewFPS(parameters, 10, 20);
        setFocus(parameters, true, true, false);
        setBarcodeSceneMode(parameters);
        setVideoStabilization(parameters);
        setFocusArea(parameters);
        setMetering(parameters);
        Point previewSize = findBestPreviewSizeValue(parameters, screenSize);
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.x, previewSize.y);
        }
        camera.setParameters(parameters);
    }

    public static void setFocus(Camera.Parameters parameters, boolean autoFocus,
                                boolean disableContinuous, boolean safeMode) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        String focusMode = null;
        if (autoFocus) {
            if (safeMode || disableContinuous) {
                focusMode = findSettableValue("focus mode", supportedFocusModes,
                        Camera.Parameters.FOCUS_MODE_AUTO);
            } else {
                focusMode = findSettableValue("focus mode", supportedFocusModes,
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                        Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
        // Maybe selected auto-focus but not available, so fall through here:
        if (!safeMode && focusMode == null) {
            focusMode = findSettableValue("focus mode", supportedFocusModes,
                    Camera.Parameters.FOCUS_MODE_MACRO, Camera.Parameters.FOCUS_MODE_EDOF);
        }
        if (focusMode != null) {
            if (focusMode.equals(parameters.getFocusMode())) {
            } else {
                parameters.setFocusMode(focusMode);
            }
        }
    }

    public static void setBarcodeSceneMode(Camera.Parameters parameters) {
        if (Camera.Parameters.SCENE_MODE_BARCODE.equals(parameters.getSceneMode())) {
//            Log.i(TAG, "Barcode scene mode already set");
            return;
        }
        String sceneMode = findSettableValue("scene mode", parameters.getSupportedSceneModes(),
                Camera.Parameters.SCENE_MODE_BARCODE);
        if (sceneMode != null) {
            parameters.setSceneMode(sceneMode);
        }
    }

    private static String findSettableValue(String name, Collection<String> supportedValues,
                                            String... desiredValues) {
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    return desiredValue;
                }
            }
        }
        return null;
    }

    public static void setFocusArea(Camera.Parameters parameters) {
        if (parameters.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            parameters.setFocusAreas(middleArea);
        }
    }

    public static void setMetering(Camera.Parameters parameters) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> middleArea = buildMiddleArea(AREA_PER_1000);
            parameters.setMeteringAreas(middleArea);
        } else {
        }
    }

    private static List<Camera.Area> buildMiddleArea(int areaPer1000) {
        return Collections.singletonList(new Camera.Area(new Rect(-areaPer1000, -areaPer1000,
                areaPer1000, areaPer1000), 1));
    }

    public static void setVideoStabilization(Camera.Parameters parameters) {
        if (parameters.isVideoStabilizationSupported()) {
            if (parameters.getVideoStabilization()) {
            } else {
                parameters.setVideoStabilization(true);
            }
        } else {
        }
    }

    public static void setBestPreviewFPS(Camera.Parameters parameters, int minFPS, int maxFPS) {
        List<int[]> supportedPreviewFpsRanges = parameters.getSupportedPreviewFpsRange();
        if (supportedPreviewFpsRanges != null && !supportedPreviewFpsRanges.isEmpty()) {
            int[] suitableFPSRange = null;
            for (int[] fpsRange : supportedPreviewFpsRanges) {
                int thisMin = fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                int thisMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
                if (thisMin >= minFPS * 1000 && thisMax <= maxFPS * 1000) {
                    suitableFPSRange = fpsRange;
                    break;
                }
            }
            if (suitableFPSRange != null) {
                int[] currentFpsRange = new int[2];
                parameters.getPreviewFpsRange(currentFpsRange);
                if (!Arrays.equals(currentFpsRange, suitableFPSRange)) {
                    parameters.setPreviewFpsRange(
                            suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                            suitableFPSRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                }
            }
        }
    }

    public static Point findBestPreviewSizeValue(Camera.Parameters parameters,
                                                 Point screenResolution) {

        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            if (defaultSize == null) {
                return null;
            }
            return new Point(defaultSize.width, defaultSize.height);
        }

        // Sort by size, descending
        List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        double screenAspectRatio = (double) screenResolution.x / (double) screenResolution.y;

        // Remove sizes that are unsuitable
        Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewSize = it.next();
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
                Point exactPoint = new Point(realWidth, realHeight);
                return exactPoint;
            }
        }

        // If no exact match, use largest preview size. This was not a great
        // idea on older devices because
        // of the additional computation needed. We're likely to get here on
        // newer Android 4+ devices, where
        // the CPU is much more powerful.
        if (!supportedPreviewSizes.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewSizes.get(0);
            Point largestSize = new Point(largestPreview.width, largestPreview.height);
            return largestSize;
        }

        // If there is nothing at all suitable, return current preview size
        Camera.Size defaultPreview = parameters.getPreviewSize();
        if (defaultPreview == null) {
            return null;
        }
        Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
        return defaultSize;
    }


}
