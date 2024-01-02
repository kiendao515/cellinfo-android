package com.hust.btl.service;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hust.btl.entity.Position;

import org.json.JSONObject;

public class ApiManager {
    private static final String BASE_URL = "https://api.findcellid.com/api/look_up";

    private final Context context;
    private final RequestQueue requestQueue;

    public ApiManager(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void makeApiCall(int mnc, int mcc, int lac, int cid, String radio, final ApiCallback callback) {
        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("mnc", String.valueOf(mnc))
                .appendQueryParameter("mcc", String.valueOf(mcc))
                .appendQueryParameter("lac",String.valueOf(lac))
                .appendQueryParameter("cid", String.valueOf(cid))
                .appendQueryParameter("radio", radio);

        String apiUrl = uriBuilder.build().toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.getMessage());
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    public interface ApiCallback {
        void onSuccess(JSONObject response);

        void onError(String errorMessage);
    }
}

