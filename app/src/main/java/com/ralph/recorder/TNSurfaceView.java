package com.ralph.recorder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.Toast;

import java.io.IOException;

/**
 * @author juzenhon
 */
public class TNSurfaceView extends TextureView implements TextureView.SurfaceTextureListener {

    AutoFocusManager focusManager;
    private SurfaceTexture mSurface;

    public TNSurfaceView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public TNSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
    }

    public void switchCamera() {

        if (mSurface != null) {
            TNCameraManager.getManager().release();
            if (focusManager != null) {
                focusManager.stop();
            }
            try {
                TNCameraManager.getManager().cameraSwitch();
            } catch (Exception e) {

            }
            startPreview();
        }
    }

    void openCamera() {
        try {
            TNCameraManager.getManager().openCamera(getWidth(), getHeight());
        } catch (Exception e) {

        }
        startPreview();
    }

    private void startPreview() {
        Camera camera = TNCameraManager.getManager().getCamera();
        if (camera == null) {
            Toast.makeText(getContext(), "没有摄像头或相机权限被拒绝", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            camera.setPreviewTexture(mSurface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();

        if (focusManager == null) {
            focusManager = new AutoFocusManager(getContext(), camera);
        }
        focusManager.start();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (focusManager != null) {
            focusManager.stop();
        }
        TNCameraManager.getManager().release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        mSurface = surface;
    }
}
