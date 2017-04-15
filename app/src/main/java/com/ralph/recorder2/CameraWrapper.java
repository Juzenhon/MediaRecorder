package com.ralph.recorder2;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;

/**
 * Description : Created by zhuxinhong on 2017/3/14.
 */
class CameraWrapper {

    Camera camera;

    int cameraId;

    Camera.CameraInfo cameraInfo;

    private boolean isFocusing = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private CameraWrapper() {

    }

    boolean isBackCamera() {
        return cameraInfo != null && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
    }


    public static CameraWrapper openCamera(boolean backCamera) {
        Camera camera = null;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int face = backCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        int id = -1;
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == face) {
                id = i;
                camera = Camera.open(i);
                break;
            }
        }
        CameraWrapper wrapper = null;
        if (camera != null) {
            wrapper = new CameraWrapper();
            wrapper.camera = camera;
            wrapper.cameraInfo = cameraInfo;
            wrapper.cameraId = id;
        }
        return wrapper;
    }

    public int getDegree(){
//        android.hardware.Camera.CameraInfo info =
//                new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(cameraId, info);
//        int rotation = activity.getWindowManager().getDefaultDisplay()
//                .getRotation();
        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public void enableFlash() {
        if (camera != null && isBackCamera()) {
            Camera.Parameters parameters = camera.getParameters();
            String flashMode = parameters.getFlashMode();

            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//开启
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
            }
            camera.setParameters(parameters);
        }
    }

    public void release() {
        handler.removeCallbacks(runnable);
        if (camera != null) {
            camera.cancelAutoFocus();
            camera.stopPreview();
            camera.release();
            camera = null;
            cameraInfo = null;
            cameraId = -1;
            isFocusing = false;
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            focus();
        }
    };

    public void startPreview() {
        cancelFocus();
        if (camera != null) {
            camera.startPreview();
            focus();
        }
    }

    public void stopPreview() {
        cancelFocus();
        if (camera != null) {
            camera.lock();
            camera.stopPreview();
        }
    }

    public void takePicture(Camera.PictureCallback callback) {
        cancelFocus();
        if (camera != null) {
            camera.takePicture(null, null, callback);
        }
    }

    void cancelFocus() {
        handler.removeCallbacks(runnable);
        if (isFocusing && camera != null) {
            camera.cancelAutoFocus();
        }
        isFocusing = false;
    }

    public void focus() {
        if (camera != null && isBackCamera() && !isFocusing) {
            isFocusing = true;

            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    camera.cancelAutoFocus();
                    isFocusing = false;
                    handler.postDelayed(runnable, 3000);
                }
            });


        }
    }

}
