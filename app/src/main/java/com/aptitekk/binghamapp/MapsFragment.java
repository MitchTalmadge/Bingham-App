package com.aptitekk.binghamapp;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rey.material.widget.FloatingActionButton;

public class MapsFragment extends Fragment implements OnMapReadyCallback, MainActivity.BackButtonListener/*, View.OnClickListener*/ {

    GoogleMap map;
    SupportMapFragment mMapFragment;

    GroundOverlay firstFloorOverlay = null;
    GroundOverlay secondFloorMainOverlay = null;
    GroundOverlay secondFloorVocOverlay = null;

    float firstFloorWidth = 239.5f;
    float firstFloorHeight = 179.0f;
    LatLng firstFloorPos = new LatLng(40.56372380000001, -111.94644699999999);

    float secondMainFloorWidth = 90.5f;
    float secondMainFloorHeight = 149.0f;
    LatLng secondMainFloorPos = new LatLng(40.56355879999996, -111.94651700000001);

    float secondVocFloorWidth = 43.5f;
    float secondVocFloorHeight = 159.0f;
    LatLng secondVocFloorPos = new LatLng(40.563388799999906, -111.94733200000027);

    boolean showFirstFloor = true;

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
        View mainView = inflater.inflate(R.layout.fragment_map, container, false);
        return mainView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button wUp = (Button) getView().findViewById(R.id.widthup);
        Button wDown = (Button) getView().findViewById(R.id.widthdown);
        Button hUp = (Button) getView().findViewById(R.id.heightup);
        Button hDown = (Button) getView().findViewById(R.id.heightdown);
        Button moveUp = (Button) getView().findViewById(R.id.moveup);
        Button moveDown = (Button) getView().findViewById(R.id.movedown);
        Button moveLeft = (Button) getView().findViewById(R.id.moveleft);
        Button moveRight = (Button) getView().findViewById(R.id.moveright);

        wUp.setOnClickListener(this);
        wDown.setOnClickListener(this);
        hUp.setOnClickListener(this);
        hDown.setOnClickListener(this);
        moveUp.setOnClickListener(this);
        moveDown.setOnClickListener(this);
        moveLeft.setOnClickListener(this);
        moveRight.setOnClickListener(this); FOR POSITIONING NEW OVERLAYS */

        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_HYBRID)
                .compassEnabled(true)
                .rotateGesturesEnabled(true)
                .tiltGesturesEnabled(true)
                .zoomGesturesEnabled(true)
                .scrollGesturesEnabled(true)
        ;
        mMapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map, mMapFragment);
        fragmentTransaction.addToBackStack(null);

        mMapFragment.getMapAsync(this);

        final FloatingActionButton fab_line = (FloatingActionButton)this.getView().findViewById(R.id.floorToggle);
        fab_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_line.setLineMorphingState((fab_line.getLineMorphingState() + 1) % 2, true);
                showFirstFloor = !showFirstFloor;
                updateOverlays();
            }
        });

        fragmentTransaction.commit();

        ((MainActivity) getActivity()).setBackButtonListener(this);

    }

    public void updateOverlays() {
        if(showFirstFloor) {
            if(secondFloorMainOverlay != null) {
                secondFloorMainOverlay.remove();
            }
            if(secondFloorVocOverlay != null) {
                secondFloorVocOverlay.remove();
            }
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.firstfloor_map);
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 1;
            BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(bitmap);
            GroundOverlayOptions firstFloor = new GroundOverlayOptions()
                    .image(image)
                    .position(firstFloorPos, firstFloorWidth, firstFloorHeight)
                    .transparency(0.3f);
            firstFloorOverlay = map.addGroundOverlay(firstFloor);
        } else {
            firstFloorOverlay.remove();

            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.secondfloormapmain);
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 1;
            BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(bitmap);
            GroundOverlayOptions secondFloor = new GroundOverlayOptions()
                    .image(image)
                    .position(secondMainFloorPos, secondMainFloorWidth, secondMainFloorHeight)
                    .transparency(0.3f);
            secondFloorMainOverlay = map.addGroundOverlay(secondFloor);

            Bitmap bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.secondfloormapvoc);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 1;
            BitmapDescriptor image2 = BitmapDescriptorFactory.fromBitmap(bitmap2);
            GroundOverlayOptions secondFloor1 = new GroundOverlayOptions()
                    .image(image2)
                    .position(secondVocFloorPos, secondVocFloorWidth, secondVocFloorHeight)
                    .transparency(0.3f);
            secondFloorVocOverlay = map.addGroundOverlay(secondFloor1);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setPadding(0, 10, 0, 0);
        updateOverlays();
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(binghamGrounds, 0));

        /*map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));*/
    }

    @Override
    public boolean onBackPressed() {
        ((MainActivity) getActivity()).popToMainMenu();
        return false;
    }

    /*@Override
    @Deprecated
    public void onClick(View v) {
        if (v.getId() == R.id.widthup) {
            this.firstFloorWidth = this.firstFloorWidth + 1;
        } else if (v.getId() == R.id.widthdown) {
            this.firstFloorWidth = this.firstFloorWidth - 1;
        } else if (v.getId() == R.id.heightup) {
            this.firstFloorHeight = this.firstFloorHeight + 1;
        } else if (v.getId() == R.id.heightdown) {
            this.firstFloorHeight = this.firstFloorHeight - 1;
        } else if (v.getId() == R.id.moveup) {
            bingham = new LatLng(bingham.latitude + 0.000005, bingham.longitude);
        } else if (v.getId() == R.id.movedown) {
            bingham = new LatLng(bingham.latitude - 0.000005, bingham.longitude);
        } else if (v.getId() == R.id.moveleft) {
            bingham = new LatLng(bingham.latitude, bingham.longitude + 0.000005);
        } else if (v.getId() == R.id.moveright) {
            bingham = new LatLng(bingham.latitude, bingham.longitude - 0.000005);
        }

        Log.i(MainActivity.LOG_NAME, "width: " + this.firstFloorWidth + " height: " + this.firstFloorHeight);
        Log.i(MainActivity.LOG_NAME, "Lat: " + this.bingham.latitude + " Lng: " + this.bingham.longitude);

        firstFloorOverlay.setDimensions(this.firstFloorWidth, this.firstFloorHeight);
        firstFloorOverlay.setPosition(bingham);
    }*/
}
