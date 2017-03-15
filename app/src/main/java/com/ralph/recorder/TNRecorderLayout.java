package com.ralph.recorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Description :
 * Created by zhuxinhong on 2017/3/14.
 * Job number：135198
 * Phone ：13520295328
 * Email：zhuxinhong@syswin.com
 * Person in charge : zhuxinhong
 * Leader：zhuxinhong
 */
public class TNRecorderLayout extends RelativeLayout {

    private TNSurfaceView mSurfaceView;

    private TextView mMoveUpHint, mCancelHint;

    private View mPressLayout;

    private View mSwitchCamera, mCloseView;

    private TNProgressView mProgressView;

    private int mDuration = 10;

    private long mCurrentTime;

    private boolean isExecuted = false;

    private IRecorder mRecorder;

    public TNRecorderLayout(Context context) {
        this(context, null);
    }

    public TNRecorderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSurfaceView = new TNSurfaceView(getContext());
        mMoveUpHint = (TextView) findViewById(R.id.sv_move_up_hint);
        mCancelHint = (TextView) findViewById(R.id.sv_cancel_hint);
        mPressLayout = findViewById(R.id.sv_press_view);
        mSwitchCamera = findViewById(R.id.sv_camera_switch);
        mSwitchCamera.setVisibility(GONE);
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurfaceView.switchCamera();
            }
        });
        mCloseView = findViewById(R.id.sv_close_view);
        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) getContext()).finish();
            }
        });
        mProgressView = (TNProgressView) findViewById(R.id.sv_progress_view);
        mPressLayout.setEnabled(false);
        mPressLayout.findViewById(R.id.sv_press_btn_view).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return pressOnTouch(v, event);
            }
        });
        mProgressView.setDuration(mDuration);
    }

    void initRecordUIState(){
        mCloseView.setVisibility(VISIBLE);
        mSwitchCamera.setVisibility(VISIBLE);
        mProgressView.setVisibility(GONE);
        mMoveUpHint.setVisibility(View.GONE);
        mCancelHint.setVisibility(View.GONE);
    }

    void prepareCameraSurface() {
        if (mSurfaceView.getParent() != null) {
            ((ViewGroup) mSurfaceView.getParent()).removeView(mSurfaceView);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mSurfaceView, 0, lp);


        mPressLayout.setEnabled(true);
        mSwitchCamera.setVisibility(VISIBLE);
    }

    private Runnable mSubmitRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isExecuted) {
                isExecuted = true;
                long delta = System.currentTimeMillis() - mCurrentTime;
                if (delta < 3000) {
                    if (mRecorder != null) {
                        Toast.makeText(getContext(), "视频太短了", Toast.LENGTH_SHORT).show();
                        mRecorder.cancel();
                    }
                } else {
                    if (mRecorder != null) {
                        mRecorder.end();
                    }
                }
                mProgressView.stop();

                initRecordUIState();
            }
            //
        }
    };

    private Runnable mCancelRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecorder != null) {
                mRecorder.cancel();
            }
            mProgressView.stop();
            initRecordUIState();
        }
    };


    public void setRecorder(IRecorder recorder) {
        mRecorder = recorder;
    }

    private boolean pressOnTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                if (mRecorder != null) {
                    if (Build.VERSION.SDK_INT > 22) {
                        int result = getContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO);
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            ((Activity) getContext()).requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 2);
                            return false;
                        } else {
                            result = getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (result != PackageManager.PERMISSION_GRANTED) {
                                ((Activity) getContext()).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                                return false;
                            }
                        }
                    }
                    boolean success = mRecorder.start();
                    if (!success) {
                        Toast.makeText(getContext(),"录制失败", Toast.LENGTH_SHORT).show();
//                        post(mCancelRunnable);
                        return false;
                    }
                }
                mSwitchCamera.setVisibility(GONE);
                mCloseView.setVisibility(GONE);
                mMoveUpHint.setVisibility(View.VISIBLE);
                mCancelHint.setVisibility(View.GONE);
                mProgressView.setVisibility(VISIBLE);
                mProgressView.start();

                postDelayed(mSubmitRunnable, mDuration * 1000);
                isExecuted = false;
                mCurrentTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_MOVE:
                if(isExecuted){
                    initRecordUIState();
                }else {
                    if (y > 0 && y < v.getHeight()) {
                        mProgressView.setNormal(true);
                        mMoveUpHint.setVisibility(View.VISIBLE);
                        mCancelHint.setVisibility(View.GONE);
                    } else {
                        mProgressView.setNormal(false);
                        mMoveUpHint.setVisibility(View.GONE);
                        mCancelHint.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                removeCallbacks(mSubmitRunnable);
                if (isExecuted) {
                    initRecordUIState();
                }else {
                    if (y > 0 && y < v.getHeight()) {
                        //end
                        post(mSubmitRunnable);
                    } else {
                        post(mCancelRunnable);
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        int surfaceHeight = dm.widthPixels * 4 / 3;
//        LayoutParams rlp = (LayoutParams) mSurfaceView.getLayoutParams();
//        rlp.height = surfaceHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

}
