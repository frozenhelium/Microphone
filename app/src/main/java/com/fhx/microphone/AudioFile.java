package com.fhx.microphone;

import android.util.Log;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by fhx on 2/2/16.
 */
public class AudioFile {
    private RandomAccessFile mFile;
    private int mBufferSize = 0;
    private PCMHeader mHeader;

    public AudioFile(String fileName) throws IOException{
        mFile = new RandomAccessFile(
                android.os.Environment.getExternalStorageDirectory() + "/" + fileName,
                "rw");
        mHeader = new PCMHeader();
    }

    public void prepare(short numChannels, int sampleRate, short sampleSize) throws IOException{
        mHeader.numChannels = numChannels;
        mHeader.sampleRate = sampleRate;
        mHeader.sampleSize = sampleSize;
        mHeader.prepare(mFile);
    }

    public void write(byte[] buffer) throws IOException{
        Log.v("AudioFile", "writing " + buffer.length + " bytes");
        mFile.write(buffer);
        mBufferSize += buffer.length;
    }

    public void close() throws IOException{
        mHeader.finalize(mFile, mBufferSize);
        mFile.close();
    }

    private class PCMHeader{
        public short numChannels;  // no. of channels
        public int sampleRate;     // sample rate (in Hz)
        public short sampleSize;   // bits per sample
        public void prepare(RandomAccessFile file) throws IOException{
            file.setLength(0);
            file.writeBytes("RIFF");
            file.writeInt(0);
            file.writeBytes("WAVE");
            file.writeBytes("fmt ");
            file.writeInt(Integer.reverseBytes(16));
            file.writeShort(Short.reverseBytes((short) 1));
            file.writeShort(Short.reverseBytes(this.numChannels));
            file.writeInt(Integer.reverseBytes(this.sampleRate));
            file.writeInt(Integer.reverseBytes(this.sampleRate * this.sampleSize * this.numChannels / 8));
            file.writeShort(Short.reverseBytes((short) (this.numChannels * this.sampleSize / 8)));
            file.writeShort(Short.reverseBytes(this.sampleSize));
            file.writeBytes("data");
            file.writeInt(0);
        }
        public void finalize(RandomAccessFile file, int totalDataSize) throws IOException {
            file.seek(4);
            file.writeInt(Integer.reverseBytes(36 + totalDataSize));
            file.seek(40);
            file.writeInt(Integer.reverseBytes(totalDataSize));
        }
    }

}
