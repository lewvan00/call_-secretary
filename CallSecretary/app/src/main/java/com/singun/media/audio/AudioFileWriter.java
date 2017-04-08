package com.singun.media.audio;

import android.text.TextUtils;

import com.singun.media.audio.encode.AudioEncodeUtil;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by singun on 2017/3/1 0001.
 */

public class AudioFileWriter {
    private AudioConfig mAudioConfig;
    private DataOutputStream mOutputStream;

    public AudioFileWriter(AudioConfig audioConfig) {
        mAudioConfig = audioConfig;
    }

    public boolean createAudioFile(boolean forceCreate) {
        if (TextUtils.isEmpty(mAudioConfig.audioDirPath) || TextUtils.isEmpty(mAudioConfig.audioName)) {
            return false;
        }
        File dir = new File(mAudioConfig.audioDirPath);
        if (dir.isFile()) {
            dir.delete();
        }
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }

        File pcmFile = checkFile(forceCreate);
        if (pcmFile == null) {
            return false;
        }

        try {
            int cacheByte = mAudioConfig.audioDataSize * 2;
            FileOutputStream fileOutputStream = new FileOutputStream(pcmFile);
            mOutputStream = new DataOutputStream(new BufferedOutputStream(fileOutputStream, cacheByte));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private File checkFile(boolean forceCreate) {
        File pcmFile = new File(mAudioConfig.audioDirPath, mAudioConfig.audioName + ".pcm");
        if (pcmFile.exists()) {
            if (forceCreate) {
                pcmFile.delete();
            } else {
                return null;
            }
        }

        File wavFile = new File(mAudioConfig.audioDirPath, mAudioConfig.audioName + ".wav");
        if (wavFile.exists()) {
            if (forceCreate) {
                wavFile.delete();
            } else {
                return null;
            }
        }
        return pcmFile;
    }

    public void saveRecordData(int length) {
        if (mOutputStream == null) {
            return;
        }
        if (length > mAudioConfig.audioDataOut.length) {
            length = mAudioConfig.audioDataOut.length;
        }
        try {
            DataOutputStream dataOutputStream = mOutputStream;
            if (dataOutputStream == null) {
                return;
            }
            byte[] bytes = toBytes(length);
            dataOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] toBytes(int length) {
        int shortIndex, byteIndex;
        byte[] buffer = new byte[length * 2];
        shortIndex = byteIndex = 0;
        for (; shortIndex != length; ) {
            buffer[byteIndex] = (byte) (mAudioConfig.audioDataOut[shortIndex] & 0x00FF);
            buffer[byteIndex + 1] = (byte) ((mAudioConfig.audioDataOut[shortIndex] & 0xFF00) >> 8);
            ++shortIndex;
            byteIndex += 2;
        }
        return buffer;
    }

    public void saveAudioFormat(int sampleRate, int channelCount) {
        if (mOutputStream == null) {
            return;
        }
        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = null;

        File pcmFile = new File(mAudioConfig.audioDirPath, mAudioConfig.audioName + ".pcm");
        File wavFile = new File(mAudioConfig.audioDirPath, mAudioConfig.audioName + ".wav");

        AudioEncodeUtil.convertPcmToWav(pcmFile, wavFile, sampleRate, channelCount);
    }

    public void release() {
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
