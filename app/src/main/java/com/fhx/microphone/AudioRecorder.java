package com.fhx.microphone;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by fhx on 2/1/16.
 */
public class AudioRecorder {
    private AudioRecord mRecorder;
    private AudioRecordConfig mConfig;
    private byte[] mBuffer;
    private AudioFile mAudioFile;

    public AudioRecorder(){
        mConfig = new AudioRecordConfig();
        mConfig.numChannels = 1;
        mConfig.sampleRate = 48000;
        mConfig.format = AudioFormat.ENCODING_PCM_16BIT;
        mConfig.primaryMic = MediaRecorder.AudioSource.DEFAULT;

        if(mConfig.isValid()){
            mRecorder = new AudioRecord(
                    mConfig.primaryMic,
                    mConfig.sampleRate,
                    mConfig.numChannels==1? AudioFormat.CHANNEL_IN_MONO: AudioFormat.CHANNEL_IN_STEREO,
                    mConfig.format,
                    mConfig.getBufferSize()
            );
            if(mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                Log.e("AudioRecorder", "failed to create AudioRecord instance");
            }

            mRecorder.setRecordPositionUpdateListener(onRecordPositionUpdate);
            mRecorder.setPositionNotificationPeriod(mConfig.getFramePeriod());
            mBuffer = new byte[mConfig.numChannels
                    * mConfig.getFramePeriod()
                    * mConfig.getSampleSize()
                    / 8 ];
        }
    }

    public void release(){
        mRecorder.release();
    }

    public void startRecording(){
        try {
            mAudioFile = new AudioFile("test.wav");
            mAudioFile.prepare((short) mConfig.numChannels,
                    mConfig.sampleRate,
                    (short) mConfig.getSampleSize());
        }
        catch (IOException e){
            Log.e("AudioRecorder", e.getMessage());
        }
        mRecorder.startRecording();
    }

    public void stopRecording(){
        mRecorder.stop();
        try {
            mAudioFile.close();
        }
        catch(IOException e){
            Log.e("AudioRecorder", e.getMessage());
        }
    }

    private AudioRecord.OnRecordPositionUpdateListener onRecordPositionUpdate
            = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioRecord recorder) {

        }

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            recorder.read(mBuffer, 0, mBuffer.length);
            try{
                mAudioFile.write(mBuffer);
            }
            catch (IOException e){
                Log.e("AudioRecorder", e.getMessage());
            }
        }
    };

    private class AudioRecordConfig{
        public int numChannels;
        public int sampleRate;
        public int format;

        public int primaryMic;
        public int secondaryMic;

        private static final int STORAGE_INTERVAL = 1000;

        public boolean isValid(){
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    numChannels==1? AudioFormat.CHANNEL_IN_MONO: AudioFormat.CHANNEL_IN_STEREO,
                    format);
            if(minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE){
                return false;
            }
            return true;
        }

        public int getBufferSize(){
            /*int minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    numChannels==1? AudioFormat.CHANNEL_IN_MONO: AudioFormat.CHANNEL_IN_STEREO,
                    format);
            if(minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE){
                // maybe throw some error
                return 0;
            }*/

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
