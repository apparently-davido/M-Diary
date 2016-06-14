package com.example.davidoyeku.m_diary;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by DavidOyeku on 25/03/15.
 * Actvity to display images larger
 */
public class EnlargeImageViewActivity extends ActionBarActivity {
    public static final String IMAGE_PATH = "imagePath";
    private String imagePath;
    private ImageView imageView;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enlarge_image_activity);
        //get the passed image path from previous activity
        imagePath = getIntent().getStringExtra(IMAGE_PATH);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_camera)
                .showImageForEmptyUri(R.drawable.ic_action_camera)
                .showImageOnFail(R.drawable.ic_action_camera)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(EnlargeImageViewActivity
                .this).defaultDisplayImageOptions(options).build();
        imageView = (ImageView) findViewById(R.id.imageview_enlarge);
        ImageLoader.getInstance().loadImage(imagePath, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                // Do whatever you want with Bitmap
                imageView.setImageBitmap(loadedImage);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
