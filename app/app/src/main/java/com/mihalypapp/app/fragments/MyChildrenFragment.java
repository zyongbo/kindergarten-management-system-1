package com.mihalypapp.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.adapters.ChildCardArrayAdapter;
import com.mihalypapp.app.models.Child;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpCookie;
import java.util.ArrayList;

public class MyChildrenFragment extends Fragment {

    private static final String TAG = "MyChildrenFragment";

    private ArrayList<Child> childList = new ArrayList<>();
    ChildCardArrayAdapter adapter;
    ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My children");

        View view = inflater.inflate(R.layout.fragment_my_children, container, false);
        listView = view.findViewById(R.id.list_view_children);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Child child = (Child) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(getContext(), ChildActivity.class);
                intent.putExtra(ChildActivity.CHILD_ID, child.getId());
                startActivity(intent);
            }
        });

        fetchMyChildren();
        return view;
    }

    private void fetchMyChildren() {
        JsonObjectRequest childrenRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.157:3000/myChildren", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray children = response.getJSONArray("children");

                                for (int i = 0; i < children.length(); i++) {
                                    JSONObject child = children.getJSONObject(i);
                                    childList.add(new Child(
                                            child.getInt("childId"),
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            child.getString("groupType")
                                    ));
                                    adapter = new ChildCardArrayAdapter(getContext(), childList, 0);
                                    listView.setAdapter(adapter);
                                }
                            } else {
                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
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

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(childrenRequest);
    }
}
