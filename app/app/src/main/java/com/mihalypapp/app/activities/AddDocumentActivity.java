package com.mihalypapp.app.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.mihalypapp.app.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class AddDocumentActivity extends AppCompatActivity {

    private static final String TAG = "AddDocumentActivity";

    private static final int READ_REQUEST_CODE = 42;

    private TextInputLayout textInputDescription;
    private TextInputLayout textInputRole;
    private TextView textViewFileName;
    private Button buttonUpload;
    private Button buttonSelectFile;

    private String descriptionInput;
    private String roleInput;

    private ProgressDialog progressDialog;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);

        textInputDescription = findViewById(R.id.text_input_description);
        textInputRole = findViewById(R.id.text_input_role);
        textViewFileName = findViewById(R.id.text_view_file_name);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add a new document");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] ROLES = new String[]{"Parent", "Teacher", "Principal", "All"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, ROLES);
        AutoCompleteTextView exposedDropdown = findViewById(R.id.exposed_dropdown_role);
        exposedDropdown.setAdapter(adapter);

        buttonSelectFile = findViewById(R.id.button_select_file);
        buttonUpload = findViewById(R.id.button_upload);

        buttonSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateDescription() | !validateRole() | !validateFile()) {
                    return;
                }
                clearFields();

                final Map<String, String> params = new HashMap<>(2);
                params.put("role", roleInput);
                params.put("description", descriptionInput);

                progressDialog = new ProgressDialog(AddDocumentActivity.this);
                progressDialog.setTitle("Uploading");
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                new Thread() {
                    public void run() {
                        String result = multipartRequest(MainActivity.URL + "addDocument", params, uri, "document", getMimeType(AddDocumentActivity.this, uri));

                        if(result.equals("OK")) {
                            progressDialog.dismiss();
                            handler.sendEmptyMessage(0);
                        }
                        Log.i(TAG, result);
                    }
                }.start();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Toast.makeText(AddDocumentActivity.this, "File successfully uploaded!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            textViewFileName.setText(getDisplayName(uri));
            buttonUpload.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private boolean validateDescription() {
        descriptionInput = textInputDescription.getEditText().getText().toString().trim();

        if (descriptionInput.isEmpty()) {
            textInputDescription.setError("Field can't be empty.");
            return false;
        } else {
            textInputDescription.setError(null);
            return true;
        }
    }

    private boolean validateRole() {
        roleInput = textInputRole.getEditText().getText().toString().trim();

        if (roleInput.isEmpty()) {
            textInputRole.setError("Field can't be empty.");
            return false;
        } else {
            textInputRole.setError(null);
            return true;
        }
    }

    private boolean validateFile() {
        if (textViewFileName.length() == 0) {
            Toast.makeText(this, "Please select a file!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void clearFields() {
        textInputDescription.getEditText().setText("");
        textInputRole.getEditText().setText("");
        textViewFileName.setText("");
        buttonUpload.setEnabled(false);
        getCurrentFocus().clearFocus();
    }

    public String getDisplayName(Uri uri) {
        String displayName = "";
        Cursor cursor = getContentResolver()
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
            InputStream fileInputStream = getContentResolver().openInputStream(fileUri);

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
                //throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
                //Toast.makeText(this, "Upload failed!!", Toast.LENGTH_SHORT).show();
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
}
