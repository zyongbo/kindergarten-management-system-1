package com.mihalypapp.app.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.AddDocumentActivity;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.adapters.DocumentCardArrayAdapter;
import com.mihalypapp.app.models.Document;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ListDocumentsFragment extends Fragment implements DeleteDocumentDialog.DeleteDocumentListener {

    private static final String TAG = "ListDocumentsFragment";

    private static final int WRITE_REQUEST = 112;
    private static final int RC_ADD_DOCUMENT = 9850;

    private long downloadID;


    private ListView listView;
    private FloatingActionButton floatingActionButton;

    private ArrayAdapter<Document> adapter;
    private ArrayList<Document> documentList = new ArrayList<>();

    private String userRole;

    private BroadcastReceiver broadcastReceiver;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_documents, container, false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("News");

        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.list_view_documents);

        floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setEnabled(false);
        floatingActionButton.setVisibility(View.GONE);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddDocumentActivity.class);
                startActivityForResult(intent, RC_ADD_DOCUMENT);
            }
        });

        fetchDocuments();

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long broadcastDownloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if(broadcastDownloadID == downloadID) {
                    if(getDownloadStatus() == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Download not complete.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        getActivity().registerReceiver(broadcastReceiver, filter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
            Objects.requireNonNull(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    private void fetchDocuments() {
        documentList.clear();
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest fetchDocumentsRequest = new JsonObjectRequest(Request.Method.GET, MainActivity.URL + "documents", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                final JSONArray documents = response.getJSONArray("documents");
                                for (int i = 0; i < documents.length(); i++) {
                                    JSONObject document = documents.getJSONObject(i);
                                    documentList.add(new Document(
                                            document.getInt("documentId"),
                                            document.getString("documentDescription"),
                                            document.getString("documentName"),
                                            document.getString("documentRole"),
                                            document.getString("documentDate")
                                    ));
                                }
                                userRole = response.getString("userRole");
                                if (!userRole.equals("PRINCIPAL")) {
                                    adapter = new DocumentCardArrayAdapter(getContext(), documentList, 0);
                                } else {
                                    adapter = new DocumentCardArrayAdapter(getContext(), documentList, 1);
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            Log.i(TAG, "megtagadvba");
                                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);
                                        } else {
                                            floatingActionButton.setEnabled(true);
                                            floatingActionButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                ((DocumentCardArrayAdapter) adapter).setOnDownloadButtonClickListener(new DocumentCardArrayAdapter.OnDownloadButtonClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onItemClick(View itemView, int position) {
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                            if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);
                                            } else {
                                                beginDownload(documentList.get(position).getId(), documentList.get(position).getName());
                                            }
                                        }
                                    }
                                });

                                ((DocumentCardArrayAdapter) adapter).setOnDeleteButtonClickListener(new DocumentCardArrayAdapter.OnDeleteButtonClickListener() {
                                    @Override
                                    public void onItemClick(View itemView, int position) {
                                        openDeleteDialog(documentList.get(position).getId());
                                    }
                                });

                                listView.setAdapter(adapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getContext(),"Error fetchDocumentsRequest", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(fetchDocumentsRequest);
    }

    private void deleteDocument(int docId) {
        JSONObject params = new JSONObject();
        try {
            params.put("docId", docId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest deleteDocumentRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "deleteDocument", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Response: " + response.toString());
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(getContext(), "Document successfully deleted!", Toast.LENGTH_SHORT).show();
                                fetchDocuments();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getContext(),"Error deleteDocumentRequest", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(deleteDocumentRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void beginDownload(int fileId, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(MainActivity.URL + "document/" + Integer.valueOf(fileId).toString()))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                .setTitle(fileName)
                .setDescription("Downloading file...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userRole.equals("PRINCIPAL")) {
                        floatingActionButton.setEnabled(true);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
        }
    }

    private int getDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadID);

        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);

        if(cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);

            return status;
        }

        return DownloadManager.ERROR_UNKNOWN;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.documents_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_refresh);
        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                fetchDocuments();
                return false;
            }
        });
    }

    private void openDeleteDialog(int docId) {
        DeleteDocumentDialog dialog = new DeleteDocumentDialog(docId);
        dialog.show(ListDocumentsFragment.this.getChildFragmentManager(), "Delete document dialog");
    }

    @Override
    public void onDeleteYesClicked(int docId) {
        Toast.makeText(getContext(), Integer.valueOf(docId).toString(), Toast.LENGTH_SHORT).show();
        deleteDocument(docId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_DOCUMENT) {
            if (resultCode == Activity.RESULT_OK) {
                fetchDocuments();
            }
        }
    }
}
