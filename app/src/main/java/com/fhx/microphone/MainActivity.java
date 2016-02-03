package com.fhx.microphone;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private AudioRecorder mAudioRecorder;
    private boolean mRecording = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_default);
        setSupportActionBar(toolbar);


        // Create the app directory
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.d("Microphone", "Failed to detect External Storage");
        } else {
            File appDir = new File(Environment.getExternalStorageDirectory()+File.separator+"Microphone");
            if(!appDir.exists()) {
                appDir.mkdirs();
            }
        }

        mAudioRecorder = new AudioRecorder();
        final TextView recTimerText = (TextView)findViewById(R.id.text_time);
        final RecordButton recBtn = (RecordButton)findViewById(R.id.btn_record);
        mAudioRecorder.setOnPeriodicNotificationListener(new AudioRecorder.OnPeriodicNotificationListener() {
            @Override
            public void onPeriodicNotification(long recordDuration, float amplitude) {
                int sec = (int)(recordDuration/1000);
                int min = sec/60;
                sec = sec % 60;
                String formattedDuration = min + ":" + (sec < 10? ("0" + sec) : (""+sec));
                recTimerText.setText(formattedDuration);
                recBtn.setIndicatorLevel(amplitude);
            }
        });
        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecording) {
                    try {
                        mAudioRecorder.startRecording();
                        mRecording = true;
                        ((RecordButton) v).setIsRecording(true);
                    } catch (IOException e) {
                        Log.e("MainActivity", e.getMessage());
                    }
                } else {
                    mAudioRecorder.stopRecording();
                    mRecording = false;
                    ((RecordButton) v).setIsRecording(false);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioRecorder.release();
    }
}
