package com.mihalypapp.app.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mihalypapp.app.R;
import com.mihalypapp.app.adapters.GroupCardAdapter;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.GroupCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListGroupsFragment extends Fragment {

    private static final String TAG = "ListGroupsFragment";

    private ArrayList<GroupCard> groupCardList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;

    private GroupCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_groups, container, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new GroupCardAdapter(groupCardList);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchGroups();
                }
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        swipeContainer = view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    fetchGroups();
                }
            }
        });

        adapter.setOnItemClickListener(new GroupCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                String name = groupCardList.get(position).getYear();
                Toast.makeText(getContext(), name + " was clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        fetchGroups();
        return view;
    }

    private void fetchGroups() {
        fetching = true;

        JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
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

                                removeProgressBar();

                                if (refreshing) {
                                    groupCardList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < groups.length(); i++) {
                                    JSONObject group = groups.getJSONObject(i);
                                    groupCardList.add(new GroupCard(
                                            R.drawable.ic_launcher_foreground,
                                            group.getString("name"),
                                            group.getString("type"),
                                            group.getString("year")
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(groupCardList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getGroupRequest ERROR");
                            }

                            swipeContainer.setRefreshing(false);
                            fetching = false;
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

    private void addProgressBar(){
        showingProgressBar = true;
        groupCardList.add(null);
        adapter.notifyItemInserted(groupCardList.size() - 1);
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            groupCardList.remove(groupCardList.size() - 1);
            adapter.notifyItemRemoved(groupCardList.size());
        }
    }
}
