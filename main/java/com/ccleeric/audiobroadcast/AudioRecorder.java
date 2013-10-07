package com.ccleeric.audiobroadcast;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by ccleeric on 13/9/30.
 */
public class AudioRecorder implements Subject {
    private final String TAG = "AudioRecorder";

    public static final int AUDIO_SOURCE_UNSUPPORTED = 0x301;


    private final int AUDIO_RATE_HZ = 8000;                            //Sample Rate 44100,22050, 16000, and 11025Hz
    private final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;    // CHANNEL_IN_MONO, CHANNEL_IN_STEREO
    private final int AUDIO_FORMAT  = AudioFormat.ENCODING_PCM_16BIT;   //ENCODING_PCM_16BIT, ENCODING_PCM_8BIT

    private AudioRecord mRecorder;
    private int mBufferSize;
    private byte[] mAudioData;
    private boolean mAudioStop;
    private Thread mRecorderThread;
    private ArrayList<Observer> mObservers;

    public AudioRecorder() {
        mBufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE_HZ,AUDIO_CHANNEL,AUDIO_FORMAT);
        mAudioData = new byte[mBufferSize];

        mAudioStop = true;
        mObservers = new ArrayList<Observer>();
    }

    public void setRecordSource(String audioSource) {
        int source = MediaRecorder.AudioSource.MIC;

        if (audioSource.equals("Voice Call")) {
            source = MediaRecorder.AudioSource.VOICE_CALL;
        }

        if(mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        mRecorder = new AudioRecord(source, AUDIO_RATE_HZ,
                                AUDIO_CHANNEL, AUDIO_FORMAT, mBufferSize);
    }

    public void start(final OutputStream audioStream) {
        mAudioStop = false;

        if(mRecorderThread != null) {
            mRecorderThread = null;
        }

        mRecorderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int bytes;
                try {
                    mRecorder.startRecording();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Audio Source doesn't support!", e);
                    notifyObserver(AUDIO_SOURCE_UNSUPPORTED, "Audio Source doesn't support!");
                    mRecorder.release();
                    return;
                }

                while(!mAudioStop) {
                    bytes = mRecorder.read(mAudioData, 0, mBufferSize);

                    try {
                        audioStream.write(mAudioData, 0, mBufferSize);
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        break;
                    }
                }
                mRecorder.stop();
                mRecorder.release();
            }
        });

        mRecorderThread.start();
    }

    public void stop() {
        mAudioStop = true;
    }

    @Override
    public void attachObserver(Observer observer) {
        mObservers.add(observer);
    }

    @Override
    public void detachObserver(Observer observer) {
        mObservers.remove(observer);
    }

    @Override
    public void notifyObserver(int action, Object args) {
        for(Observer obs : mObservers) {
            obs.update(action, args);
        }
    }
}
