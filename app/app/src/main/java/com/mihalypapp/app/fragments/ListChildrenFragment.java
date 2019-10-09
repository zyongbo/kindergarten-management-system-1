package com.mihalypapp.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.AddChildActivity;
import com.mihalypapp.app.activities.ChildActivity;
import com.mihalypapp.app.adapters.ChildCardAdapter;
import com.mihalypapp.app.models.Child;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListChildrenFragment extends Fragment {

    private static final String TAG = "ListChildrenFragment";

    private static final int RC_ADD_CHILD = 10;

    private ArrayList<Child> childList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;

    private RecyclerView recyclerView;
    private ChildCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_children, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Children");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new ChildCardAdapter(childList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchChildren();
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
                    fetchChildren();
                }
            }
        });

        adapter.setOnItemClickListener(new ChildCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int childId = childList.get(position).getId();
                Intent intent = new Intent(getContext(), ChildActivity.class);
                intent.putExtra(ChildActivity.CHILD_ID, childId);
                startActivity(intent);
            }
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddChildActivity.class);
                startActivityForResult(intent, RC_ADD_CHILD);
            }
        });

        fetchChildren();
        return view;
    }

    private void fetchChildren() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getChildrenRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/children", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray children = response.getJSONArray("children");

                                removeProgressBar();

                                if (refreshing) {
                                    childList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < children.length(); i++) {
                                    JSONObject child = children.getJSONObject(i);
                                    childList.add(new Child(
                                            child.getInt("childId"),
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            child.getString("groupType"),
                                            child.getString("parentName"),
                                            child.getString("parentEmail")
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(childList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getChildrenRequest ERROR");
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
        requestQueue.add(getChildrenRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        childList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(childList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            childList.remove(childList.size() - 1);
            adapter.notifyItemRemoved(childList.size());
        }
    }
}
