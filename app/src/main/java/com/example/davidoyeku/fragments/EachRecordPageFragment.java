package com.example.davidoyeku.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.davidoyeku.m_diary.EnlargeImageViewActivity;
import com.example.davidoyeku.m_diary.R;
import com.example.davidoyeku.m_diary.VideoViewActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DavidOyeku on 22/03/15.
 * A fragment that holds and displays a record.
 */
public class EachRecordPageFragment extends Fragment {
    public static final String IMAGE_PATH = "imagePath";
    public static final String LOCATION = "location";
    public static final String AUDIO_PATH = "audio";
    public static final String VIDEO_PATH = "vidPath";
    public static final String CONTENT = "content";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String DATE = "date";
    private static GoogleMap map;
    private String imagePath, contentString, locationString, videoPathString, audioPathString;
    private Double longitude, latitude;
    private long date;
    private int id;
    private ImageView image;
    private TextView day, monthAndYear, weekdayAndTime, location, weather, content;
    private LinearLayout videoFL, audioFL, locationFL;
    private DisplayImageOptions options;
    private Date d;
    private MediaPlayer player;
    private MapView mMapView;
    private TextView audioTime;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EachRecordPageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            imagePath = getArguments().getString(IMAGE_PATH);
            contentString = getArguments().getString(CONTENT);
            date = getArguments().getLong(DATE, 0);
            d = new Date(date);
            locationString = getArguments().getString(LOCATION);
            longitude = Double.parseDouble(getArguments().getString(LONGITUDE));
            latitude = Double.parseDouble(getArguments().getString(LATITUDE));
            audioPathString = getArguments().getString(AUDIO_PATH);
            videoPathString = getArguments().getString(VIDEO_PATH);

        }

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_camera)
                .showImageForEmptyUri(R.drawable.ic_action_camera)
                .showImageOnFail(R.drawable.ic_action_camera)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).defaultDisplayImageOptions(options).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);
        //if the lat and long are null, then no need to load the map
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        //check that the lat and long are not null and they aren't empty
        if (longitude != 0 && latitude != 0 || longitude != null && latitude != null) {
//            if(longitude!=0 && latitude!=0 && longitude!=null && latitude!=null) {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mMapView.onCreate(savedInstanceState);
                    mMapView.onResume();

                    try {
                        MapsInitializer.initialize(getActivity().getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    map = mMapView.getMap();
                    MarkerOptions marker = new MarkerOptions();
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    if (latitude != null && longitude != null) {
                        marker.position(
                                new LatLng(latitude, longitude));
                        map.addMarker(marker);
                    }
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude)).zoom(12).build();
                    map.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }
            });

        } else {
            //remove the map view from the layout when they user doesnt have any location recorded!
            mMapView.setVisibility(View.GONE);
        }

        image = (ImageView) rootView.findViewById(R.id.imageview_fragment_item);
        if (!imagePath.isEmpty() && imagePath != null) { // if there is an image path
            //fetch the image with image loader
            ImageLoader.getInstance().loadImage(imagePath, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // Do whatever you want with Bitmap
                    image.setImageBitmap(loadedImage);
                    image.setAdjustViewBounds(true);
                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                }
            });//set the listener to view the image in new activity
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), EnlargeImageViewActivity.class).putExtra(IMAGE_PATH, imagePath));
                }
            });
        } else { //if no image path then just remove the image view
            image.setVisibility(View.GONE);
        }
        content = (TextView) rootView.findViewById(R.id.content_textview_viewactivity);
        day = (TextView) rootView.findViewById(R.id.day_textview_viewactivity);
        monthAndYear = (TextView) rootView.findViewById(R.id.month_and_year_textview_viewactivity);
        weekdayAndTime = (TextView) rootView.findViewById(R.id.weekday_and_time_textview_viewactivity);
        audioFL = (LinearLayout) rootView.findViewById(R.id.audio_frame_layout);
        videoFL = (LinearLayout) rootView.findViewById(R.id.video_frame_layout);
        weekdayAndTime.setText(new SimpleDateFormat("EEEE HH:mm").format(d));
        monthAndYear.setText(new SimpleDateFormat("MMMM   yyyy").format(d));
        day.setText(new SimpleDateFormat("d").format(d));
        content.setText(contentString);
        //if video path isnt empty
        if (!videoPathString.isEmpty()) {
            //set a click listener
            videoFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), VideoViewActivity.class);
                    intent.putExtra(VIDEO_PATH, videoPathString);
                    //launch video playing activity
                    startActivity(intent);
                }
            });
        } else {//if not video path then remove video layout
            videoFL.setVisibility(View.GONE);
        }
        //if audio path is not empty ,add a listnenr play audio
        if (!audioPathString.isEmpty()) {
            audioFL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playRecording();
                }
            });
        } else {
            audioFL.setVisibility(View.GONE);
        }
        return rootView;
    }

    //plays the recording on the phone
    public void playRecording() {
        player = new MediaPlayer();
        try {
            player.setDataSource(audioPathString);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        contentString = getArguments().getString(CONTENT);
        content.setText(contentString);
    }
}
