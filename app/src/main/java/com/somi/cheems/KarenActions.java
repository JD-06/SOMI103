package com.somi.cheems;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KarenActions {

    private RequestQueue mRequestQueue;
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void actions(String action, DetectIntentResponse response, Context context) {
        this.context = context;
        TTS.init(context);
        switch (action) {
            case "DuckDuckGoInstantAnswer":
                getdata(response.getQueryResult().getParameters().getFieldsOrThrow("any").getStringValue());
                break;
            case "Ubicacion":
                Ubicacion();
                break;
            case "Identificador":
                intentGlobal(context, IdentificadorImagenes.class);
                break;
            case "hora":
                TTS.speak(hora());
                break;
            case "":
                break;
            default:
                String botReply = response.getQueryResult().getFulfillmentText();
                TTS.speak(botReply);
        }
    }

    private void getdata(String word) {

        Response.ErrorListener response_error_listener = error -> {
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                //TODO
            } else if (error instanceof AuthFailureError) {
                //TODO
            } else if (error instanceof ServerError) {
                //TODO
            } else if (error instanceof NetworkError) {
                //TODO
            } else if (error instanceof ParseError) {
                //TODO
            }
        };


        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://api.duckduckgo.com/?q=" + word.replace(" ","+") + "&format=json&pretty=1&no_html=1&skip_disambig=1",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getString("AbstractText").equals("")) {
                                TTS.speak(jsonObject.getString("AbstractText"));
                            } else {
                                // JSONArray jsonArray = new JSONArray(response);
                                JSONArray myJsonArray = jsonObject.getJSONArray("RelatedTopics");
                                TTS.speak(myJsonArray.getJSONObject(0).getString("Text"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR","error => "+error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept-Language", context.getString(R.string.str_duckduckidioma));
                return params;
            }
        };
        getRequestQueue().add(stringRequest);

    }

    private RequestQueue getRequestQueue() {
        //requestQueue is used to stack your request and handles your cache.
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    public void Ubicacion() {
        // Get the location manager

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location != null){
            setLocation(location);
        }else{
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                setLocation(location);
            }else Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLocation (Location loc){
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    String direccion = (DirCalle.getAddressLine(0));
                    TTS.speak(context.getString(R.string.str_direccion)+direccion);
                    Toast.makeText(context, context.getString(R.string.str_direccion)+direccion, Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void intentGlobal(Context contextint, Class aClass){
        Intent intent = new Intent(contextint, aClass);
        contextint.startActivity(intent);
    }

    public String hora(){
        Calendar calendario = Calendar.getInstance();
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int min = calendario.get(Calendar.MINUTE);
        return context.getString(R.string.str_hora)+ hora+":"+min;
    }
}
