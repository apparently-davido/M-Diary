package com.example.davidoyeku.m_diary;

import android.app.AlarmManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.davidoyeku.custom_classes.Records;
import com.example.davidoyeku.fragments.CalendarFragment;
import com.example.davidoyeku.fragments.ImageGridFragment;
import com.example.davidoyeku.fragments.MapFragment;
import com.example.davidoyeku.fragments.NavigationDrawerFragment;
import com.example.davidoyeku.fragments.SearchListFragment;
import com.example.davidoyeku.fragments.TimelineFragment;
import com.shamanland.fab.FloatingActionButton;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    // used by the actionbar
    private final String QUERY = "QUERY";
    FragmentManager fragmentManager;
    //buttons
    FloatingActionButton fab;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private AlarmManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restoreActionBar();
        //handle search bar queries
        handleIntent(getIntent());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                drawerLayout);
        drawerLayout.closeDrawers();


    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0: //launch timeline fragment
                TimelineFragment fragment = new TimelineFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
                break;
            case 1: //launch calender fragment
                CalendarFragment calendarFragment = new CalendarFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, calendarFragment)
                        .commit();
                break;
            case 2: //launch image fragment
                ImageGridFragment imageGridFragment = new ImageGridFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, imageGridFragment)
                        .commit();
                break;
            case 3: //launch map fragment
                MapFragment maps = new MapFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, maps)
                        .commit();
                break;
            case 4: //settigns activity
                MainActivity.this.startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            default:
                //do nothing
                break;
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.timeline);
                break;
            case 2:
                mTitle = getString(R.string.calendar);
                break;
            case 3:
                mTitle = getString(R.string.photos);
                break;

            case 4:
                mTitle = getString(R.string.maps);
                break;

            case 5:
                mTitle = getString(R.string.settings);
                break;
        }
    }

    //setting up the action bar
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /*
    * This is where the search query is handled
    * the query string is passed through as an intent
    * then extracted
    * */
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //search database for the keyword
            Cursor c = Records.getKeyWordCursor(query.trim());
            //search fragment
            SearchListFragment newFragment = new SearchListFragment();
            Bundle args = new Bundle();
            args.putString(QUERY, query); //pass the query string to searchlistfragment
            newFragment.setArguments(args);
            //launch the fragment
            fragmentManager.beginTransaction().replace(R.id.container, newFragment).commit();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {

            getMenuInflater().inflate(R.menu.main, menu);
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));


            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
