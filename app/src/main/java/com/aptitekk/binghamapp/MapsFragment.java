package com.aptitekk.binghamapp;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by kevint on 8/12/2015.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap map;
    MapFragment mMapFragment;

    final LatLng bingham = new LatLng(40.5636511, -111.9455659);
    final LatLngBounds binghamGrounds = new LatLngBounds(
            new LatLng(40.562165, -111.948173),
            new LatLng(40.566093, -111.943560)
            );

    public MapsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_replaceable, container, false);
        return mainView;

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_HYBRID)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true)
                .zoomGesturesEnabled(true)
                .scrollGesturesEnabled(false)
                ;
        mMapFragment = MapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction =
                this.getActivity().getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentSpace, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setPadding(0, 10, 0, 0);
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.firstfloor_map);
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize=1;
        BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(bitmap);
        GroundOverlayOptions firstFloor = new GroundOverlayOptions()
                .image(image)
                .position(bingham, 860f, 650f)
                .transparency(1f);
        map.addGroundOverlay(firstFloor);
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(binghamGrounds, 0));


        /*map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));*/
    }

}
