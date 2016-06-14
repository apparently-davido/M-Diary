package com.example.davidoyeku.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.davidoyeku.custom_classes.Records;
import com.example.davidoyeku.m_diary.R;
import com.example.davidoyeku.m_diary.ViewActivityPager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * An image grid fragment that holds and shows all the picturs
 */
public class ImageGridFragment extends AbsListViewBaseFragment {
    private String POSITION = "POSITION";
    private DisplayImageOptions options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_camera)
                .showImageForEmptyUri(R.drawable.ic_action_camera)
                .showImageOnFail(R.drawable.ic_action_camera)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Cursor cursor = Records.getRecordWithImages();
        View rootView = inflater.inflate(R.layout.picture_fragment, container, false);
        gridView = (GridView) rootView.findViewById(R.id.grid);
        ((GridView) gridView).setAdapter(new ImageAdapter(getActivity(), cursor));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                Toast.makeText(getActivity(), "" + cursor.getInt(0), Toast.LENGTH_SHORT).show();
                //pass the id to the viewpager activity
                startActivity(new Intent(getActivity(), ViewActivityPager.class).putExtra(POSITION, String.valueOf(cursor.getInt(0))));

            }
        });
        return rootView;
    }

    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }

    public class ImageAdapter extends CursorAdapter {


        public ImageAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_grid_image, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String pathString = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));
            final ViewHolder holder;

            holder = new ViewHolder();
            assert view != null;
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
            view.setTag(holder);

            ImageLoader.getInstance()
                    .displayImage(pathString, holder.imageView, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.progressBar.setProgress(0);
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            holder.progressBar.setProgress(Math.round(100.0f * current / total));
                        }
                    });


        }


    }
}