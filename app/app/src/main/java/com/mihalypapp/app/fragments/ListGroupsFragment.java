package com.mihalypapp.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.GroupCardListAdapter;
import com.mihalypapp.app.models.ItemGroupCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListGroupsFragment extends Fragment {

    private static final String TAG = "ListGroupsFragment";

    private ArrayList<ItemGroupCard> groupCardList;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_groups, container, false);

        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        groupCardList = new ArrayList<>();
        //groupCardList.add(new ItemGroupCard(R.drawable.ic_launcher_foreground, "Teacher One", "Middle", "2015"));
        adapter = new GroupCardListAdapter(groupCardList);
        recyclerView.setAdapter(adapter);

        fetchGroups();

        return view;
    }

    private void fetchGroups() {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {

                JSONObject params = new JSONObject();
                try {
                    params.put("offset", 0);
                    params.put("quantity", 15);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final JsonObjectRequest getGroupsRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/groups", params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getString("status").equals("success")) {
                                        Log.i(TAG, response.toString());
                                        JSONArray groups = response.getJSONArray("groups");

                                        for (int i = 0; i < groups.length(); i++) {
                                            JSONObject group = groups.getJSONObject(i);
                                            groupCardList.add(new ItemGroupCard(
                                                    R.drawable.ic_launcher_foreground,
                                                    group.getString("name"),
                                                    group.getString("type"),
                                                    group.getString("year")
                                            ));
                                        }
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, "getGroupRequest ERROR");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                    }
                });

                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                requestQueue.add(getGroupsRequest);
            }
        });
    }
}
