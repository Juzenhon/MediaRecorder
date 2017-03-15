package com.ralph.recorder;

import android.hardware.Camera;
import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Description :
 * Created by zhuxinhong on 2017/3/14.
 * Job number：135198
 * Phone ：13520295328
 * Email：zhuxinhong@syswin.com
 * Person in charge : zhuxinhong
 * Leader：zhuxinhong
 */
public class TNMediaRecorder implements IRecorder {

    private static final int VIDEO_OUT_WIDTH = 320;

    private static final int VIDEO_OUT_HEIGHT = 240;


    private MediaRecorder mRecorder;
    private boolean isRecording = false;

    private String mOutputPath;

    public TNMediaRecorder() {
        mRecorder = new MediaRecorder();
    }

    private void initMediaRecorder(int degrees) {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setVideoSize(VIDEO_OUT_WIDTH, VIDEO_OUT_HEIGHT);
        mRecorder.setVideoFrameRate(20);
        mRecorder.setOrientationHint(degrees);
    }

    @Override
    public void setOutputPath(String path) {
        mOutputPath = path;
    }

    @Override
    public boolean start() {
        Camera camera = TNCameraManager.getManager().getCamera();
        if (camera == null || mOutputPath ==  null || isRecording) {
            return false;
        }
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        } else {
            mRecorder.reset();
        }
        try {
            camera.unlock();
            mRecorder.setCamera(camera);
            initMediaRecorder(TNCameraManager.getManager().getRecorderDegrees());
            mRecorder.setOutputFile(mOutputPath);
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalStateException e) {
            return false;
        }catch (Exception e){
            return false;
        }
        isRecording = true;
        return true;
    }

    @Override
    public void cancel() {
        if (mRecorder != null && isRecording) {
            try {
                mRecorder.stop();
            } catch (Exception e) {

            }
        }
        isRecording = false;
    }

    @Override
    public String end() {
        String ret = isRecording ? mOutputPath : null;
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        isRecording = false;
        return ret;
    }
}
