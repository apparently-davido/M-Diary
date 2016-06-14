package com.example.davidoyeku.m_diary;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by DavidOyeku on 17/04/15.
 * An activity to view video
 * the path is passed from prevous class
 */
public class VideoViewActivity extends ActionBarActivity {
    public static final String VIDEO_PATH = "vidPath";
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view_activity);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        String vidPath = getIntent().getStringExtra(VIDEO_PATH);// get the video path
        if (!vidPath.isEmpty()) { // if the video path is not empty
            mVideoView.setVideoURI(Uri.parse(vidPath));
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.start();// play the video
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

}


