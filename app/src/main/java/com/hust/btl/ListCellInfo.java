package com.hust.btl;

import android.content.Context;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hust.btl.entity.CellTower;
import com.hust.btl.entity.GetPositionResponse;
import com.hust.btl.entity.Position;
import com.hust.btl.service.ApiManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// ... other imports

public class ListCellInfo extends Fragment {

    private Handler handler;
    private Runnable getCellInfoRunnable;
    public static List<CellTower> cellTower;
    private ApiManager apiManager;
    private static CopyOnWriteArrayList<Position> position = new CopyOnWriteArrayList<>();
    public static List<Position> getPositionList() {
        return new ArrayList<>(position); // Return a copy of the list for thread safety
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiManager = new ApiManager(getContext()); // Use getContext() for the context

        handler = new Handler(Looper.getMainLooper());
        getCellInfoRunnable = new Runnable() {
            @Override
            public void run() {
                getCellInfoAsync(new CellInfoCallback(){
                    @Override
                    public void onCellInfoReceived(List<CellTower> receivedCellTower) {
                        cellTower = receivedCellTower;
                        getPositionFromCellInfo(cellTower,new GetPositionCallBack(){
                            @Override
                            public void onPositionReceived(List<Position> p) {
                                position.clear();
                                position.addAll(p); // Thread-safe update
                            }
                        });
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateCellTowerDataInView(cellTower);
                            }
                        });
                        handler.postDelayed(getCellInfoRunnable, 20000); // Schedule to run again after 5 seconds
                    }
                });
            }
        };

        // Start the periodic task immediately when activity starts
        handler.post(getCellInfoRunnable);
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null){
                            callback.onCellInfoReceived(cellTower);
                            Toast.makeText(getActivity(), "Get cell info successfully ", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }
    private void getPositionFromCellInfo(List<CellTower> cellTower, final GetPositionCallBack apiCallback){
        List<Position> positionList= new ArrayList<>();

        for(int i=0; i < cellTower.size(); i++){
            apiManager.makeApiCall(cellTower.get(i).getMobileNetworkCode(), cellTower.get(i).getMobileCountryCode(),
                    cellTower.get(i).getLocationAreaCode(), cellTower.get(i).getCellId(), "gsm", new ApiManager.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Gson gson = new Gson();
                            GetPositionResponse apiResponse = gson.fromJson(response.toString(), GetPositionResponse.class);

                            Position p = new Position();
                            p.setAddress(apiResponse.getAddress());
                            p.setLat(apiResponse.getLat());
                            p.setLon(apiResponse.getLon());
                            positionList.add(p);

                            // Check if all positions have been received before invoking the callback
                            if (positionList.size() == cellTower.size()) {
                                apiCallback.onPositionReceived(positionList);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Handle API error
                        }
                    });
        }
    }

    private List<CellTower> getCellInfo(){
        List<CellTower> tower = Collections.synchronizedList(new ArrayList<>());
        TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    private void updateCellTowerDataInView(List<CellTower> cellTowers) {
        View view = getView();
        if (view != null) {
            LinearLayout container = view.findViewById(R.id.linear_layout_container);
            if (container != null) {
                container.removeAllViews();
            }

        }
        LinearLayout container = view.findViewById(R.id.linear_layout_container);
        if(container!= null){
            container.removeAllViews();
        }
//      container.removeAllViews(); // Clear existing views

        for (int i=0;i< cellTowers.size();i++) {
            // Inflate the card view layout
            View cardView = getLayoutInflater().inflate(R.layout.card_view_layout, container, false);

            // Find the TextViews in the card layout
            TextView tvCellId = cardView.findViewById(R.id.tvCellId);
            TextView tvLocationAreaCode = cardView.findViewById(R.id.tvLocationAreaCode);
            TextView tvMobileCountryCode = cardView.findViewById(R.id.tvMobileCountryCode);
            TextView tvMobileNetworkCode = cardView.findViewById(R.id.tvMobileNetworkCode);
            TextView location = cardView.findViewById(R.id.location);

            // Set the text for each TextView using the data from the current tower in the loop
            tvCellId.setText(String.valueOf(cellTowers.get(i).getCellId()));
            tvMobileNetworkCode.setText(String.valueOf(cellTowers.get(i).getMobileNetworkCode()));
            tvMobileCountryCode.setText(String.valueOf(cellTowers.get(i).getMobileCountryCode()));
            tvLocationAreaCode.setText(String.valueOf(cellTowers.get(i).getLocationAreaCode()));
//            location.setText(String.valueOf(position.size() != 0 ? position.get(i).getAddress() : null));
            // Add the card view to the container
            container.addView(cardView);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(getCellInfoRunnable);
        }
    }

}
