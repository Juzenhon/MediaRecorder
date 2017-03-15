
package com.ralph.recorder;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

final class AutoFocusManager implements Camera.AutoFocusCallback {


    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    Point mFocusPoint;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<>(3);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean stopped;
    private boolean focusing;
    private final boolean useAutoFocus;
    private final Camera camera;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;

    AutoFocusManager(Context context, Camera camera) {
        mContext = context;
        this.camera = camera;
        Camera.Parameters parameters = camera.getParameters();
        String currentFocusMode = parameters.getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        autoFocusAgainLater();

        if (success) {
            Toast.makeText(mContext, "focus success", Toast.LENGTH_SHORT).show();
            theCamera.cancelAutoFocus();
        } else {
            Toast.makeText(mContext, "focus failed", Toast.LENGTH_SHORT).show();
        }
    }

    Runnable mFocusRunnable = new Runnable() {
        @Override
        public void run() {
            start();
        }
    };

    private synchronized void autoFocusAgainLater() {
        mHandler.removeCallbacks(mFocusRunnable);
        mHandler.postDelayed(mFocusRunnable, 3000);
    }

    synchronized void start() {
        if (useAutoFocus) {

            if (!stopped && !focusing) {
                try {
                    camera.autoFocus(this);
                    focusing = true;
                } catch (RuntimeException re) {
                    autoFocusAgainLater();
                }
            }
        }
    }

    private synchronized void cancelOutstandingTask() {
        mHandler.removeCallbacks(mFocusRunnable);
    }

    synchronized void stop() {
        stopped = true;
        cancelOutstandingTask();
        camera.cancelAutoFocus();
    }

}
