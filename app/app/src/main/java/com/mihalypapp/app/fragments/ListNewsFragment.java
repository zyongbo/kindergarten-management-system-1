package com.mihalypapp.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.mihalypapp.app.activities.AddNewsActivity;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.adapters.NewsCardAdapter;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;
import com.mihalypapp.app.models.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListNewsFragment extends Fragment {

    private static final String TAG = "ListNewsFragment";

    private static final int RC_ADD_NEWS = 9849;

    private ArrayList<News> newsCardList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;

    private RecyclerView recyclerView;
    private NewsCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;
    FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_news, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("News");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new NewsCardAdapter(newsCardList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchNews();
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
                    fetchNews();
                }
            }
        });

        floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setVisibility(View.GONE);
        floatingActionButton.setEnabled(false);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddNewsActivity.class);
                startActivityForResult(intent, RC_ADD_NEWS);
            }
        });

        fetchNews();
        return view;
    }

    private void fetchNews() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getNewsRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "news", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());

                                if (response.getString("userRole").equals("PRINCIPAL")) {
                                    floatingActionButton.setEnabled(true);
                                    floatingActionButton.setVisibility(View.VISIBLE);
                                }

                                JSONArray allNews = response.getJSONArray("allNews");

                                removeProgressBar();

                                if (refreshing) {
                                    newsCardList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < allNews.length(); i++) {
                                    JSONObject news = allNews.getJSONObject(i);
                                    newsCardList.add(new News(
                                            news.getInt("newsId"),
                                            news.getString("newsTitle"),
                                            news.getString("newsContent"),
                                            news.getString("newsDate")
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(newsCardList.size() - i, i);
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
        requestQueue.add(getNewsRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        newsCardList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(newsCardList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            newsCardList.remove(newsCardList.size() - 1);
            adapter.notifyItemRemoved(newsCardList.size());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_NEWS) {
            if (resultCode == Activity.RESULT_OK) {
                refreshing = true;
                offset = 0;
                fetchNews();
            }
        }
    }
}
