package com.example.davidoyeku.m_diary;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.davidoyeku.custom_classes.Records;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;

/**
 * Created by DavidOyeku on 06/03/15.
 * This is the add activity class, its purpose
 */
public class AddActivity extends ActionBarActivity implements YahooWeatherInfoListener {
    //Request codes for media activities
    private final int SELECT_PHOTO_REQUEST_CODE = 1;
    private final int AUDIO_REQUEST_CODE = 5;
    private final int CAMERA_REQUEST_CODE = 6;
    private final int VIDEO_REQUEST_CODE = 7;
    private final String TIMESTAMP = "TIMESTAMP";
    // holds the text to be entered for
    private EditText content;
    //for moving the text cursor
    private Button cursorLeft;
    private Button cursorRight;
    // boolean to know if location button has been clicked
    private boolean locationClicked;
    // the lower action bar buttons
    private ImageButton addImage, addAudio, camera, addVideo, addLocation, addWeather;
    // strings that hold value of the entries before being passed to database
    private String music, imagePath, audioPath, videoPath, currentLocation;
    // for getting song title currently being played on the persons phone
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            music = intent.getStringExtra("track");
            Toast.makeText(AddActivity.this, music, Toast.LENGTH_SHORT).show();
        }
    };
    private long timestamp;
    // for storing the users location
    private Double longitude, latitude;
    // manage the location data collection i.e location listener
    private LocationManager mLocationManager;
    private DisplayImageOptions options;
    private ImageLoaderConfiguration config;
    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);// collecting weather information from yahoo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_entry_layout);
        checkBundles(getIntent());
        restoreActionBar();
        //initiate strings to empty string
        music = "";
        imagePath = "";
        audioPath = "";
        videoPath = "";
        content.setBackgroundDrawable(null);
        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        checkCurrentTrack(iF);
        getUserLocation();

        registerReceiver(mReceiver, iF);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_camera)
                .showImageForEmptyUri(R.drawable.ic_action_camera)
                .showImageOnFail(R.drawable.ic_action_camera)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        config = new ImageLoaderConfiguration.Builder(AddActivity
                .this).defaultDisplayImageOptions(options).build();
        //find ui components
        content = (EditText) findViewById(R.id.content_edittext_view);
        cursorLeft = (Button) findViewById(R.id.button_cursor_left);
        cursorRight = (Button) findViewById(R.id.button_cursor_right);
        addImage = (ImageButton) findViewById(R.id.imagebutton_image);
        addAudio = (ImageButton) findViewById(R.id.imagebutton_mic);
        camera = (ImageButton) findViewById(R.id.imagebutton_camera);
        addVideo = (ImageButton) findViewById(R.id.imagebutton_video);
        addLocation = (ImageButton) findViewById(R.id.imagebutton_location);
        addLocation.setImageResource(R.drawable.ic_action_place_green);
        addWeather = (ImageButton) findViewById(R.id.imagebutton_weather);
        addWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latitude != 0 && longitude != 0) {// if the location is not empty get the weather
                    getWeatherByLatLon(latitude + "", longitude + "");
                }
            }
        });

        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationClicked) {
                    //if location is not clicked
                    getUserLocation(); //then get the location
                    addLocation.setImageResource(R.drawable.ic_action_place_green);
                    locationClicked = true;
                } else {
                    //else remove it and go back to default settings
                    Toast.makeText(getApplicationContext(), "Location Removed", Toast.LENGTH_SHORT).show();
                    currentLocation = "";
                    addLocation.setImageResource(R.drawable.ic_action_place);
                    locationClicked = false;
                }
            }
        });

        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent video = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (video.resolveActivity(getPackageManager()) != null) {
                    //start video capturing activity, the result is sent to on activity result
                    startActivityForResult(video, VIDEO_REQUEST_CODE);
                }
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues val = new ContentValues();
                val.put(MediaStore.Images.Media.TITLE, "DiaryPhoto");
                val.put(MediaStore.Images.Media.DESCRIPTION, "Image captured for M-Diary");
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //start the camera for pciture
                startActivityForResult(i, CAMERA_REQUEST_CODE);
            }
        });

        addAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start audio recording activity
                Intent intent = new Intent(AddActivity.this, AudioRecordActivity.class);
                AddActivity.this.startActivityForResult(intent, AUDIO_REQUEST_CODE);

            }
        });

        //used for moving the cursor left
        cursorLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = content.getSelectionStart();
                if ((pos - 1) >= 0) {
                    content.setSelection(pos - 1);
                }
            }
        });

        //ysed for moving the cursor the the right
        cursorRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = content.getText().length();
                int pos = content.getSelectionStart();
                if ((pos + 1) <= length) {
                    content.setSelection(pos + 1);
                }
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO_REQUEST_CODE);


            }
        });
        //Long Click Listeners
        addImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String title = getString(R.string.remove_photo_dialog_title);
                String message = getString(R.string.remove_photo_dialog_message);

                createDialog(title, message, SELECT_PHOTO_REQUEST_CODE);
                return true;
            }
        });

        addVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String title = getString(R.string.remove_video_dialog_title);
                String message = getString(R.string.remove_video_dialog_message);

                createDialog(title, message, VIDEO_REQUEST_CODE);
                return true;
            }
        });

        addAudio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String title = getString(R.string.remove_audio_dialog_title);
                String message = getString(R.string.remove_audio_dialog_message);

                createDialog(title, message, AUDIO_REQUEST_CODE);
                return true;
            }
        });


    }

    private void checkBundles(Intent intent) {
        timestamp = intent.getLongExtra(TIMESTAMP, 0);
        Toast.makeText(getApplicationContext(), "" + new Date(timestamp).toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Collect data from media activity here!! e.g photo path, video path etc
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Toast.makeText(getApplicationContext(), requestCode + "", Toast.LENGTH_SHORT).show();

        switch (requestCode) {
            // if a photo picker activity was just used
            case SELECT_PHOTO_REQUEST_CODE:
                //and there is no error
                if (resultCode == RESULT_OK) {
                    //remove the uri and change the icon
                    setBackGround(data);
                }
                break;
            //if it is an audio activity
            case AUDIO_REQUEST_CODE:
                //and err thing is ok
                if (resultCode == RESULT_OK) {
                    try {
                        //try to get the path
                        audioPath = data.getStringExtra("path");
                        Toast.makeText(getApplicationContext(), "audio saved!", Toast.LENGTH_SHORT).show();
                        if (!audioPath.isEmpty()) {
                            addAudio.setImageResource(R.drawable.ic_action_mic_green); //change the icon
                        }
                    } catch (Exception e) {
                        Log.d("audio", "the audio path was probably null");
                        e.printStackTrace();
                    }
                }
                break;
            //if a video activity has been used
            case VIDEO_REQUEST_CODE:
                // and evrthing is ok
                if (resultCode == RESULT_OK) {
                    try {// try and get the video path
                        videoPath = data.getData().toString();
                        if (!videoPath.isEmpty()) { // if the path is not emoty
                            //change background
                            addVideo.setImageResource(R.drawable.ic_action_video_green);
                        }
                        Toast.makeText(getApplicationContext(), videoPath, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            //if camera has just been used
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    setBackGround(data);
                    //if the image path is not empty
                    if (!imagePath.equals("")) {

                        Toast.makeText(getApplicationContext(), imagePath, Toast.LENGTH_SHORT).show();
                        //check icon
                        addImage.setBackgroundResource(R.drawable.ic_action_picture_green);
                    }

                }
                break;

            default:
                //do nothing!
                break;
        }
    }

    public void restoreActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        SimpleDateFormat format = new SimpleDateFormat("d  MMMM   yyyy");
        if (timestamp == 0) {
            actionBar.setTitle(format.format(new Date()));
        } else {//change the time to date from caldroid if timestamp isnt 0
            Date d = new Date();
            d.setTime(timestamp);
            actionBar.setTitle(format.format(d));
        }
        actionBar.setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_accept) {
            //get ready to store in database
            Records r = new Records();
            //get string
            r.content = content.getText().toString();
            //if no date passed from caldroid
            if (timestamp == 0) {
                r.date = new Date().getTime();//use current time
            } else {
                r.date = timestamp;//else use caldroid passed on from calendar fragment
            }
            //map every data gathered from this activity
            //to their respective column in database
            r.music = music;
            r.audio = audioPath;
            r.imagePath = imagePath;
            r.vidPath = videoPath;
            r.location = currentLocation;
            r.latitude = latitude + "";
            r.longitude = longitude + "";
            r.save();//save to database
            Toast.makeText(AddActivity.this, "Saved to database!", Toast.LENGTH_SHORT).show();
            this.finish(); //close the activity and return to parent activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setBackGround(Intent data) {
        // grab and save the uri path
        final Uri imageUri = data.getData();
        imagePath = imageUri.toString();
        addImage.setImageResource(R.drawable.ic_action_picture_green);//change color to green to notify user

    }


    /**
     * dialogs for removing data from the bottom menu bar
     * <p/>
     * **
     */
    public void createDialog(String title, String message, final int action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setMessage(message).setTitle(title);
        builder.setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                switch (action) {
                    case SELECT_PHOTO_REQUEST_CODE:
                        if (!imagePath.isEmpty()) {//if image pathisnt empty
                            imagePath = ""; //make it empty
                            addImage.setBackground(null); //remove the image background
                            Toast.makeText(getApplicationContext(), "Image Removed!", Toast.LENGTH_SHORT).show();// notify user
                            addImage.setImageResource(R.drawable.ic_action_picture); //set the picture back to default
                        } else {
                            Toast.makeText(getApplicationContext(), "No image to remove", Toast.LENGTH_SHORT).show(); //notify user
                        }
                        break;

                    case AUDIO_REQUEST_CODE:
                        if (!audioPath.isEmpty()) {//if audio path is not empty
                            audioPath = "";//make it empty
                            Toast.makeText(getApplicationContext(), "Audio Removed!", Toast.LENGTH_SHORT).show();
                            addAudio.setImageResource(R.drawable.ic_action_mic); //reset to default icon
                        } else {
                            Toast.makeText(getApplicationContext(), "No audio to remove", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case VIDEO_REQUEST_CODE:
                        if (!videoPath.isEmpty()) { //if video path is not empty
                            videoPath = ""; //make it empty
                            Toast.makeText(getApplicationContext(), "Video Removed!", Toast.LENGTH_SHORT).show(); // notify user
                            addVideo.setImageResource(R.drawable.ic_action_video); //set image to default
                        } else {
                            Toast.makeText(getApplicationContext(), "No video to remove", Toast.LENGTH_SHORT).show(); //notify user
                        }
                        break;

                    default:
                        // do nothing
                        break;

                }

            }
        });
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * This method grabs the users location from
     * and stores it in the currentLocation string variable
     * *
     */

    public void getUserLocation() {
        LocationListener mLocationListener = new LocationListener() {


            @Override
            public void onLocationChanged(final Location location) {
                //cross check the data returned
                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    locationClicked = true;
                    Toast.makeText(getApplicationContext(), "Location Received", Toast.LENGTH_SHORT).show();
                    Geocoder geocoder = new Geocoder(AddActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (addresses == null || addresses.size() == 0) {
                        currentLocation = "";
                        Log.e("ERRRRROOOORRRR", "Address is either null or there are no addresses");
                    } else {
                        Address address = addresses.get(0);
                        ArrayList<String> addressFragments = new ArrayList<String>();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            addressFragments.add(address.getAddressLine(i));
                        }
                        currentLocation = TextUtils.join(System.getProperty("line.separator"),
                                addressFragments).toString();
                    }
                } else {
                    Log.d("location null", "the location returned from getUserLocation is null");

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);


        mLocationManager.requestSingleUpdate(provider, mLocationListener, Looper.myLooper());
    }

    @Override
    /**
     * where the weather information is returned,
     * extract all the data needed here.
     * **/

    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        //check it isnt empty
        if (weatherInfo != null) {
            Toast.makeText(getApplicationContext(), weatherInfo.getCurrentTemp() + "ºC", Toast.LENGTH_SHORT).show();
            addWeather.setImageBitmap(weatherInfo.getCurrentConditionIcon());
        }
    }

    /*
    * gets the user location by passing the longitude and latitude
    * the result is returned to the method gotWeatherInfo
    *
    * **/
    private void getWeatherByLatLon(String lat, String lon) {
        //download the icon of the weather
        mYahooWeather.setNeedDownloadIcons(true);
        //produce the weather results in celsius
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByLatLon(getApplicationContext(), lat, lon, AddActivity.this);
    }


    public void checkCurrentTrack(IntentFilter iF) {
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");

        iF.addAction("com.htc.music.metachanged");

        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");

        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.metachanged");
        //HTC Music
        iF.addAction("com.htc.music.playstatechanged");
        iF.addAction("com.htc.music.playbackcomplete");
        iF.addAction("com.htc.music.metachanged");
        //MIUI Player
        iF.addAction("com.miui.player.playstatechanged");
        iF.addAction("com.miui.player.playbackcomplete");
        iF.addAction("com.miui.player.metachanged");
        //Real
        iF.addAction("com.real.IMP.playstatechanged");
        iF.addAction("com.real.IMP.playbackcomplete");
        iF.addAction("com.real.IMP.metachanged");
        //SEMC Music Player
        iF.addAction("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED");
        iF.addAction("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED");
        iF.addAction("com.sonyericsson.music.TRACK_COMPLETED");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.sonyericsson.music.playbackcomplete");
        iF.addAction("com.sonyericsson.music.playstatechanged");
        //rdio
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.rdio.android.playstatechanged");
        //Samsung Music Player
        iF.addAction("com.samsung.sec.android.MusicPlayer.playstatechanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.playbackcomplete");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.sec.android.app.music.playstatechanged");
        iF.addAction("com.sec.android.app.music.playbackcomplete");
        iF.addAction("com.sec.android.app.music.metachanged");
        //Winamp
        iF.addAction("com.nullsoft.winamp.playstatechanged");
        //Amazon
        iF.addAction("com.amazon.mp3.playstatechanged");
        //Rhapsody
        iF.addAction("com.rhapsody.playstatechanged");
        //PowerAmp
        iF.addAction("com.maxmpz.audioplayer.playstatechanged");
        //will be added any....
        //scrobblers detect for players (poweramp for example)
        //Last.fm
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("fm.last.android.playbackpaused");
        iF.addAction("fm.last.android.playbackcomplete");
        //A simple last.fm scrobbler
        iF.addAction("com.adam.aslfms.notify.playstatechanged");
        //Scrobble Droid
        iF.addAction("net.jjc1138.android.scrobbler.action.MUSIC_STATUS");
    }

}
