package com.ralph.recorder;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 *
 * @author juzenhon
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.recorder_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this,TNRecorderActivity.class);
                it.putExtra(TNRecorderActivity.EXTRA_PATH,getPath());
                startActivity(it);
            }
        });
    }

    String getPath(){
        return Environment.getExternalStorageDirectory() + "/test.mp4";
    }
}
