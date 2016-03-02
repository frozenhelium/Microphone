package com.fhx.microphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by fhx on 2/1/16.
 */
public class AudioRecorder {
    private AudioRecord mRecorder;
    private AudioRecordConfig mConfig;
    private byte[] mBuffer;
    private AudioFile mAudioFile;
    private long mRecordingStartTime;
    private PeriodicNotificationListener mNotifier = null;

    private Context mContext;


    public void setOnPeriodicNotificationListener(PeriodicNotificationListener onPeriodicNotification) {
        mNotifier = onPeriodicNotification;
    }

    public AudioRecorder(Context context){
        mContext = context;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        mConfig = new AudioRecordConfig();
        mConfig.numChannels = Integer.valueOf(preferences.getString("channel_config", "1"));
        mConfig.sampleRate = Integer.valueOf(preferences.getString("sample_rate", "44100"));
        mConfig.format = AudioFormat.ENCODING_PCM_16BIT;

        int audioSources[] = {
                MediaRecorder.AudioSource.DEFAULT,
                MediaRecorder.AudioSource.CAMCORDER,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.VOICE_CALL,
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource.VOICE_DOWNLINK,
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.VOICE_UPLINK
        };
        int audioSourceIndex = Integer.valueOf(preferences.getString("primary_audio_source", "0"));
        if(mConfig.numChannels == 2) {
            audioSourceIndex = Integer.valueOf(preferences.getString("secondary_audio_source", "1"));
        }
        mConfig.audioSource = audioSources[audioSourceIndex];

        if(mConfig.isValid()){
            mRecorder = new AudioRecord(
                    mConfig.audioSource,
                    mConfig.sampleRate,
                    mConfig.numChannels==2? AudioFormat.CHANNEL_IN_STEREO: AudioFormat.CHANNEL_IN_MONO,
                    mConfig.format,
                    mConfig.getBufferSize()
            );
            if(mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                Log.e("AudioRecorder", "failed to create AudioRecord instance");
            }
            mRecorder.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioRecord recorder) {

                }

                @Override
                public void onPeriodicNotification(AudioRecord recorder) {
                    recorder.read(mBuffer, 0, mBuffer.length);
                    short currentMax = 0;
                    short current;
                    for (int i = 0, lim = mBuffer.length / 2; i < lim; i += 2) {
                        current = getShort(mBuffer[i], mBuffer[i + 1]);
                        currentMax = current > currentMax ? current : currentMax;
                    }
                    try {
                        if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                            mAudioFile.write(mBuffer);
                            if (mNotifier != null) {
                                mNotifier.onPeriodicNotification(
                                        Calendar.getInstance().getTimeInMillis() - mRecordingStartTime,
                                        Math.abs(currentMax / 32768f));
                            }
                        }
                    } catch (IOException e) {
                        Log.e("onPeriodicNotification", e.getMessage());
                    }
                }

                private short getShort(byte argB1, byte argB2) {
                    return (short) (argB1 | (argB2 << 8));
                }
            });
            mRecorder.setPositionNotificationPeriod(mConfig.getFramePeriod());
            mBuffer = new byte[mConfig.numChannels
                    * mConfig.getFramePeriod()
                    * mConfig.getSampleSize()
                    / 8 ];
        }
    }

    public boolean isRecording(){
        return mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    public void release(){
        mRecorder.release();
    }

    public String startRecording() throws IOException{
        mAudioFile = new AudioFile();
        mAudioFile.prepare((short) mConfig.numChannels,
                mConfig.sampleRate,
                (short) mConfig.getSampleSize());
        mRecorder.startRecording();
        mRecordingStartTime = Calendar.getInstance().getTimeInMillis();
        // The periodic notification is triggered only after
        // first appropriate read() call
        mRecorder.read(mBuffer, 0, mBuffer.length);
        return mAudioFile.getFileName();
    }

    public String stopRecording(){
        mRecorder.stop();
        try {
            mAudioFile.close();
        }
        catch(IOException e){
            Log.e("AudioRecorder", e.getMessage());
        }
        return mAudioFile.getFileName();
    }


    public interface PeriodicNotificationListener{
        void onPeriodicNotification(long recordDuration, float amplitude);
    }

    public static boolean isSampleRateSupported(int sampleRate){
        // PCM 16 bit per sample is guaranteed to be supported by devices.
        return isSupportedConfig(sampleRate, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    public static boolean isStereoRecordingSupported(){
        // 44100Hz is currently the only rate that is guaranteed to work on all device
        return isSupportedConfig(44100, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    public static boolean isSupportedConfig(int sampleRate, int channelConfig, int format){
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, format);
        if(minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return false;
        }
        return true;
    }

    private class AudioRecordConfig{
        public int numChannels;
        public int sampleRate;
        public int format;

        public int audioSource;

        @Deprecated // use audioSource instead
        public int primaryMic;
        @Deprecated // use audioSource instead
        public int secondaryMic;

        private static final int STORAGE_INTERVAL = 50;

        public boolean isValid(){
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    numChannels==2? AudioFormat.CHANNEL_IN_STEREO: AudioFormat.CHANNEL_IN_MONO,
                    format);
            if(minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE){
                return false;
            }
            return true;
        }

        public int getBufferSize(){
            return 2 * numChannels * this.getFramePeriod() * this.getSampleSize() / 8;
        }

        public int getFramePeriod(){
            return sampleRate*STORAGE_INTERVAL/1000;
        }
        public int getSampleSize(){
            return format==AudioFormat.ENCODING_PCM_16BIT? 16:8;
        }
    }
}
