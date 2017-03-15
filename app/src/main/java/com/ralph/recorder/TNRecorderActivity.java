package com.ralph.recorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Toast;


/**
 * @author juzenhon
 */
public class TNRecorderActivity extends AppCompatActivity implements IRecordView {

    public static final String EXTRA_PATH = "video_path";

    TNRecorderLayout mRecorderLayout;

    IRecorder mRecorder = new TNMediaRecorder();

    String mVideoOutputPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoOutputPath = getIntent().getStringExtra(EXTRA_PATH);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.small_video_record_layout);
        mRecorderLayout = (TNRecorderLayout) findViewById(R.id.sv_recorder_layout);

        PackageManager manager = getPackageManager();
        mRecorder.setOutputPath(mVideoOutputPath);
        mRecorderLayout.setRecorder(mRecorder);
        mRecorderLayout.setIRecordView(TNRecorderActivity.this);
        int result = manager.checkPermission(Manifest.permission.CAMERA, getPackageName());
        if (result != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > 22) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Toast.makeText(getApplicationContext(), "相机权限被禁用", Toast.LENGTH_SHORT).show();
            }
        } else {
            mRecorderLayout.prepareCameraSurface();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mRecorderLayout.prepareCameraSurface();
            } else {
                Toast.makeText(this, "相机权限被禁用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.cancel();
    }

    @Override
    public void onCloseRecord() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onFinished(String path) {
        Intent it = new Intent();
        it.putExtra(EXTRA_PATH, path);
        setResult(RESULT_OK, it);
        finish();
    }
}
