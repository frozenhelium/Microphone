package com.fhx.microphone;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import static com.fhx.microphone.AudioRecorder.*;

public class AudioRecordingService extends Service {
    private static final int NOTIFICATION_ID = 101;
    public static final String ACTION_START_RECORDING= "com.fhx.action.start";
    public static final String ACTION_STOP_RECORDING = "com.fhx.action.stop";

    private IBinder mBinder;
    private AudioRecorder mAudioRecorder = null;
    private Notification mNotification;
    private PeriodicNotificationListener mPeriodicListener;
    private RecordingStopListener mRecordingStopListener = null;

    public class RecordingBinder extends Binder {
        public AudioRecordingService getServiceInstance(){
            return AudioRecordingService.this;
        }
    }

    public interface RecordingStopListener{
        void onRecordingStop();
    }

    public AudioRecordingService() {
        mBinder = new RecordingBinder();
    }

    private void logE(String msg){
        Log.e("AudioRecordingService", msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // notificationIntent.setAction(ACTION_MAIN);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent stopIntent = new Intent(this, AudioRecordingService.class);
        stopIntent.setAction(ACTION_STOP_RECORDING);

        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Microphone")
                .setTicker("Microphone")
                .setContentText("Recording")
                .setSmallIcon(R.drawable.ic_stat_recording)
                .setLargeIcon(icon)
                .setContentIntent(notificationPendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_stop, "Stop Recording", stopPendingIntent)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals(ACTION_START_RECORDING)){
            this.startRecording();
        } else if(intent.getAction().equals(ACTION_STOP_RECORDING)){
            this.stopRecording();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mAudioRecorder != null){
            if(mAudioRecorder.isRecording()) {
                mAudioRecorder.stopRecording();
            }
            mAudioRecorder.release();
        }
        super.onDestroy();
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
    }

    public void startRecording(){
        this.createAudioRecorderInstance();
        if(mAudioRecorder != null){
            try {
                mAudioRecorder.setOnPeriodicNotificationListener(new PeriodicNotificationListener() {
                    @Override
                    public void onPeriodicNotification(long recordDuration, float amplitude) {
                        if(mPeriodicListener != null){
                            mPeriodicListener.onPeriodicNotification(recordDuration, amplitude);
                        }
                    }
                });
                mAudioRecorder.startRecording();
                startForeground(NOTIFICATION_ID, mNotification);
            } catch(Exception e){
                e.printStackTrace();
            }
        }else{
            this.logE("failed to initialize AudioRecorder");
        }
    }

    public void stopRecording(){
        if(mAudioRecorder!=null && mAudioRecorder.isRecording()){
            String fileName = mAudioRecorder.stopRecording();
            stopForeground(true);
            mAudioRecorder.release();
            mAudioRecorder = null;
            if(mRecordingStopListener != null){
                mRecordingStopListener.onRecordingStop();
            }
            Toast.makeText(getBaseContext(), "saved to: " + fileName, Toast.LENGTH_LONG).show();
            stopSelf();
        }
    }

    public boolean isRecording(){
        return mAudioRecorder != null && mAudioRecorder.isRecording();
    }

    public void setPeriodicNotificationListener(PeriodicNotificationListener listener){
        mPeriodicListener = listener;
    }

    public void setRecordingStopCallback(RecordingStopListener listener){
        mRecordingStopListener = listener;
    }

    public void clearRecordingStopCallback(){
        mRecordingStopListener = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
