
package com.ralph.recorder2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ralph.recorder.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Description : Created by zhuxinhong on 2017/3/14.
 */
public class VideoRecorderLayout extends RelativeLayout
        implements VideoCameraTakenView.OnViewActionListener {

    private View closeBtn, retryBtn, okBtn, cameraSwitchBtn;

    private VideoCameraTakenView cameraTakenView;

    private ImageView pictureView, flashView;

    private TextView recordHint;

    private IRecorder2 mRecorder = null;

    private IRecordView mRecordView;

    private VideoSurfaceView mSurfaceView;
    /**
     * 1 video,2 picture
     */
    private int type = -1;

    private int mOrient = 1, mRecordOrient = 1;

    private String outputPath;

    private Bitmap bitmap;

    private long startTime, endTime;

    private boolean isPreview = true;

    private OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == closeBtn) {
                if (mRecordView != null) {
                    mRecordView.onCloseRecord();
                }
            } else if (v == retryBtn) {
                if (outputPath != null) {
                    File file = new File(outputPath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                mSurfaceView.stopPlay();
                if (type == 1) {
                    LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
                    layoutParams.width = LayoutParams.MATCH_PARENT;
                    layoutParams.height = LayoutParams.MATCH_PARENT;
                    layoutParams.topMargin = 0;
                    mSurfaceView.setLayoutParams(layoutParams);
                    if (mRecordOrient == 2 || mRecordOrient == 4) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mSurfaceView.reopenCamera();
                            }
                        });

                    } else {
                        mSurfaceView.reopenCamera();
                    }
                } else {
                    bitmap = null;
                    mSurfaceView.getCamera().startPreview();
                }
                initUIState();
            } else if (v == okBtn) {
                if (mRecordView != null) {
                    int width = 0, height = 0;
                    if (type == 1) {
                        if (mRecordOrient == 1 || mRecordOrient == 3) {
                            width = VideoMediaRecorder.videoHeight;
                            height = VideoMediaRecorder.videoWidth;
                        } else {
                            width = VideoMediaRecorder.videoWidth;
                            height = VideoMediaRecorder.videoHeight;
                        }
                        mSurfaceView.stopPlay();
                    } else {
                        if (bitmap != null) {
                            width = bitmap.getWidth();
                            height = bitmap.getHeight();
                        }
                        outputPath = saveBitmap(bitmap);
                    }

                    mRecordView.onFinished(type, outputPath, width, height, (int) (endTime - startTime) / 1000);
                }
            } else if (v == cameraSwitchBtn) {
                if (mSurfaceView != null) {
                    mSurfaceView.switchCamera();
                }
            } else if (v == flashView) {
                if (mSurfaceView != null) {
                    mSurfaceView.openFlash();
                }
            }
        }
    };

    public VideoRecorderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSurfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
        closeBtn = findViewById(R.id.sv_close_view);
        retryBtn = findViewById(R.id.sv_result_retry);
        okBtn = findViewById(R.id.sv_result_ok);
        cameraSwitchBtn = findViewById(R.id.sv_camera_switch);
        recordHint = (TextView) findViewById(R.id.sv_record_hint);
        cameraTakenView = (VideoCameraTakenView) findViewById(R.id.sv_progress_view);
        pictureView = (ImageView) findViewById(R.id.sv_picture_show);
        flashView = (ImageView) findViewById(R.id.sv_flash_btn);
        cameraTakenView.setActionListener(VideoRecorderLayout.this);
        closeBtn.setOnClickListener(mClickListener);
        retryBtn.setOnClickListener(mClickListener);
        okBtn.setOnClickListener(mClickListener);
        flashView.setOnClickListener(mClickListener);
        cameraSwitchBtn.setOnClickListener(mClickListener);
        initUIState();
    }

    public void setRecordView(IRecordView recordView) {
        mRecordView = recordView;
    }

    public void setRecorder(IRecorder2 recorder) {
        mRecorder = recorder;
    }

    private void initUIState() {
        isPreview = true;
        closeBtn.setVisibility(VISIBLE);
        retryBtn.setVisibility(INVISIBLE);
        okBtn.setVisibility(INVISIBLE);
        pictureView.setVisibility(GONE);
        recordHint.setVisibility(VISIBLE);
        cameraTakenView.setVisibility(VISIBLE);
        flashView.setVisibility(VISIBLE);
        setCameraSwitchVisible();
    }

    private void setCameraSwitchVisible() {
        cameraSwitchBtn.setVisibility(VISIBLE);
        int toDegrees = (mOrient - 1) * 90;
        cameraSwitchBtn.setRotation(toDegrees);
    }

    public boolean onStartRecord() {
        boolean result = false;
        if (mRecorder != null) {
            startTime = System.currentTimeMillis();
            mRecordOrient = mOrient;
            result = mRecorder.startRecord(mSurfaceView.getCamera(), mSurfaceView.getWidth(), mSurfaceView.getHeight(), mRecordOrient);
            if (result) {
                flashView.setVisibility(GONE);
                cameraSwitchBtn.setVisibility(GONE);
                recordHint.setVisibility(GONE);
                isPreview = false;
            }
        }
        return result;
    }

    @Override
    public void onTakePicture() {
        isPreview = false;
        mRecordOrient = mOrient;
        cameraTakenView.setVisibility(GONE);
        recordHint.setVisibility(GONE);
        closeBtn.setVisibility(GONE);
        mRecorder.takePicture(mSurfaceView.getCamera(), new IRecorder2.ITakePictureCallback() {
            @Override
            public void onPicture(Bitmap bmp) {
                buttonAppear();
                type = 2;
                bitmap = bmp;

                retryBtn.setVisibility(VISIBLE);
                okBtn.setVisibility(VISIBLE);
                cameraSwitchBtn.setVisibility(GONE);
                flashView.setVisibility(GONE);
                recordHint.setVisibility(GONE);
                pictureView.setVisibility(VISIBLE);
                pictureView.setImageBitmap(bmp);
            }
        }, mRecordOrient);
    }

    private void buttonAppear() {
        int left = retryBtn.getLeft() + retryBtn.getMeasuredWidth() / 2;
        int right = okBtn.getLeft() + okBtn.getMeasuredWidth() / 2;
        TranslateAnimation toLeft = new TranslateAnimation(getWidth() / 2 - left, 0, 0, 0);
        TranslateAnimation toRight = new TranslateAnimation(getWidth() / 2 - right, 0, 0, 0);
        toLeft.setFillBefore(true);
        toRight.setFillBefore(true);
        toLeft.setDuration(240);
        toRight.setDuration(240);
        retryBtn.startAnimation(toLeft);
        okBtn.startAnimation(toRight);
    }

    @Override
    public void onStopRecord() {

        if (mRecorder != null) {
            final String path = mRecorder.endRecord(mSurfaceView.getCamera());
            if (path == null) {
                flashView.setVisibility(VISIBLE);
                setCameraSwitchVisible();
                recordHint.setVisibility(VISIBLE);
                //ToastUtil.showTextViewPrompt("录制视频过短，请重新录制");
                mSurfaceView.getCamera().startPreview();
                isPreview = true;
            } else {
                buttonAppear();
                type = 1;
                closeBtn.setVisibility(GONE);
                retryBtn.setVisibility(VISIBLE);
                okBtn.setVisibility(VISIBLE);
                recordHint.setVisibility(GONE);
                flashView.setVisibility(GONE);
                recordHint.setVisibility(GONE);
                cameraSwitchBtn.setVisibility(GONE);
                cameraTakenView.setVisibility(GONE);

                endTime = System.currentTimeMillis();
                outputPath = path;

                LayoutParams layoutParams = (LayoutParams) mSurfaceView.getLayoutParams();
                if (mRecordOrient == 2 || mRecordOrient == 4) {
                    int surfaceHeight = VideoMediaRecorder.videoHeight * getWidth() / VideoMediaRecorder.videoWidth;
                    layoutParams.height = surfaceHeight;
                    layoutParams.topMargin = (getHeight() - surfaceHeight) / 2;
                } else {
                    layoutParams.height = LayoutParams.MATCH_PARENT;
                }
                mSurfaceView.setLayoutParams(layoutParams);
                mSurfaceView.playVideo(path);
            }
        }
    }

    @Override
    public long getRecordDuration() {
        return 10000;
    }

    public void requestOrient(int originOrient, int orient) {
//        if (mRecorder == null || !mRecorder.isRecording()) {
        if (isPreview) {
            int toDegrees = (orient - 1) * 90;
            cameraSwitchBtn.setRotation(toDegrees);
        }
        mOrient = orient;
    }

    String saveBitmap(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }
        String path = Environment.getExternalStorageDirectory().getPath() + "/"
                + System.currentTimeMillis() + ".jpg";

        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        return path;
    }
}
