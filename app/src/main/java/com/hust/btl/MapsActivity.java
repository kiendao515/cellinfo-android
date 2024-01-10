package com.hust.btl;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hust.btl.R;
import com.hust.btl.entity.Position;

import java.util.ArrayList;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;

    public MapsActivity() {
    }

    // creating array list for adding all our locations.
    private ArrayList<LatLng> locationArrayList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap ggMap) {
        this.googleMap = ggMap;
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            for(int i = 0; i < ListCellInfo.getPositionList().size(); i++) {
                this.googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(ListCellInfo.getPositionList().get(i).getLat(), ListCellInfo.getPositionList().get(i).getLon()))
                        .title(ListCellInfo.getPositionList().get(i).getAddress()));
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(ListCellInfo.getPositionList().get(i).getLat(), ListCellInfo.getPositionList().get(i).getLon()), 16.0f));
            }
        }
    }
}
