package com.fhx.microphone;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
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
    private AudioRecorder mAudioRecorder = null;
    private boolean mRecording = false;

    private MenuItem mMenuItemSettings = null;
    private MenuItem mMenuItemRecordings = null;

    private TextView mTextTimer = null;
    private RecordButton mBtnRecord = null;

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

        createAudioRecorderInstance();
        mTextTimer = (TextView)findViewById(R.id.text_time);
        mBtnRecord = (RecordButton)findViewById(R.id.btn_record);
        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecording) {
                    try {
                        mAudioRecorder.startRecording();
                        mRecording = true;
                        ((RecordButton) v).setIsRecording(true);
                        if(mMenuItemRecordings != null)  mMenuItemRecordings.setEnabled(false);
                        if(mMenuItemSettings != null) mMenuItemSettings.setEnabled(false);
                    } catch (IOException e) {
                        Log.e("MainActivity", e.getMessage());
                    }
                } else {
                    mAudioRecorder.stopRecording();
                    mRecording = false;
                    ((RecordButton) v).setIsRecording(false);
                    if(mMenuItemRecordings != null)  mMenuItemRecordings.setEnabled(true);
                    if(mMenuItemSettings != null) mMenuItemSettings.setEnabled(true);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuItemRecordings = menu.findItem(R.id.action_recordings);
        mMenuItemSettings = menu.findItem(R.id.action_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if(item.getItemId() == R.id.action_recordings){
            startActivity(new Intent(this, RecordingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAudioRecorder != null) mAudioRecorder.release();
    }

    private void createAudioRecorderInstance(){
        if(mAudioRecorder != null){
            if(mAudioRecorder.isRecording()){
                mAudioRecorder.stopRecording();
            }
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
        try {
            mAudioRecorder = new AudioRecorder(this);
        } catch(Exception e){
            e.printStackTrace();
        }
        if(mAudioRecorder == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Microphone: Fatal Exception");
            builder.setMessage("failed to create AudioRecord instance, "
                    + "this might be due to invalid settings\n"
                    + "Do you want to reset the settings and try again?");
            builder.setInverseBackgroundForced(true);
            builder.setCancelable(false);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Reset the preferences to default values
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor preferenceEditor = preferences.edit();
                    preferenceEditor.clear();
                    preferenceEditor.commit();

                    // relaunch the activity
                    Intent intent = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                    System.exit(1);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            mAudioRecorder.setOnPeriodicNotificationListener(new AudioRecorder.OnPeriodicNotificationListener() {
                @Override
                public void onPeriodicNotification(long recordDuration, float amplitude) {
                    int sec = (int) (recordDuration / 1000);
                    int min = sec / 60;
                    sec = sec % 60;
                    String formattedDuration = min + ":" + (sec < 10 ? ("0" + sec) : ("" + sec));
                    mTextTimer.setText(formattedDuration);
                    mBtnRecord.setIndicatorLevel(amplitude);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        // quick and ugly fix to apply preference changes
        createAudioRecorderInstance();
        super.onResume();
    }
}
