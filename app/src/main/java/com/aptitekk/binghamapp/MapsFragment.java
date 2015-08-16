package com.aptitekk.binghamapp;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.EditText;
import com.rey.material.widget.FloatingActionButton;

public class MapsFragment extends Fragment implements OnMapReadyCallback, MainActivity.BackButtonListener/*, GoogleMap.OnMapLongClickListener, View.OnClickListener*/ {

    GoogleMap map;
    SupportMapFragment mMapFragment;

    GroundOverlay firstFloorOverlay = null;
    GroundOverlay secondFloorMainOverlay = null;
    GroundOverlay secondFloorVocOverlay = null;

    float firstFloorWidth = 236.5f;
    float firstFloorHeight = 181.0f;
    LatLng firstFloorPos = new LatLng(40.5637438, -111.94642);

    float secondFloorWidth = 56.5f;
    float secondFloorHeight = 139.0f;
    LatLng secondFloorPos = new LatLng(40.563567, -111.94668);

    float secondFloorVocWidth = 35.5f;
    float secondFloorVocHeight = 23.0f;
    LatLng secondFloorVocPos = new LatLng(40.5639138, -111.947342);

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
        moveRight.setOnClickListener(this);*/ //For adjusting new images

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

        final FloatingActionButton fab_line = (FloatingActionButton) this.getView().findViewById(R.id.floorToggle);
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
        if (showFirstFloor) {
            if (secondFloorMainOverlay != null) {
                secondFloorMainOverlay.remove();
            }
            if (secondFloorVocOverlay != null) {
                secondFloorVocOverlay.remove();
            }
            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_floor_1);
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

            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_floor_2);
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 1;
            BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(bitmap);
            GroundOverlayOptions secondFloor = new GroundOverlayOptions()
                    .image(image)
                    .position(secondFloorPos, secondFloorWidth, secondFloorHeight)
                    .transparency(0.3f);
            secondFloorMainOverlay = map.addGroundOverlay(secondFloor);

            Bitmap bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.map_floor_2_voc);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 1;
            BitmapDescriptor image2 = BitmapDescriptorFactory.fromBitmap(bitmap2);
            GroundOverlayOptions secondFloor1 = new GroundOverlayOptions()
                    .image(image2)
                    .position(secondFloorVocPos, secondFloorVocWidth, secondFloorVocHeight)
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
    public void onMapLongClick(final LatLng latLng) {
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Marker"));
        Dialog.Builder builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {

            @Override
            protected void onBuildDone(Dialog dialog) {
                dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }


            @Override
            public void onPositiveActionClicked(DialogFragment fragment) {
                EditText roomNumber = (EditText) fragment.getDialog().findViewById(R.id.text_input);

                Log.i(MainActivity.LOG_NAME, roomNumber.getText().toString() + " @ Lat: " + latLng.latitude + " Long: " + latLng.longitude);

                super.onPositiveActionClicked(fragment);
            }


            @Override
            public void onNegativeActionClicked(DialogFragment fragment) {
                super.onNegativeActionClicked(fragment);
            }
        };


        builder.title("Marker Dropped")
                .positiveAction("RECORD")
                .negativeAction("CANCEL")
                .contentView(R.layout.dialog_text_input);
        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getFragmentManager(), null);


    }*/

    /*@Override
    @Deprecated
    public void onClick(View v) {
        if (v.getId() == R.id.widthup) {
            this.secondFloorVocWidth = this.secondFloorVocWidth + 1;
        } else if (v.getId() == R.id.widthdown) {
            this.secondFloorVocWidth = this.secondFloorVocWidth - 1;
        } else if (v.getId() == R.id.heightup) {
            this.secondFloorVocHeight = this.secondFloorVocHeight + 1;
        } else if (v.getId() == R.id.heightdown) {
            this.secondFloorVocHeight = this.secondFloorVocHeight - 1;
        } else if (v.getId() == R.id.moveup) {
            secondFloorVocPos = new LatLng(secondFloorVocPos.latitude + 0.000005, secondFloorVocPos.longitude);
        } else if (v.getId() == R.id.movedown) {
            secondFloorVocPos = new LatLng(secondFloorVocPos.latitude - 0.000005, secondFloorVocPos.longitude);
        } else if (v.getId() == R.id.moveleft) {
            secondFloorVocPos = new LatLng(secondFloorVocPos.latitude, secondFloorVocPos.longitude + 0.000005);
        } else if (v.getId() == R.id.moveright) {
            secondFloorVocPos = new LatLng(secondFloorVocPos.latitude, secondFloorVocPos.longitude - 0.000005);
        }

        Log.i(MainActivity.LOG_NAME, "width: " + this.secondFloorVocWidth + " height: " + this.secondFloorVocHeight);
        Log.i(MainActivity.LOG_NAME, "Lat: " + this.secondFloorVocPos.latitude + " Lng: " + this.secondFloorVocPos.longitude);

        firstFloorOverlay.setDimensions(this.secondFloorVocWidth, this.secondFloorVocHeight);
        firstFloorOverlay.setPosition(secondFloorVocPos);
    }*/ //For adjusting new images
}
