package com.mihalypapp.app.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.adapters.AutoCompleteGroupAdapter;
import com.mihalypapp.app.models.Group;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class ImportExportFragment extends Fragment {

    private static final String TAG = "ImportExportFragment";

    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST = 112;

    private long downloadID;

    private View view;

    private TextInputLayout textInputTable;
    private String tableInput;

    private ArrayList<Group> groupList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteGroups;
    private TextInputLayout textInputLayoutGroups;
    private AutoCompleteGroupAdapter autoCompleteGroupAdapter;
    private Group selectedGroup;
    private boolean isGroupSelected = false;

    private TextView textViewFileName;
    private Button buttonImport;
    private Button buttonExport;
    private Button buttonSelectFile;

    private ProgressDialog progressDialog;
    private Uri uri;
    private BroadcastReceiver broadcastReceiver;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_import_export, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Import, export CSV");

        textInputTable = view.findViewById(R.id.text_input_table);
        textInputLayoutGroups = view.findViewById(R.id.text_input_groups);
        textInputLayoutGroups.setEnabled(false);
        autoCompleteGroups = view.findViewById(R.id.auto_complete_groups);
        autoCompleteGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGroup = (Group) adapterView.getAdapter().getItem(i);
                isGroupSelected = true;
                textInputLayoutGroups.setError(null);
            }
        });
        autoCompleteGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isGroupSelected = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        textViewFileName = view.findViewById(R.id.text_view_file_name);
        buttonSelectFile = view.findViewById(R.id.button_select_file);
        buttonImport = view.findViewById(R.id.button_upload);
        buttonExport = view.findViewById(R.id.button_download);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);
            } else {
                buttonSelectFile.setEnabled(true);
                buttonExport.setEnabled(true);
            }
        }

        buttonSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        buttonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Map<String, String> params = new HashMap<>(2);
                if (textInputTable.getEditText().getText().toString().trim().equals("children")) {
                    if (!validateTable() | !validateGroup()) {
                        return;
                    }
                    params.put("groupId", Integer.valueOf(selectedGroup.getId()).toString());
                } else {
                    if (!validateTable()) {
                        return;
                    }
                }

                clearFields();
                params.put("tableName", tableInput);

                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Uploading");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                new Thread() {
                    public void run() {
                        String result = multipartRequest(MainActivity.URL + "importCsv", params, uri, "document", getMimeType(getContext(), uri));
                        try {
                            JSONObject res = new JSONObject(result);
                            String response = res.getString("status");

                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            if (response.equals("success")) {
                                bundle.putString("status", "success");
                                message.setData(bundle);
                            } else if (response.equals("failed")) {
                                bundle.putString("status", "failed");
                                bundle.putString("err", res.getString("err"));
                                message.setData(bundle);
                            }
                            progressDialog.dismiss();
                            handler.sendMessage(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        /*if(result.equals("OK")) {
                            progressDialog.dismiss();
                            handler.sendEmptyMessage(0);
                        } else if (){
                            progressDialog.dismiss();
                            handler2.sendEmptyMessage(0);
                        }*/
                        Log.i(TAG, result);
                    }
                }.start();
            }
        });

        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateTable()) {
                    return;
                }
                beginDownload(tableInput);
            }
        });

        String[] ROLES = new String[]{"users", "children"};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, ROLES);
        AutoCompleteTextView exposedDropdown = view.findViewById(R.id.exposed_dropdown_table);
        exposedDropdown.setAdapter(adapter);
        exposedDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getAdapter().getItem(i).toString();
                if (item.equals("children")) {
                    textInputLayoutGroups.setVisibility(View.VISIBLE);
                } else {
                    textInputLayoutGroups.setVisibility(View.INVISIBLE);
                }
            }
        });

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

        fetchGroups();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            uri = data.getData();
            textViewFileName.setText(getDisplayName(uri));
            buttonImport.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonSelectFile.setEnabled(true);
                    buttonExport.setEnabled(true);
                }
                break;
            }
        }
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

    private void beginDownload(String tableName) {
        Log.i(TAG, MainActivity.URL + "exportCsv/" + tableName);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(MainActivity.URL + "exportCsv/" + tableName))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                .setTitle(tableName + ".csv")
                .setDescription("Downloading file...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, tableName + ".csv");

        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);
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

    private void fetchGroups() {
        JsonObjectRequest fetchGroupsRequest = new JsonObjectRequest(Request.Method.GET, MainActivity.URL + "groups", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                groupList.clear();
                                Log.i(TAG, response.toString());
                                JSONArray groups = response.getJSONArray("groups");

                                for (int i = 0; i < groups.length(); i++) {
                                    JSONObject group = groups.getJSONObject(i);
                                    groupList.add(new Group(
                                            group.getInt("groupid"),
                                            group.getString("type"),
                                            group.getString("teacherName"),
                                            group.getString("year")
                                    ));
                                    autoCompleteGroupAdapter = new AutoCompleteGroupAdapter(getContext(), groupList);
                                    autoCompleteGroups.setAdapter(autoCompleteGroupAdapter);
                                }
                                textInputLayoutGroups.setEnabled(true);
                                //buttonAddChild.setEnabled(true);
                            } else {
                                Log.i(TAG, "Smthg wrong!");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(fetchGroupsRequest);
    }

    private boolean validateGroup() {
        if (isGroupSelected) {
            textInputLayoutGroups.setError(null);
            return true;
        } else {
            textInputLayoutGroups.setError("Please select a group!");
            return false;
        }
    }
    private boolean validateTable() {
        tableInput = textInputTable.getEditText().getText().toString().trim();

        if (tableInput.isEmpty()) {
            textInputTable.setError("Field can't be empty.");
            return false;
        } else {
            textInputTable.setError(null);
            return true;
        }
    }


    private void clearFields() {
        textInputLayoutGroups.getEditText().setText("");
        textInputTable.getEditText().setText("");
        view.clearFocus();
    }

    public String getDisplayName(Uri uri) {
        String displayName = "";
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {

                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);
            }
        } finally {
            cursor.close();
        }

        return displayName;
    }

    public String multipartRequest(String urlTo, Map<String, String> params, Uri fileUri, String fileField, String fileMimeType) {
        HttpURLConnection connection;
        DataOutputStream outputStream;
        InputStream inputStream;

        String twoHyphens = "--";
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {
            InputStream fileInputStream = getActivity().getContentResolver().openInputStream(fileUri);

            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + getDisplayName(uri) + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = params.get(key);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            if (200 != connection.getResponseCode()) {
                Log.i(TAG, "ASD");
                //throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
                //Toast.makeText(getActivity(), "Upload failed!!", Toast.LENGTH_SHORT).show();
                /*Message message = handler2.obtainMessage();
                message.sendToTarget()*/;
            }

            inputStream = connection.getInputStream();

            result = this.convertStreamToString(inputStream);

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            throw new RuntimeException();
        }

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            if(bundle.getString("status").equals("success")) {
                Toast.makeText(getActivity(), "File successfully uploaded!", Toast.LENGTH_SHORT).show();
            } else if (bundle.getString("status").equals("failed")) {
                String err = bundle.getString("err");
                if (err.equals("NOT_CSV")) {
                    Toast.makeText(getActivity(), "The file extension should be .CSV!", Toast.LENGTH_SHORT).show();
                } else if (err.equals("ER_DUP_ENTRY")) {
                    Toast.makeText(getActivity(), "ER_DUP_ENTRY!", Toast.LENGTH_SHORT).show();
                } else if (err.equals("ER_NO_REFERENCED_ROW_2")){
                    Toast.makeText(getActivity(), "Foreign key constraint fails!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Unknown error!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Something wrong!", Toast.LENGTH_SHORT).show();
            }
        }

    };
}
