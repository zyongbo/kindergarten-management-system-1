package com.mihalypapp.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.mihalypapp.app.models.ItemUserCard;
import com.mihalypapp.app.adapters.UserCardListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class ListUsersScreenSlidePageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "LUSSPFragment";

    private ArrayList<ItemUserCard> userCardList;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int offset = 0;
    private int quantity = 15;

    private boolean onRefresh = false;
    private boolean currentlyLoading = false;
    private boolean everythingLoaded = false;

    private int firstVisibleItemPos;
    private int visibleItemCount;
    private int totalItemCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_users_screen_slide_page, container, false);

        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark);

        userCardList = new ArrayList<>();
        adapter = new UserCardListAdapter(userCardList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                fetchUsers();
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //Log.i(TAG, "visibleItemCount: " + Integer.valueOf(recyclerView.getChildCount()) + ", totalItemCount: " + Integer.valueOf(linearLayoutManager.getItemCount()) + ", firstVisibleItemPos: " + Integer.valueOf(linearLayoutManager.findFirstVisibleItemPosition()) + ", totalItemCount: " + totalItemCount);
                        //Log.i(TAG, String.valueOf(Boolean.valueOf(loading)));
                        if (dy > 0 && !everythingLoaded && !currentlyLoading) {
                            firstVisibleItemPos = linearLayoutManager.findFirstVisibleItemPosition();
                            visibleItemCount = recyclerView.getChildCount();
                            totalItemCount = linearLayoutManager.getItemCount();

                            if (!currentlyLoading && (totalItemCount - visibleItemCount) <= firstVisibleItemPos) {
                                offset += quantity;
                                onRefresh = false;
                                fetchUsers();
                            }
                        }
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onRefresh() {
        everythingLoaded = false;
        offset = 0;
        userCardList.clear();
        adapter.notifyDataSetChanged();
        onRefresh = true;
        recyclerView.setNestedScrollingEnabled(false);
        fetchUsers();
    }

    private void fetchUsers() {
        currentlyLoading = true;
        recyclerView.post(new Runnable() {
            @Override
            public void run() {

                if (!onRefresh) {
                    userCardList.add(null);
                    adapter.notifyDataSetChanged();
                }

                JSONObject params = new JSONObject();
                try {
                    params.put("role", getRole());
                    params.put("offset", offset);
                    params.put("quantity", quantity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final JsonObjectRequest getUsersRequest = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.157:3000/users", params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getString("status").equals("success")) {
                                        Log.i(TAG, response.toString());
                                        JSONArray users = response.getJSONArray("users");

                                        if (users.length() < quantity)
                                            everythingLoaded = true;

                                        if (!onRefresh)
                                            userCardList.remove(userCardList.size() - 1);

                                        for (int i = 0; i < users.length(); i++) {
                                            JSONObject user = users.getJSONObject(i);
                                            userCardList.add(new ItemUserCard(
                                                    R.drawable.ic_launcher_foreground,
                                                    user.getString("name"),
                                                    user.getString("email")
                                            ));
                                        }
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.e(TAG, "getUserRequest ERROR");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                currentlyLoading = false;
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        currentlyLoading = false;
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                requestQueue.add(getUsersRequest);
            }
        });

    }

    public abstract String getRole();
}
