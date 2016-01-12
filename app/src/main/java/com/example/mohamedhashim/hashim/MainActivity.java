package com.example.mohamedhashim.hashim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ListFragment.OnMovieSelectedListener {
    private boolean mTwoPane;
    private String DFTAG = DetailFragment.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTwoPane = findViewById(R.id.frame_details) != null;

        //initialize a preference holding the favourites if not yet created.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(getString(R.string.pref_sort_fav))) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            Set<String> hs = new HashSet<>();
            edit.putStringSet(getString(R.string.pref_sort_fav), hs);
            edit.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //launching the settings activity
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(String response) {
        //this function gets called when a user clicks on an item,
        if (mTwoPane){
            //if in two pane mode, contact the fragment directly and replace the right frame with it.
            DetailFragment fragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Intent.EXTRA_TEXT, response);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_details,fragment).commit();
        }else{
            //if in one pane mode, open the detailsActivity which will contact the fragment.
            Intent intent = new Intent(this, DetailsActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, response);
            startActivity(intent);
        }
    }
}