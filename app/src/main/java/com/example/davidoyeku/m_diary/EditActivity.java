package com.example.davidoyeku.m_diary;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.davidoyeku.custom_classes.Records;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zh.wang.android.apis.yweathergetter4a.WeatherInfo;
import zh.wang.android.apis.yweathergetter4a.YahooWeather;
import zh.wang.android.apis.yweathergetter4a.YahooWeatherInfoListener;

/**
 * Created by DavidOyeku on 25/03/15.
 * Activity that allows the user to change an existing record in database
 */
public class EditActivity extends ActionBarActivity implements YahooWeatherInfoListener {
    //request codes
    private final int SELECT_PHOTO_REQUEST_CODE = 1;
    private final int AUDIO_REQUEST_CODE = 5;
    private final int CAMERA_REQUEST_CODE = 6;
    private final int VIDEO_REQUEST_CODE = 7;
    //key for receving id of record in database
    private String POSITION = "POSITION";
    private int position;
    private ImageButton addImage, addAudio, camera, addVideo, addLocation, addWeather;
    private Button cursorLeft, cursorRight;
    private String contentString, music, imagePath, audioPath, videoPath, currentLocation;
    private EditText content;
    private DisplayImageOptions options;
    private ImageLoaderConfiguration config;
    private Records record;
    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);
    private Double longitude, latitude;
    private LocationManager mLocationManager;
    private boolean locationClicked, weatherClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.edit_entry_layout);
        getUserLocation(); //get user marks
        if (getIntent() != null) {
            //get the record id
            position = getIntent().getIntExtra(POSITION, -1);
            if (position >= 0) {
                //find the record
                record = Records.load(Records.class, position);
                //get all relevants data
                imagePath = record.imagePath;
                longitude = Double.parseDouble(record.longitude);
                latitude = Double.parseDouble(record.latitude);
                videoPath = record.vidPath;
                audioPath = record.audio;
                music = record.music;
                contentString = record.content;
            }
        }

        if (record != null) {//check the record isnt null
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_action_camera)
                    .showImageForEmptyUri(R.drawable.ic_action_camera)
                    .showImageOnFail(R.drawable.ic_action_camera)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();
            config = new ImageLoaderConfiguration.Builder(EditActivity
                    .this).defaultDisplayImageOptions(options).build();
            Toast.makeText(getApplicationContext(), position + "", Toast.LENGTH_SHORT).show();//show current cursor index
            //getting images and textview resources etc
            cursorLeft = (Button) findViewById(R.id.button_cursor_left_editactivity);
            cursorRight = (Button) findViewById(R.id.button_cursor_right_editactivity);
            content = (EditText) findViewById(R.id.content_edittext_view_editactivity);
            addImage = (ImageButton) findViewById(R.id.imagebutton_image_editactivity);
            addLocation = (ImageButton) findViewById(R.id.imagebutton_location_editactivity);
            addWeather = (ImageButton) findViewById(R.id.imagebutton_weather_editactivity);
            camera = (ImageButton) findViewById(R.id.imagebutton_camera_editactivity);
            addVideo = (ImageButton) findViewById(R.id.imagebutton_video_editactivity);
            addAudio = (ImageButton) findViewById(R.id.imagebutton_mic_editactivity);


            cursorLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = content.getSelectionStart();
                    if ((pos - 1) >= 0) {
                        content.setSelection(pos - 1);
                    }
                }
            });


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
                    //start image picking intent
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO_REQUEST_CODE);
                }
            });

            addAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditActivity.this, AudioRecordActivity.class);
