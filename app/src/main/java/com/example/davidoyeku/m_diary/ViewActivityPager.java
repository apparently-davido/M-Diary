package com.example.davidoyeku.m_diary;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.davidoyeku.custom_classes.CursorPagerAdapter;
import com.example.davidoyeku.custom_classes.Records;
import com.example.davidoyeku.fragments.EachRecordPageFragment;


/**
 * Created by DavidOyeku on 13/03/15.
 */
public class ViewActivityPager extends ActionBarActivity {
    private final int UPDATE_REQUEST = 10;
    private String POSITION = "POSITION";
    private int position, id;
    private Cursor cursor;
    private ViewPager vpPager;
    private CursorPagerAdapter<EachRecordPageFragment> adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_entry_activity);
        vpPager = (ViewPager) findViewById(R.id.pager);
        cursor = Records.getAllCursor();// get all the records
        position = Integer.parseInt(getIntent().getStringExtra(POSITION));
        if (cursor != null) { //check cursor isnt null
            while (cursor.moveToNext()) {//iterate through the cursor

                if (cursor.getInt(0) == position) { // if the record id clicked on previous activity is found in thsi cursor
                    Toast.makeText(getApplicationContext(), cursor.getInt(0) + "  " + position, Toast.LENGTH_SHORT).show();
                    id = cursor.getInt(0); // set the id to our found id
                    break;
                }
            }
            adapterViewPager = new CursorPagerAdapter(getSupportFragmentManager(), EachRecordPageFragment.class, cursor);
            vpPager.setAdapter(adapterViewPager);
            vpPager.setCurrentItem(cursor.getPosition());// move the view pager to the found id position

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.viewactivitymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.edit_record:
                Intent intent = new Intent(ViewActivityPager.this, EditActivity.class);
                //check we are not going out of bounds
                if (vpPager.getCurrentItem() <= cursor.getCount()) {
                    cursor.moveToPosition(vpPager.getCurrentItem());//move the cursor to the record we want to edit
                    intent.putExtra(POSITION, cursor.getInt(0)); /// send the id to the edit activity
                    ViewActivityPager.this.startActivityForResult(intent, UPDATE_REQUEST);//start the goddamn activity
                    break;
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UPDATE_REQUEST:    //used for after we edit a record and we come back to the viewing page
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    cursor = Records.getAllCursor();
                    int intPrevPos = vpPager.getCurrentItem();
                    adapterViewPager = new CursorPagerAdapter(getSupportFragmentManager(), EachRecordPageFragment.class, cursor);
                    vpPager.setAdapter(adapterViewPager);
                    vpPager.setCurrentItem(intPrevPos);
                }
        }
    }
}
