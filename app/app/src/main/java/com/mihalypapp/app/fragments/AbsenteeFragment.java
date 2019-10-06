package com.mihalypapp.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.ChildActivity;
import com.mihalypapp.app.adapters.ChildCardArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

public class AbsenteeFragment extends Fragment {

    private static final String TAG = "AbsenteeFragment";

    ChildCardArrayAdapter childAdapter;
    ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_absentee, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Absentees");

        listView = view.findViewById(R.id.list_view_absentees);

        //fetchAbsentees();

        return view;
    }

    /*private void fetchAbsentees() {
        JSONObject params = new JSONObject();
        try {
            params.put("childId", childId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest childRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/child", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONObject child = response.getJSONArray("child").getJSONObject(0);
                                textViewChildName.setText(child.getString("childName"));
                                textViewParentName.setText(child.getString("parentName"));
                                textViewDateOfBirth.setText(child.getString("childBirth"));
                                textViewTeacherName.setText(child.getString("teacherName"));
                                textViewGroupType.setText(child.getString("groupType"));
                            } else {
                                Toast.makeText(ChildActivity.this,"Error", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(childRequest);
    }*/
}
