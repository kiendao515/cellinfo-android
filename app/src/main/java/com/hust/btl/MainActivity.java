package com.hust.btl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.hust.btl.databinding.ActivityMapsBinding;
import com.hust.btl.entity.CellTower;
import com.hust.btl.entity.GetPositionResponse;
import com.hust.btl.entity.Position;
import com.hust.btl.service.ApiManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ActivityMapsBinding binding;
    private GoogleMap googleMap;
    private List<CellTower> cellTower;
    private static List<Position> position;
    private ApiManager apiManager;
    private String url = "https://api.findcellid.com/api/look_up";
    private Handler handler;
    private Runnable getCellInfoRunnable;
    private boolean isMapInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiManager = new ApiManager(this);
        handler = new Handler(Looper.getMainLooper());
        getCellInfoRunnable = new Runnable() {
            @Override
            public void run() {
                // Assuming getCellInfoAsync is a method that fetches cell information
                getCellInfoAsync(new CellInfoCallback() {
                    @Override
                    public void onCellInfoReceived(List<CellTower> receivedCellTower) {
                        cellTower = receivedCellTower;
                        getPositionFromCellInfo(cellTower,new GetPositionCallBack(){
                            @Override
                            public void onPositionReceived(List<Position> p) {
                                position = p;
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update your view with the received data on the UI thread
                                updateCellTowerDataInView(cellTower);
                            }
                        });
                        // Schedule the next execution
                        handler.postDelayed(getCellInfoRunnable, 20000); // Schedule to run again after 5 seconds
                    }
                });
            }
        };

        // Start the periodic task immediately when activity starts
        handler.post(getCellInfoRunnable);

