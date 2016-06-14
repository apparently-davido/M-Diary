package com.example.davidoyeku.m_diary;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by DavidOyeku on 12/03/15.
 * a class that records the users audio
 * saves it and then returns to previous activity
 */
public class AudioRecordActivity extends ActionBarActivity {

    private Chronometer chronometer; //the timer counter
    private ImageButton start, play; //start and play buttons
    private boolean started;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private String audioFilePath;
    private File outFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_recording_activity);
        started = false;
        //get refernce to the buttons and timer
        start = (ImageButton) findViewById(R.id.record_start_button);
        play = (ImageButton) findViewById(R.id.play_button);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        //play the audio when clicked
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecording();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started) { // if recording has not started
                    started = true; //set the flag to true
                    beginRecording(); //start recording
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();// begin the timer
                    start.setImageResource(R.drawable.ic_action_stop_dark);
                    Toast.makeText(getApplicationContext(), "started recording", Toast.LENGTH_SHORT).show();

                } else {
                    started = false; // else if it has been recording
                    chronometer.stop(); // stop the timer
                    stopRecording(); // and stop recording
                    Toast.makeText(getApplicationContext(), "" + outFile.getPath(), Toast.LENGTH_SHORT).show();
                    start.setVisibility(View.INVISIBLE);
                    play.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return true;
    }


    public void beginRecording() {

        outFile = new File(getApplicationContext().getFilesDir().toString(), "audiorecorder" + ".3gpp");
        if (outFile.exists()) {
            outFile.delete();
        }

        //start recorder
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFile(outFile.getAbsolutePath());
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        try {
            recorder.prepare();
            recorder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void stopRecording() {
        if (recorder != null) {
            //stop the recording
            recorder.stop();

        }

    }

    public void playRecording() {
        ditchMediaPlayer();
        player = new MediaPlayer();
        try {//fetch the sound
            player.setDataSource(outFile.getPath());
            player.prepare();
            player.start(); //play it
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void ditchMediaPlayer() {
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        switch (item.getItemId()) {

            case R.id.action_accept: //when the save or tick button is pressed
                packageAndReturn(); // package the recorderd data and send it to previous activity
                break;
        }

        onBackPressed();


        return true;
    }

    @Override
    public void onBackPressed() {
        packageAndReturn();
    }

    public void packageAndReturn() {
        Toast.makeText(getApplicationContext(), "Back pressed!", Toast.LENGTH_SHORT).show();
        if (outFile != null && outFile.exists() // if the file exist, not null or isnt empty
                && (!outFile.getPath().toString().equals(""))) {
            Intent intent = new Intent();
            intent.putExtra("path", audioFilePath = outFile.getPath()); //send the file to addActivity
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra("path", "");
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
