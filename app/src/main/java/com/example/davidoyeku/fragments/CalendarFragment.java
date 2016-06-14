package com.example.davidoyeku.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by DavidOyeku on 20/03/15.
 * a calendar fragment that allows the user to zoom from one date in the diary
 * to another date
 */
public class CalendarFragment extends Fragment {
    private final String TIMESTAMP = "TIMESTAMP", POSITION = "POSITION";
    private View rootView;
    private Date startOfDay, endOfDay;
    private DisplayImageOptions options; //image configurations
    private CaldroidFragment caldroidFragment;
    //list view adapter that populates the listview under the caldroid fragment
    private ArrayAdapter<Records> adapter;
    //listview to hold and display the records
    private ListView recordsListView;
    //list to hold the records returned from database query
    private List<Records> recordsList;
    private CaldroidListener listener;

    public CalendarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.calendar_fragment, container, false);
        //find the listview from layout
        recordsListView = (ListView) rootView.findViewById(R.id.calendar_listview);
        //find the dates within two times and return then as a list
        recordsList = Records.getWithinDateRange(Records.getStartOfDay(new Date()).getTime(), Records.getEndDateOfDay(new Date()).getTime());
        //get the layout view for the rows
        adapter = new MyArrayAdapter(getActivity(), R.layout.timeline_row_layout, recordsList);
        //set the adapter for the listview
        recordsListView.setAdapter(adapter);
        recordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                startActivity(new Intent(getActivity(), ViewActivityPager.class).putExtra(POSITION, String.valueOf(recordsList.get(position).getId()))); //pass the
                //cursor index id to the next activity (viewactivity pager)
            }
        });
        //image configurations
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_action_camera)
                .showImageForEmptyUri(R.drawable.ic_action_mic)
                .showImageOnFail(R.drawable.ic_action_place)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity()).defaultDisplayImageOptions(options).build();
        //making the calendar fragment
        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        //put the dates you want it to be calendar to be set as
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.add(R.id.child_fragment, caldroidFragment);
        t.commit();
//
        listener = new CaldroidListener() {

            @Override
            //when the date has been changed fetch records that fall between that date
            //then update the listview
            public void onSelectDate(Date date, View view) {
                caldroidFragment.setBackgroundResourceForDate(R.color.LightSeaGreen, date);//color its background
                startOfDay = Records.getStartOfDay(date);
                endOfDay = Records.getEndDateOfDay(date);
                Log.d("date", startOfDay.toString() + "\n" + endOfDay.toString());
                adapter.clear();
                recordsList = Records.getWithinDateRange(startOfDay.getTime(), endOfDay.getTime());
                adapter.addAll(recordsList);
                adapter.notifyDataSetChanged();
                Log.d("Size", recordsList.size() + "");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        recordsListView.invalidateViews();
                        recordsListView.refreshDrawableState();
                    }
                });


            }

            //when month has changed, colour all the months so that
            //user can identify
            @Override
            public void onChangeMonth(int month, int year) {
                Calendar c = Calendar.getInstance();
                c.set(year, month - 1, 1);
                startOfDay = c.getTime();
                c.set(month, c.getActualMaximum(month));
                endOfDay = c.getTime();
                recordsList = Records.getWithinDateRange(startOfDay.getTime(), endOfDay.getTime());
                Date d = new Date();
                for (int i = 0; i < recordsList.size(); i++) {//loop through all date
                    d.setTime(recordsList.get(i).date); // get a date
                    caldroidFragment.setBackgroundResourceForDate(R.color.LightSeaGreen, d);//color its background

                }
                caldroidFragment.refreshView();
                Toast.makeText(getActivity(), recordsList.size() + "", Toast.LENGTH_SHORT).show();
            }

            @Override
            //on long click start a add record activity
            public void onLongClickDate(Date date, View view) {
                startActivity(new Intent(getActivity(), AddActivity.class).putExtra(TIMESTAMP, date.getTime()));
            }

            @Override
            public void onCaldroidViewCreated() {
                caldroidFragment.setCalendarDate(new Date());
            }

        };
        caldroidFragment.setCaldroidListener(listener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //refresh everything so we have latest stuff
        recordsList.clear();
        caldroidFragment.refreshView();
        caldroidFragment.setCalendarDate(new Date());
    }

    private class MyArrayAdapter extends ArrayAdapter<Records> {
        SimpleDateFormat format;
        private Context mContext;
        private int layoutResourceId;
        private TextView content, date;
        private ImageView image;
        private Records r;
        private List<Records> listItem;

        public MyArrayAdapter(Context context, int resource, List<Records> objects) {
            super(context, resource, objects);
            this.layoutResourceId = resource;
            this.mContext = context;
            format = new SimpleDateFormat("HH:mm");
            listItem = objects;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                //inflate layout

                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            //get current position item
            r = listItem.get(position);
            content = (TextView) convertView.findViewById(R.id.content_text_view);
            image = (ImageView) convertView.findViewById(R.id.imageview_thumbnail);
            date = (TextView) convertView.findViewById(R.id.textview_time);
            //if there is a picture path then try and set it
            String pathString = r.imagePath.toString();
            if (pathString != null && !pathString.isEmpty()) {
                ImageLoader.getInstance().displayImage(pathString, image, options);
                image.setVisibility(View.VISIBLE);
                content.setGravity(Gravity.TOP);

            } else {
                image.setVisibility(View.GONE);
                content.setGravity(Gravity.BOTTOM);
            }
            content.setText(r.content);
            Date d = new Date();
            d.setTime(r.date);
            date.setText(format.format(d));
            return convertView;
        }
    }


}
