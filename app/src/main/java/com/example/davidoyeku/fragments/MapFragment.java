package com.example.davidoyeku.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.davidoyeku.custom_classes.Records;
import com.example.davidoyeku.m_diary.R;
import com.example.davidoyeku.m_diary.ViewActivityPager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by DavidOyeku on 29/03/15.
 */
public class MapFragment extends android.support.v4.app.Fragment {
    private static GoogleMap map;
    private View rootView;
    private MapView mMapView;
    private Double latitude, longitude;
    private String POSITION = "POSITION";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.map_fragment, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor cursor = Records.getAllCursor(); // get all the records so we can get their location
        map = mMapView.getMap();
        //marker to pin point location on map
        MarkerOptions marker = new MarkerOptions();
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        while (cursor.moveToNext()) { // while there is more records
            //convert the longitude and lattitude to doubles
            longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("longitude")));
            latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow("latitude")));
            //when lat and long are  not null
            if ((latitude != null && longitude != null) || (latitude != 0.0 && longitude == 0.0)) {
                //put marker on the map
                marker.position(
                        new LatLng(latitude, longitude)).title(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                marker.snippet(cursor.getInt(0) + "");

                map.addMarker(marker);
            }

        }

        GoogleMap.OnMarkerClickListener markListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker != null) {
                    Toast.makeText(getActivity(), "marker cliked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), ViewActivityPager.class).putExtra(POSITION, marker.getSnippet() + ""));
                }
                return true;
            }
        };
        map.setOnMarkerClickListener(markListener);
        cursor.close();
//camera position and animation
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(12).build();
        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        return rootView;
    }


}
