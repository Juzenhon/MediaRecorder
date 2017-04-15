
package com.ralph.recorder2;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Description : Created by zhuxinhong on 2017/3/14.
 */
public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private MediaPlayer mPlayer;

    private CameraWrapper mCamera = null;

    private boolean isBackCamera = true;

    public VideoSurfaceView(Context context) {
        this(context, null);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void switchCamera() {
        stopPlay();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        boolean backCamera = !isBackCamera;
        openCamera(backCamera);
    }

    public void openFlash() {
        if (mCamera != null) {
            mCamera.enableFlash();
        }
    }

    CameraWrapper getCamera() {
        return mCamera;
    }

    void openCamera(boolean isback) {
        CameraWrapper wrapper = CameraWrapper.openCamera(isback);
        if (wrapper == null) {
            Toast.makeText(getContext(), "没有摄像头或相机权限被拒绝", Toast.LENGTH_SHORT).show();
            return;
        }
        isBackCamera = isback;
        Camera camera = wrapper.camera;
        camera.setDisplayOrientation(wrapper.getDegree());
        CameraUtils.initCamera(camera, getWidth(), getHeight());
        try {
            camera.setPreviewDisplay(getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 开启预览
        camera.startPreview();
        wrapper.focus();
        mCamera = wrapper;
    }

    void reopenCamera() {
        openCamera(isBackCamera);
    }

    void playVideo(String path) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer.reset();
        try {
            mPlayer.setLooping(true);
            mPlayer.setDataSource(path);
            getHolder().setKeepScreenOn(true);
            mPlayer.setDisplay(getHolder());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopPlay() {
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera(isBackCamera);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null && mCamera.camera != null) {
            CameraUtils.initCamera(mCamera.camera, width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPlay();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
