package com.cektrend.trashget.collector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cektrend.trashget.adapter.TruckListAdapter;
import com.cektrend.trashget.Interface.NameValue;
import com.cektrend.trashget.R;
import com.cektrend.trashget.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRequest extends AppCompatActivity implements Track, NameValue {
    List<TruckList> list = new ArrayList<>();
    RecyclerView recyclerView;
    Button bt;
    List list1;
    public static String latitude, longitude;
    TruckListAdapter adapter;
    ArrayList<String> arrayList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_request);
        recyclerView = findViewById(R.id.recycler);
        adapter = new TruckListAdapter(list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setTrack(GetRequest.this);
        adapter.setvalue(GetRequest.this);
        recyclerView.setAdapter(adapter);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://fundevelopers.website/TomTom/truck.php";
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()) {
                    try {
                        final JSONObject jsonObject = response.getJSONObject(count);
                        // data=new String[4];
                        String name = jsonObject.getString("name");
                        String mobile = jsonObject.getString("mobile_no");
                        String latitude = jsonObject.getString("latitude");
                        String longitude = jsonObject.getString("longitude");
                        if (!(latitude.equals("0") && longitude.equals("0"))) {
                            TruckList truckList = new TruckList("Name :" + name, "Mobile :" + mobile);
                            list.add(truckList);
                            adapter.notifyDataSetChanged();
                            Log.e("name", name);
                        }
                        if (list.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "No response found", Toast.LENGTH_SHORT).show();

                        }
//  Toast.makeText(GetRequest.this,name,Toast.LENGTH_LONG).show();
                        count++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void track(final String name) {
        //   Log.e("iiii",""+i);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.137.1/php/tracktruck.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String[] split = response.split(",");
                String latitude = split[0];
                String longitude = split[1];
                Intent intent = new Intent(GetRequest.this, TrackTruck.class);
                intent.putExtra("lat", latitude);
                intent.putExtra("lang", longitude);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    @Override
    public void notifyme(String username) {
        String[] split = username.split(":");
        String username1 = split[1];
        Log.e("userr", username1);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://fundevelopers.website/TomTom/statusupdate.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", username1);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}
