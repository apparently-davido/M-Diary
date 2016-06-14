package com.example.davidoyeku.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.example.davidoyeku.custom_classes.Records;
import com.example.davidoyeku.m_diary.AddActivity;
import com.example.davidoyeku.m_diary.R;
import com.example.davidoyeku.m_diary.ViewActivityPager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.shamanland.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DavidOyeku on 03/03/15.
 */
public class TimelineFragment extends Fragment {
    private View rootView;
    private FloatingActionButton fab;
    private DisplayImageOptions options;
    private ImageLoaderConfiguration config;
    private String POSITION = "POSITION";
    private TodoCursorAdapter todoAdapter;
    private Cursor todoCursor;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //get the listview
        ListView recordsListView = (ListView) rootView.findViewById(R.id.record_list_view);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Toast.makeText(getActivity(), new Date().getDay() + "", Toast.LENGTH_SHORT).show();
        //image loader configurations
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
        todoCursor = Records.getAllCursor();//get all records
        todoAdapter = new TodoCursorAdapter(getActivity(), todoCursor); //set new cursor adapter
        recordsListView.setAdapter(todoAdapter); //set the adapter for the listview
        recordsListView.setAnimation(null);//no animations
        recordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                todoCursor.moveToPosition(position);//move cursor to same index as clicked on listview
                startActivity(new Intent(getActivity(), ViewActivityPager.class).putExtra(POSITION, String.valueOf(todoCursor.getInt(0)))); //pass the
                //cursor index id to the next activity (viewactivity pager)
            }
        });
        //settign long click for deleting an item
        recordsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String message = getString(R.string.delete_record_confirmation);
                String title = getString(R.string.delete_record_title);
                todoCursor.moveToPosition(position);
                createDialog(title, message, 1, todoCursor.getInt(0));
                return true;
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddActivity.class));

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        todoCursor = Records.getAllCursor();
        todoAdapter.swapCursor(todoCursor);
        super.onResume();
    }

    public void createDialog(String title, String message, final int action, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setTitle(title);
        builder.setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                switch (action) {

                    case 1:
                        Records record = Records.load(Records.class, position);
                        if (record != null) {
                            record.delete();
                            todoCursor = Records.getAllCursor();
                            todoAdapter.swapCursor(todoCursor);
                            todoAdapter.notifyDataSetChanged();

                        }

                    default:
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

    public class TodoCursorAdapter extends CursorAdapter {
        private TextView content, date, location, separator;
        private ImageView image;
        private SimpleDateFormat format;
        private Date d;
        private int textViewWidth;

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


            String contentString = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            String pathString = cursor.getString(cursor.getColumnIndexOrThrow("imagePath"));
            String locationString = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            Long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            Date now, prev;
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
                location.setText(locationString);
            } else {
                location.setVisibility(View.INVISIBLE);
            }
            d = new Date();
            d.setTime(dateLong);
            date.setText(format.format(d));


        }
    }
}
