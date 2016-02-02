package com.fhx.microphone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private AudioRecorder mAudioRecorder;
    private boolean mRecording = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioRecorder = new AudioRecorder();

        final Button recBtn = (Button)findViewById(R.id.btn_record);
        recBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRecording){
                    mAudioRecorder.startRecording();
                    mRecording = true;
                    ((Button)v).setText("Stop Recording");
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
