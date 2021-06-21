package com.softgyan.findcallers.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class CallRecorder {

    private static final String TAG = "CallRecorder";

    private final Context context;
    private final AudioManager audioManager;
    private final int[] output_formats = {MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.OutputFormat.THREE_GPP};
    private final String[] file_exts = {AUDIO_RECORDER_FILE_EXT_MP4,
            AUDIO_RECORDER_FILE_EXT_3GP};
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "FindCallers";
    private MediaRecorder recorder = null;
    private final int currentFormat = 1;

    public CallRecorder(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);
        Log.d(TAG, "CallRecorder: call recorder initiative");
    }

    public void stopRecording() {
        Log.d(TAG, "stopRecording: called 1");
        audioManager.setSpeakerphoneOn(false);
        try {
            if (null != recorder) {
                recorder.stop();
                recorder.reset();
                recorder.release();

                recorder = null;

                Log.d(TAG, "stopRecording: stop call recording");
            } else {
                Log.d(TAG, "stopRecording: getting null value");
            }
        } catch (RuntimeException stopException) {
            Log.d(TAG, "stopRecording: error : " + stopException.getMessage());
        }
    }


    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        String fileName = getFilename();
        Log.d(TAG, "startRecording: file name : " + fileName);
        recorder.setOutputFile(fileName);
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
            Log.d(TAG, "startRecording: start recording");
        } catch (IllegalStateException | IOException e) {
            Log.e("REDORDING :: ", e.getMessage());
            e.printStackTrace();
        }
    }


    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private final MediaRecorder.OnErrorListener errorListener = (mr, what, extra) -> {
        Log.d(TAG, "onError: " + "Error: " + what + ", " + extra);
    };

    private final MediaRecorder.OnInfoListener infoListener = (mr, what, extra) -> {
        Log.d(TAG, "onError: " + "Error: " + what + ", " + extra);
    };

}
