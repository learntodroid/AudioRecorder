package com.learntodroid.audiorecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean audioRecordingPermissionGranted = false;

    private String fileName;
    private Button startRecordingButton, stopRecordingButton, playRecordingButton, stopPlayingButton;
    private MediaRecorder recorder;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        setContentView(R.layout.activity_main);

        startRecordingButton = findViewById(R.id.activity_main_record);
        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        stopRecordingButton = findViewById(R.id.activity_main_stop);
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
            }
        });

        playRecordingButton = findViewById(R.id.activity_main_play);
        playRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRecording();
            }
        });

        stopPlayingButton = findViewById(R.id.activity_main_stop_playing);
        stopPlayingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                audioRecordingPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!audioRecordingPermissionGranted) {
            finish();
        }
    }

    private void startRecording() {
        String uuid = UUID.randomUUID().toString();
        fileName = getExternalCacheDir().getAbsolutePath() + "/" + uuid + ".3gp";
        Log.i(MainActivity.class.getSimpleName(), fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":startRecording()", "prepare() failed");
        }

        recorder.start();

        startRecorderService();
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
            stopRecorderService();
        }
    }

    private void playRecording() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlaying();
                }
            });
            player.prepare();
            player.start();
            startPlayerService();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":playRecording()", "prepare() failed");
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
            stopPlayerService();
        }
    }

    private void startRecorderService() {
        Intent serviceIntent = new Intent(this, RecorderService.class);
        serviceIntent.putExtra("inputExtra", "Recording in progress");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopRecorderService() {
        Intent serviceIntent = new Intent(this, RecorderService.class);
        stopService(serviceIntent);
    }

    private void startPlayerService() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.putExtra("inputExtra", "Playing recording");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopPlayerService() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        stopService(serviceIntent);
    }
}