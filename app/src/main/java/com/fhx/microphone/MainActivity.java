package com.fhx.microphone;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
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

public class MainActivity extends AppCompatActivity {
    private AudioRecorder mAudioRecorder = null;
    private boolean mRecording = false;

    private MenuItem mMenuItemSettings = null;
    private MenuItem mMenuItemRecordings = null;

    private TextView mTextTimer = null;
    private RecordButton mBtnRecord = null;
    private AudioRecordingService mRecordingService;
    private ServiceConnection mRecordingServiceConnection;

    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_default);
        setSupportActionBar(toolbar);

        // Create the app directory
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.e("Microphone", "Failed to detect External Storage");
        } else {
            File appDir = new File(Environment.getExternalStorageDirectory()+File.separator+"Microphone");
            if(!appDir.exists()) {
                appDir.mkdirs();
            }
        }

        mTextTimer = (TextView)findViewById(R.id.text_time);
        mBtnRecord = (RecordButton)findViewById(R.id.btn_record);

        mRecordingServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                AudioRecordingService.RecordingBinder binder = (AudioRecordingService.RecordingBinder)service;
                mRecordingService = binder.getServiceInstance();
                mRecordingService.setPeriodicNotificationListener(new AudioRecorder.PeriodicNotificationListener() {
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
                mRecordingService.setRecordingStopCallback(new AudioRecordingService.RecordingStopListener() {
                    @Override
                    public void onRecordingStop() {
                        MainActivity.this.onRecordingStop();
                    }
                });
                if(mRecordingService.isRecording()) {
                    MainActivity.this.onRecordingStart();
                } else {
                    checkPreferencesValidity();
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recordingServiceIntent = new Intent(MainActivity.this, AudioRecordingService.class);
                if (!mRecording) {
                    recordingServiceIntent.setAction(AudioRecordingService.ACTION_START_RECORDING);
                    MainActivity.this.onRecordingStart();
                } else {
                    recordingServiceIntent.setAction(AudioRecordingService.ACTION_STOP_RECORDING);
                    MainActivity.this.onRecordingStop();
                }
                startService(recordingServiceIntent);
            }
        });
    }

    private void onRecordingStart(){
        mRecording = true;
        mBtnRecord.setIsRecording(true);
        if (mMenuItemRecordings != null) mMenuItemRecordings.setEnabled(false);
        if (mMenuItemSettings != null) mMenuItemSettings.setEnabled(false);
        if(mRecordingService != null && mRecordingService.isRecording()) {
            Log.v("MainActivity", "started");
        }
    }

    private void onRecordingStop(){
        mRecording = false;
        mBtnRecord.setIsRecording(false);
        if (mMenuItemRecordings != null) mMenuItemRecordings.setEnabled(true);
        if (mMenuItemSettings != null) mMenuItemSettings.setEnabled(true);
        if(mRecordingService != null && !mRecordingService.isRecording()) {
            Log.v("MainActivity", "stopped");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent recordingServiceIntent = new Intent(this, AudioRecordingService.class);
        bindService(recordingServiceIntent, mRecordingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenuItemRecordings = menu.findItem(R.id.action_recordings);
        mMenuItemSettings = menu.findItem(R.id.action_settings);
        if(mRecording){
            mMenuItemSettings.setEnabled(false);
            mMenuItemRecordings.setEnabled(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SETTINGS_ACTIVITY_REQUEST_CODE){
            checkPreferencesValidity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivityForResult(new Intent(this, SettingsActivity.class),
                    SETTINGS_ACTIVITY_REQUEST_CODE);
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
        mRecordingService.clearRecordingStopCallback();
        unbindService(mRecordingServiceConnection);
        if(mAudioRecorder != null) mAudioRecorder.release();
    }

    private void checkPreferencesValidity(){
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
            mAudioRecorder.release();
            mAudioRecorder = null;
        }
    }
}
