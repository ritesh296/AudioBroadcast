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

    public static final int ACTION_RECORD_START = 0;
    public static final int ACTION_RECORD_STOP  = 1;
    public static final String ACTION_RECORD_START1 = "RECORD_START";
    public static final String ACTION_RECORD_STOP1  = "RECORD_STOP";


    private final int AUDIO_RATE_HZ = 8000;                            //Sample Rate 44100,22050, 16000, and 11025Hz
    private final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;    // CHANNEL_IN_MONO, CHANNEL_IN_STEREO
    private final int AUDIO_FORMAT  = AudioFormat.ENCODING_PCM_16BIT;   //ENCODING_PCM_16BIT, ENCODING_PCM_8BIT

    private AudioRecord mRecorder;
    private int mAudioSource;
    private int mBufferSize;
    private byte[] mAudioData;
    private boolean mAudioStop;
    private Thread mRecorderThread;
    private ArrayList<Observer> mObservers;

    public AudioRecorder() {
        mAudioSource = MediaRecorder.AudioSource.MIC;
        mBufferSize = AudioRecord.getMinBufferSize(AUDIO_RATE_HZ,AUDIO_CHANNEL,AUDIO_FORMAT);

        mRecorder = new AudioRecord(mAudioSource, AUDIO_RATE_HZ,
                                        AUDIO_CHANNEL, AUDIO_FORMAT, mBufferSize);
        mAudioData = new byte[mBufferSize];

        mAudioStop = true;
        mObservers = new ArrayList<Observer>();
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

                mRecorder.startRecording();
                while(!mAudioStop) {
                    //Log.d(TAG, "1111 before recorder stream!");
                    bytes = mRecorder.read(mAudioData, 0, mBufferSize);

                    try {
                        audioStream.write(mAudioData, 0, mBufferSize);
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        break;
                    }
                }
                mRecorder.stop();
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
