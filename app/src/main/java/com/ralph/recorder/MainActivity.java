package com.ralph.recorder;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * @author juzenhon
 */
public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.recorder_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, TNRecorderActivity.class);
                it.putExtra(TNRecorderActivity.EXTRA_PATH, getPath());
                startActivity(it);
            }
        });

        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    String getPath() {
        return Environment.getExternalStorageDirectory() + "/test.mp4";
    }
}
