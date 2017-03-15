package com.ralph.recorder;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
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
public class TNRecorderActivity extends Activity {

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
        int result = manager.checkPermission(Manifest.permission.CAMERA, getPackageName());
        if (result != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > 22) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Toast.makeText(getApplicationContext(),"相机权限被禁用", Toast.LENGTH_SHORT).show();
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
}
