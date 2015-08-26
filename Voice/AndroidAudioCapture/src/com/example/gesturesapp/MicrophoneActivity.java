package com.example.gesturesapp;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class MicrophoneActivity extends Activity
{
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private static File file = null;

    int sampleRate = 44100; //mic sampling rate
    int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 2048/*bufferSize*/);

    FileOutputStream fos;

    boolean mRecording = false;
    boolean mStartPlaying = false;

    private MediaPlayer mPlayer = null;

    byte[] audioData = null;


    private void createNewFile() {
        file = new File(mFileName);
        if (!file.exists()) {
            try{
                file.createNewFile();
            }catch (IOException e){
                Log.e(LOG_TAG, "Unable to create file");
            }
        }
    }


    private void onRecord() {
        //if not recording
        if (!mRecording) {

            //start recording
            mRecording = true;
            int state = recorder.getState();
            try {
                recorder.startRecording();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }

               int recording = recorder.getRecordingState();
            //make the file
            createNewFile();

            //make file output stream
            try {
                fos = new FileOutputStream(mFileName, true);

            } catch (Exception e) {
                e.printStackTrace();
            }


            int readSize = recorder.read(audioData, 0, bufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize
                    && fos != null) {
                try {
                    fos.write(audioData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //while recording, write captured audio to file
            /*
            while (mRecording) {
                int readSize = recorder.read(audioData, 0, bufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize
                        && fos != null) {
                    try {
                        fos.write(audioData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            */

            //stop and release recorder
            recorder.stop();
            recorder.release();

            //close file
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        recorder.release();
        recorder = null;
    }

    */


    public MicrophoneActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/microphone.pcm";
        mRecording = false;
    }


    public void buttons() {

        final Button recordButton = (Button) findViewById(R.id.ReferenceRecord);
        recordButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mRecording = !mRecording;
                onRecord();
            }
        });

        final Button stopRecord = (Button) findViewById(R.id.ReferencePlay);
        stopRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRecording = false;
            }
        });

        /*
        final Button playButton = (Button) findViewById(R.id.ReferencePlay);
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mStartPlaying = !mStartPlaying;
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    playButton.setText("Stop Playing");
                } else {
                    playButton.setText("Play Recording");
                }
            }
        });

        */
    }

    @Override
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);

        setContentView(R.layout.activity_microphone);

        buttons();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}