//                AddActivity.this.startActivity(intent);
                    EditActivity.this.startActivityForResult(intent, AUDIO_REQUEST_CODE);

                }
            });
            addLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!locationClicked) {
                        getUserLocation();
                        addLocation.setImageResource(R.drawable.ic_action_place_green);
                        locationClicked = true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Location Removed", Toast.LENGTH_SHORT).show();
                        currentLocation = "";
                        locationClicked = false;
                    }
                }
            });
            addWeather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!weatherClicked) {
                        if (latitude != 0 && longitude != 0) {
                            getWeatherByLatLon(latitude + "", longitude + "");
                        }
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
                    startActivityForResult(i, CAMERA_REQUEST_CODE);
                }
            });
            addVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //start video recording activity
                    Intent video = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (video.resolveActivity(getPackageManager()) != null) {
                        video.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);//limit video recording to 30seconds
                        startActivityForResult(video, VIDEO_REQUEST_CODE);
                    }
                }
            });
            content.setText(contentString);

            if (!record.imagePath.isEmpty()) { //check that there is an actual image path
//                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(record.imagePath);//load it on different thread and put into bitmap
//                addImage.setBackground(new BitmapDrawable(getResources(), bitmap)); //convert to drawable and set as background
                addImage.setImageResource(R.drawable.ic_action_picture_green);
            }
        }


    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return true;
    }

    public void getUserLocation() {
        LocationListener mLocationListener = new LocationListener() {


            @Override
            public void onLocationChanged(final Location location) {
                if (location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Toast.makeText(getApplicationContext(), "Location Received", Toast.LENGTH_SHORT).show();


                    Geocoder geocoder = new Geocoder(EditActivity.this, Locale.getDefault());
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
                        Toast.makeText(getApplicationContext(), address.getLocality(), Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Toast.makeText(getApplicationContext(),"Location is null",Toast.LENGTH_SHORT).show();

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
        //  lm.requestLocationUpdates(MIN_TIME_FOR_UPDATE,MIN_DISTANCE_FOR_UPDATE,criteria,mLocationListener,Looper.myLooper());
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if (weatherInfo != null) {
            // Add your code here
            // weatherInfo object contains all information returned by Yahoo Weather apis
            Toast.makeText(getApplicationContext(), weatherInfo.getCurrentTemp() + "ºC", Toast.LENGTH_SHORT).show();
            addWeather.setImageBitmap(weatherInfo.getCurrentConditionIcon());
        }
    }

    private void getWeatherByLatLon(String lat, String lon) {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByLatLon(getApplicationContext(), lat, lon, EditActivity.this);
    }

    public void setBackGroundImage(Intent data) {
        try {
            //http://javatechig.com/android/writing-image-picker-using-intent-in-android
            final Uri imageUri = data.getData();
            imagePath = imageUri.toString();
            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            BitmapDrawable ob = new BitmapDrawable(getResources(), selectedImage);
            addImage.setBackground(ob);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setBackGroundVideo(Intent data) {
        ImageLoader.getInstance().loadImage(data.getData().toString(), options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                // Do whatever you want with Bitmap
                addVideo.setImageBitmap(loadedImage);
                addVideo.setAdjustViewBounds(true);
                addVideo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });
    }

    public void setBackGround(Intent data) {
        //http://javatechig.com/android/writing-image-picker-using-intent-in-android
        final Uri imageUri = data.getData();
        imagePath = imageUri.toString();
//            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//            BitmapDrawable ob = new BitmapDrawable(getResources(), selectedImage);
        addImage.setImageResource(R.drawable.ic_action_picture_green);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Toast.makeText(getApplicationContext(), requestCode + "", Toast.LENGTH_SHORT).show();

        switch (requestCode) {
            case SELECT_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    setBackGround(data);

                }
                break;
            case AUDIO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        audioPath = data.getStringExtra("path");
                        addAudio.setImageResource(R.drawable.ic_action_mic_green);
                        Toast.makeText(getApplicationContext(), "this is audio" + audioPath, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case VIDEO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        videoPath = data.getData().toString();
                        if (!videoPath.isEmpty()) {
                            addVideo.setImageResource(R.drawable.ic_action_video_green);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (imagePath.equals("")) {
                        //if there isnt any file path saved for image
                        //set the background and save new path
                        setBackGround(data);
                        Toast.makeText(getApplicationContext(), imagePath, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), imagePath, Toast.LENGTH_SHORT).show();
                        String prevPhoto = imagePath;
                        setBackGround(data);
                        File file = new File(prevPhoto);
                        if (file.exists()) {
                            file.delete();
                        }
                    }

                }
                break;

            default:
                //do nothing!
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_accept) {
            record.content = content.getText().toString();
            record.music = music;
            record.audio = audioPath;
            record.imagePath = imagePath;
            record.vidPath = videoPath;
            record.location = currentLocation;
            record.latitude = latitude + "";
            record.longitude = longitude + "";
            record.save();
            Toast.makeText(EditActivity.this, "updated record!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
