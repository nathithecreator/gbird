package com.example.gbird;

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
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RecordAudio extends AppCompatActivity {
    BottomNavigationView nav;

    private static int MICROSOFT_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Chronometer chronometer;
    long stopTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        nav = findViewById(R.id.nav);
        chronometer = findViewById(R.id.Chronometer);

        if (isMicrophonePresent()) {
            getMicrophonePermission();
        }

        // Set the "Explore" tab as selected
        nav.setSelectedItemId(R.id.record);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    Toast.makeText(RecordAudio.this, "Home", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(RecordAudio.this, home.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.birdentry) {
                    Toast.makeText(RecordAudio.this, "Bird Sightings", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(RecordAudio.this, myBirds.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.explore) {
                    Toast.makeText(RecordAudio.this, "Learn Birds", Toast.LENGTH_LONG).show();
                    Intent intentt = new Intent(RecordAudio.this, discovery.class);
                    startActivity(intentt);
                    finish();
                } else if (itemId == R.id.record) {
                    Toast.makeText(RecordAudio.this, "Record", Toast.LENGTH_LONG).show();
                } else {
                    // Handle other cases if needed
                }

                return true;
            }
        });

    }

    public void btnRecordPressed(View v) {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(getRecordingFilePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            mediaRecorder.prepare();
            mediaRecorder.start();

            Toast.makeText(this, "Recording Now", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnStopPressed(View v) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            chronometer.setBase(SystemClock.elapsedRealtime());
            stopTime=0;
            chronometer.stop();
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_LONG).show();
        }
    }

    public void btnPlayPressed(View v) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getRecordingFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing Audio", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isMicrophonePresent() {
        PackageManager packageManager = this.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROSOFT_PERMISSION_CODE);
        }
    }

    private String getRecordingFilePath() {
        // Define the base directory path
        File baseDirectory = new File(getFilesDir(), "Internal Storage/Recordings/Voice Recorder");

        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs(); // Create the directory if it doesn't exist
        }

        File file = new File(baseDirectory, "birdSound.mp3");
        return file.getPath();
    }
}