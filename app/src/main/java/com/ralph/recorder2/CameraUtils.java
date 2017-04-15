package com.ralph.recorder2;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description : Created by zhuxinhong on 2017/3/14.
 */
final class CameraUtils {

    // private static final int AREA_PER_1000 = 400;

//    private static final double MAX_ASPECT_DISTORTION = 0.15;
//
//    private static final int MIN_PREVIEW_PIXELS = 640 * 480; // normal screen

    static void initCamera(Camera camera, int width, int height) {
        Camera.Parameters params = camera.getParameters();
        params.set("orientation", "portrait");
        params.setPictureFormat(ImageFormat.JPEG);
        params.set("jpeg-quality", 100);
        setBestPreviewFPS(params, 10, 15);
        setFocus(params);
        //setBarcodeSceneMode(params);
        //setVideoStabilization(params);
        //setFocusArea(params);
        //setMetering(params);
        Camera.Size size = getOptimalSize(params.getSupportedPreviewSizes(), width, height);
        if (size != null) {
            params.setPreviewSize(size.width, size.height);
        }
        size = getOptimalSize(params.getSupportedPictureSizes(), width, height);
        if (size != null) {
            params.setPictureSize(size.width, size.height);
        }
        camera.setParameters(params);
    }

    public static void setFocus(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        }
    }


    public static void setVideoStabilization(Camera.Parameters parameters) {
        if (Build.VERSION.SDK_INT >= 15) {
            if (parameters.isVideoStabilizationSupported()) {
                parameters.setVideoStabilization(true);
            }
            return;
        }
        parameters.set("video-stabilization", "true");
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

    private static List<Camera.Size> getOptimalSizeList(List<Camera.Size> supportSize, int screenWidth, int screenHeight) {
        int width = screenWidth;
        int height = screenHeight;
        if (screenWidth < screenHeight) {
            width = screenHeight;
            height = screenWidth;
        }
        List<Camera.Size> optimalList = null;
        Camera.Size result = null;
        if (supportSize != null) {
            List<Camera.Size> size_9_16 = new ArrayList<>();
            List<Camera.Size> size_2_3 = new ArrayList<>();
//        List<Camera.Size> size_9_11 = new ArrayList<>(); 归到2：3里
            List<Camera.Size> size_3_4 = new ArrayList<>();
//        List<Camera.Size> size_9_13 = new ArrayList<>();归到3：4里
            List<Camera.Size> size_1_1 = new ArrayList<>();

            float ratio = height / (float) width;

            for (Camera.Size size : supportSize) {

                float tempRatio = size.height / (float) size.width;

                if (tempRatio < 0.61458f) {//small than 9/16 + (2/3 - 9/16)/2
                    size_9_16.add(size);
                } else if (tempRatio < 0.7083f) {//small than 2/3 + (3/4 - 2/3)/2
                    size_2_3.add(size);
                } else if (tempRatio < 0.8751) {//small than 3/4 + (1 - 3/4)/2
                    size_3_4.add(size);
                } else {
                    size_1_1.add(size);
                }
            }

            if (ratio < 0.61458f && size_9_16.size() > 0) {
                optimalList = size_9_16;
            } else if (ratio < 0.7083f && size_2_3.size() > 0) {
                optimalList = size_2_3;
            } else if (ratio < 0.8751 && size_3_4.size() > 0) {
                optimalList = size_3_4;
            } else {
                optimalList = size_1_1;
            }
        }
        return optimalList;
    }

    /**
     * 选择可用的预览尺寸
     *
     * @param supportSize
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    static Camera.Size getOptimalSize(List<Camera.Size> supportSize, int screenWidth, int screenHeight) {

        List<Camera.Size> optimalList = getOptimalSizeList(supportSize,screenWidth,screenHeight);
        Camera.Size result = null;
        if (optimalList != null) {
            int multi = screenWidth * screenHeight;
            int delta = Integer.MAX_VALUE;
            for (Camera.Size size : optimalList) {
                int temp = Math.abs(size.width * size.height - multi);
                if (temp < delta) {
                    result = size;
                    delta = temp;
                }
            }
        }
        return result;
    }

    /**
     * 选择可用的预览尺寸
     *
     * @param supportVideoSize
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    static Camera.Size getOptimalVideoSize(List<Camera.Size> supportVideoSize, int screenWidth, int screenHeight) {

        List<Camera.Size> optimalList = getOptimalSizeList(supportVideoSize,screenWidth,screenHeight);
        Camera.Size result = null;
        if (optimalList != null) {
            int multi = screenWidth * screenHeight;
            int delta = Integer.MAX_VALUE;
            for (Camera.Size size : optimalList) {

//                if(size.height == 720 && size.width == 1280 || size.height == 480 && size.width == 640){
//                    return size;
//                }

                int temp = Math.abs(size.width * size.height - multi);
                if (temp < delta) {
                    result = size;
                    delta = temp;
                }
            }
        }
        return result;
    }

}
