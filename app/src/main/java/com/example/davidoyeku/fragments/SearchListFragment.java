package com.example.davidoyeku.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.davidoyeku.custom_classes.Records;
import com.example.davidoyeku.m_diary.R;
import com.example.davidoyeku.m_diary.ViewActivityPager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DavidOyeku on 16/04/15.
 */
public class SearchListFragment extends Fragment {
    private final String QUERY = "QUERY";
    private final String POSITION = "POSITION";
    private View rootView;
    private DisplayImageOptions options;
    private String keyWord;
    private Cursor todoCursor;
    private TodoCursorAdapter todoAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        keyWord = getArguments().getString(QUERY);
//        if(!ImageLoader.getInstance().isInited()) {
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_camera)
                .showImageForEmptyUri(R.drawable.ic_action_camera)
                .showImageOnFail(R.drawable.ic_action_camera)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.
                Builder(getActivity()).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config); // Do it on Application start
//        }
        todoCursor = Records.getKeyWordCursor(keyWord);
        todoAdapter = new TodoCursorAdapter(getActivity(), todoCursor);
        ListView recordsListView = (ListView) rootView.findViewById(R.id.record_list_view);
        recordsListView.setAdapter(todoAdapter);
        recordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                todoCursor.moveToPosition(position);
                startActivity(new Intent(getActivity(), ViewActivityPager.class).putExtra(POSITION, String.valueOf(todoCursor.getInt(0))));
            }
        });


        return rootView;
    }


    public class TodoCursorAdapter extends CursorAdapter {
        private TextView content, date, location, separator;
        private ImageView image;
        private SimpleDateFormat format;
        private Date d;
        private int textViewWidth;
        private String contentString;
        private String pathString;
        private String locationString;
        private Long dateLong;
        private Date now, prev;


        public TodoCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
            format = new SimpleDateFormat("HH:mm");
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.timeline_row_layout, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            content = (TextView) view.findViewById(R.id.content_text_view);
            textViewWidth = content.getWidth();
            image = (ImageView) view.findViewById(R.id.imageview_thumbnail);
            date = (TextView) view.findViewById(R.id.textview_time);
            location = (TextView) view.findViewById(R.id.textview_location);
            separator = (TextView) view.findViewById(R.id.separator);
            // get data for each an every row
            contentString = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            pathString = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));
            locationString = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            dateLong = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            now = new Date(dateLong);
            prev = new Date();


            Long prevDate = null;
            if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
                prevDate = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                prev.setTime(prevDate);
                cursor.moveToNext();

            }
            if (prevDate == null || prev.getDay() != now.getDay()) {
                separator.setVisibility(View.VISIBLE);
                separator.setText(new SimpleDateFormat("d  MMMM").format(now));
            } else {
                separator.setVisibility(View.GONE);
            }


            //if there is a picture path then try and set it
            if (pathString != null && !pathString.isEmpty()) {
                ImageLoader.getInstance().displayImage(pathString, image, options);
                image.setVisibility(View.VISIBLE);
                content.setGravity(Gravity.TOP);


            } else {
                //image.setImageResource(R.drawable.ic_action_camera);
                image.setVisibility(View.GONE);
                content.setGravity(Gravity.BOTTOM);

            }
            content.setText(contentString);

            if (locationString != null && !locationString.isEmpty()) {
                location.setText(locationString);//show location if available
            } else {
                location.setVisibility(View.INVISIBLE); // remove them if not available
            }
            d = new Date();
            d.setTime(dateLong);
            date.setText(format.format(d));


        }
    }

}
