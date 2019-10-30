package com.mihalypapp.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.mihalypapp.app.fragments.DeleteDocumentDialog;
import com.mihalypapp.app.fragments.ImportExportFragment;
import com.mihalypapp.app.fragments.ListChildrenFragment;
import com.mihalypapp.app.fragments.ListDocumentsFragment;
import com.mihalypapp.app.fragments.ListGroupsFragment;
import com.mihalypapp.app.fragments.ListMessagePartnersFragment;
import com.mihalypapp.app.fragments.ListNewsFragment;
import com.mihalypapp.app.fragments.ListUsersScreenSlideFragment;
import com.mihalypapp.app.R;
import com.mihalypapp.app.fragments.MyUserFragment;

import java.util.Locale;
import java.util.Objects;

public class PrincipalActivity extends DrawerActivity {

    private static final String TAG = "PrincipalActivity";

    private boolean hasRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        Intent intent = getIntent();
        if (intent.hasExtra("request")) {
            hasRequest = true;
            if (Objects.requireNonNull(intent.getStringExtra("request")).equals("groupId")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListGroupsFragment()).commit();
            }
        }
    }

    @Override
    public String GetTitle() {
        return getString(R.string.principal);
    }

    @Override
    public int GetLayout() {
        return R.layout.activity_principal;
    }

    @Override
    public Fragment GetFragment() {
        return new MyUserFragment();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_my_user:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyUserFragment()).commit();
                break;
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListMessagePartnersFragment()).commit();
                break;
            case R.id.nav_users:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListUsersScreenSlideFragment()).commit();
                break;
            case R.id.nav_children:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListChildrenFragment()).commit();
                break;
            case R.id.nav_groups:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListGroupsFragment()).commit();
                break;
            case R.id.nav_documents:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListDocumentsFragment()).commit();
                break;
            case R.id.nav_news:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ListNewsFragment()).commit();
                break;
            case R.id.nav_csv:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ImportExportFragment()).commit();
                break;
            case R.id.nav_logout:
                setResult(RESULT_LOGGED_OUT);
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(hasRequest) {
            finish();
        } else {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                moveTaskToBack(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("lang", lang);
        editor.apply();
    }

    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("lang", "");
        setLocale(language);
    }
}
