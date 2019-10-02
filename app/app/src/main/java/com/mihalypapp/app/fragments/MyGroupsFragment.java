package com.mihalypapp.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.mihalypapp.app.activities.GroupActivity;
import com.mihalypapp.app.adapters.GroupCardArrayAdapter;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyGroupsFragment extends Fragment {

    private static final String TAG = "MyGroupsFragment";

    private ArrayList<Group> groupList = new ArrayList<>();
    GroupCardArrayAdapter adapter;
    ListView listView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My groups");

        View view = inflater.inflate(R.layout.fragment_my_groups, container, false);
        listView = view.findViewById(R.id.list_view_groups);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Group group = (Group) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(getContext(), GroupActivity.class);
                intent.putExtra(GroupActivity.GROUP_ID, group.getId());
                startActivity(intent);
            }
        });

        fetchMyGroups();
        return view;
    }

    private void fetchMyGroups() {
        JsonObjectRequest groupsRequest = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.157:3000/myGroups", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray groups = response.getJSONArray("groups");

                                for (int i = 0; i < groups.length(); i++) {
                                    JSONObject group = groups.getJSONObject(i);
                                    groupList.add(new Group(
                                            group.getInt("groupId"),
                                            group.getString("groupType"),
                                            group.getString("teacherName"),
                                            group.getString("groupYear"),
                                            R.drawable.ic_launcher_foreground

                                    ));
                                    adapter = new GroupCardArrayAdapter(getContext(), groupList);
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
        requestQueue.add(groupsRequest);
    }
}
