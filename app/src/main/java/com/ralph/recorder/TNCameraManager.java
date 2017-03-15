package com.ralph.recorder;

import android.graphics.Point;
import android.hardware.Camera;

/**
 * Description :
 * Created by zhuxinhong on 2017/3/15.
 * Job number：135198
 * Phone ：13520295328
 * Email：zhuxinhong@syswin.com
 * Person in charge : zhuxinhong
 * Leader：zhuxinhong
 */
public final class TNCameraManager {

    private static TNCameraManager sManager = new TNCameraManager();

    private Camera mCamera;

    private boolean isBack = true;

    private int CameraId;

    private Point screenPoint;

    private TNCameraManager() {
    }

    public static TNCameraManager getManager() {
        return sManager;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean openCamera(int screenWidth, int screenHeight) {
        screenPoint = new Point(screenWidth, screenHeight);
        Camera camera = CameraUtils.getCamera(isBack);
        if (camera != null) {
            if (isBack) {
                camera.setDisplayOrientation(90);
            } else {
                camera.setDisplayOrientation(90);
            }
            CameraUtils.initCamera(camera, screenPoint);

        }
        mCamera = camera;

        return camera != null;
    }

    int getRecorderDegrees() {
        return isBack ? 90 : 270;
    }

    public void cameraSwitch() {
        isBack = !isBack;
        openCamera(screenPoint.x, screenPoint.y);
    }

    public void release() {
        isBack = true;
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}
