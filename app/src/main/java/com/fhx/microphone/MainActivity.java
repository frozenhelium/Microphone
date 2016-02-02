package com.fhx.microphone;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
        final Button recBtn = (Button)findViewById(R.id.btn_record);
        mAudioRecorder.setOnPeriodicNotificationListener(new AudioRecorder.OnPeriodicNotificationListener() {
            @Override
            public void onPeriodicNotification(long recordDuration) {
                int sec = (int)(recordDuration/1000);
                int min = sec/60;
                sec = sec % 60;
                recTimerText.setText(min + ":" + sec);
            }
        });
        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRecording){
                    try {
                        mAudioRecorder.startRecording();
                        mRecording = true;
                        ((Button) v).setText("Stop Recording");
                    }
                    catch (IOException e){
                        Log.e("MainActivity", e.getMessage());
                    }
                }
                else{
                    mAudioRecorder.stopRecording();
                    mRecording = false;
                    ((Button)v).setText("Start Recording");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioRecorder.release();
    }
}