//        Button showOnMapButton = findViewById(R.id.btnShowOnMap);
//        showOnMapButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!isMapInitialized) {
//                    initMap();
//                    isMapInitialized = true;
//                }
//                showMap();
//            }
//        });
    }

    private void updateCellTowerDataInView(List<CellTower> cellTowers) {
        LinearLayout container = findViewById(R.id.linear_layout_container);
        if(container!= null){
            container.removeAllViews();
        }
//      container.removeAllViews(); // Clear existing views

        for (CellTower tower : cellTowers) {
            // Inflate the card view layout
            View cardView = getLayoutInflater().inflate(R.layout.card_view_layout, container, false);

            // Find the TextViews in the card layout
            TextView tvCellId = cardView.findViewById(R.id.tvCellId);
            TextView tvLocationAreaCode = cardView.findViewById(R.id.tvLocationAreaCode);
            TextView tvMobileCountryCode = cardView.findViewById(R.id.tvMobileCountryCode);
            TextView tvMobileNetworkCode = cardView.findViewById(R.id.tvMobileNetworkCode);

            // Set the text for each TextView using the data from the current tower in the loop
            tvCellId.setText(String.valueOf(tower.getCellId()));
            tvMobileNetworkCode.setText(String.valueOf(tower.getMobileNetworkCode()));
            tvMobileCountryCode.setText(String.valueOf(tower.getMobileCountryCode()));
            tvLocationAreaCode.setText(String.valueOf(tower.getLocationAreaCode()));

            // Add the card view to the container
            container.addView(cardView);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Runnable when the activity is destroyed to avoid memory leaks
        if (handler != null) {
            handler.removeCallbacks(getCellInfoRunnable);
        }
    }


    public interface CellInfoCallback {
        void onCellInfoReceived(List<CellTower> cellTower);
    }
    public interface GetPositionCallBack{
        void onPositionReceived(List<Position> position);
    }

    private void getCellInfoAsync(final CellInfoCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<CellTower> cellTower = getCellInfo();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null){
                            callback.onCellInfoReceived(cellTower);
                            Toast.makeText(MainActivity.this, "Get cell info successfully ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }
    private void getPositionFromCellInfo(List<CellTower> cellTower, final GetPositionCallBack apiCallback){
        List<Position> positionList= new ArrayList<>();
        Position p= new Position();
        for(int i=0;i< cellTower.size();i++){
            Log.d("cell info tower",cellTower.get(i).toString());
            apiManager.makeApiCall(cellTower.get(i).getMobileNetworkCode(), cellTower.get(i).getMobileCountryCode(),
                    cellTower.get(i).getLocationAreaCode(), cellTower.get(i).getCellId(), "auto", new ApiManager.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.d("call api", response.toString());
                            Gson gson = new Gson();
                            GetPositionResponse apiResponse = gson.fromJson(response.toString(),GetPositionResponse.class);
                            p.setAddress(apiResponse.getAddress());
                            p.setLat(apiResponse.getLat());
                            p.setLon(apiResponse.getLon());
                            positionList.add(p);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Handle API error
                        }
                    });
            apiCallback.onPositionReceived(positionList);
        }

    }
    private List<CellTower> getCellInfo(){
        List<CellTower> tower = Collections.synchronizedList(new ArrayList<>());
//        List <CellTower> tower = null;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            if (cellInfoList == null) {
                Log.i("CellInfo","error in fetching cellinfo");
            } else if (cellInfoList.size() == 0) {
                Log.i("CellInfo","empty cell info list");
            } else {
                for(int i =0;i< cellInfoList.size();i++){
                    CellTower c= bindData(cellInfoList.get(i));
                    tower.add(c);
                }
                Log.d("Tower info",tower.toString());
            }
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
        return tower;
    }

    private void initMap() {
        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(GoogleMap ggMap) {
        this.googleMap = ggMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            for(int i=0;i< position.size();i++){
                Log.d("init",position.toString());
//                    LatLng location = new LatLng(position.get(i).getLat(), position.get(i).getLon());
//                    MarkerOptions markerOptions = new MarkerOptions()
//                            .position(location)
//                            .title("Vị trí từ Cell Info: " + position.get(i).getAddress());
//
//                    // Set a custom marker icon with the GIF image URL
//                    //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromUrl(position.get(i).getGifImageUrl())));
//                    this.googleMap.addMarker(markerOptions);
//                    this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));

                this.googleMap.addMarker(new MarkerOptions().position(new LatLng(position.get(i).getLat(), position.get(i).getLon())).title(position.get(i).getAddress()));
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(position.get(i).getLat(), position.get(i).getLon()),16.0f));
            }
        }
    }
    private void showMap() {
        // Hide the list and show the map
        LinearLayout container = findViewById(R.id.linear_layout_container);
        container.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getView().setVisibility(View.VISIBLE);
        }
    }
    private Bitmap getMarkerBitmapFromUrl(String url) {
        try {
            // Load the GIF image from the URL
            URL imageUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            // Resize the bitmap if needed
            int width = 100; // Set your desired width
            int height = 100; // Set your desired height
            return Bitmap.createScaledBitmap(bitmap, width, height, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public CellTower bindData(CellInfo cellInfo){
        CellTower cellTower = new CellTower();
        if (cellInfo instanceof CellInfoWcdma) {
            //联通3G
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
            cellTower.getSignal().setType("WCDMA");
            cellTower.setCellId(cellIdentityWcdma.getCid());
            cellTower.setLocationAreaCode(cellIdentityWcdma.getLac());
            cellTower.setMobileCountryCode(cellIdentityWcdma.getMcc());
            cellTower.setMobileNetworkCode(cellIdentityWcdma.getMnc());
            cellTower.setSignalStrength(cellIdentityWcdma.getPsc());
        } else if (cellInfo instanceof CellInfoLte) {
            //4G
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            cellTower.getSignal().setType("LTE");
            cellTower.setCellId(cellIdentityLte.getCi());
            cellTower.setMobileNetworkCode(cellIdentityLte.getMnc());
            cellTower.setMobileCountryCode(cellIdentityLte.getMcc());
            cellTower.setLocationAreaCode(cellIdentityLte.getTac());
            cellTower.setPci(cellIdentityLte.getPci());
            if (cellInfoLte.getCellSignalStrength() != null) {
                cellTower.getSignal().setAsuLevel(cellInfoLte.getCellSignalStrength().getAsuLevel());
                cellTower.getSignal().setSignalLevel(cellInfoLte.getCellSignalStrength().getLevel());
                cellTower.getSignal().setDbm(cellInfoLte.getCellSignalStrength().getDbm());
            }
        } else if (cellInfo instanceof CellInfoGsm) {
            //2G
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
            cellTower.getSignal().setType("GSM");
            cellTower.setCellId(cellIdentityGsm.getCid());
            cellTower.setLocationAreaCode(cellIdentityGsm.getLac());
            cellTower.setMobileCountryCode(cellIdentityGsm.getMcc());
            cellTower.setMobileNetworkCode(cellIdentityGsm.getMnc());
            cellTower.setPci(cellIdentityGsm.getPsc());
            if (cellInfoGsm.getCellSignalStrength() != null) {
                cellTower.getSignal().setAsuLevel(cellInfoGsm.getCellSignalStrength().getAsuLevel());
                cellTower.getSignal().setSignalLevel(cellInfoGsm.getCellSignalStrength().getLevel());
                cellTower.getSignal().setDbm(cellInfoGsm.getCellSignalStrength().getDbm());
            }
        } else {
            //电信2/3G
            Log.d("CDMA", "CDMA CellInfo................................................");
        }
        return cellTower;
    }


}