package com.example.mohamedhashim.hashim;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ListFragment extends Fragment {
    private GridView lvMovies;
    private ImageAdapter Adapter;
    private OnMovieSelectedListener mCallback;
    private JSONObject[] objects;
    private ProgressDialog progressDialog;
    private int selected_id;

    public ListFragment() {
        setHasOptionsMenu(true);
        selected_id = 0;
    }
    public interface OnMovieSelectedListener {
        void onMovieSelected(String response);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnMovieSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save out data for later creation.
        String sortMethod = getSortMethod();
        outState.putInt("id", selected_id);
        outState.putString("sort", sortMethod);
    }
    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //retrieve item position and sortMethod.
        if (savedInstanceState != null && savedInstanceState.containsKey("id") && savedInstanceState.getString("sort").equals(getSortMethod()))
            selected_id = savedInstanceState.getInt("id");
        else
            selected_id = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        lvMovies = (GridView) v.findViewById(R.id.lvMovies);
        lvMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mCallback.onMovieSelected(objects[position].toString());
                selected_id = position;


            }
        });
        // Inflate the layout for this fragment
        return v;
    }
    @Override
    public void onStart() {
        super.onStart();

        //show progressBar
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please wait..");
        progressDialog.setMessage("Loading..");
        progressDialog.show();

        //get sortMethod from preferences
        String sortMethod = getSortMethod();
        //get jsonObjects once and for all :D, depending on the sortMethod
        if (sortMethod.equals(getString(R.string.pref_sort_fav)))
            updateMoviesFav();
        else
            updateMovies(sortMethod);
    }

    private String getSortMethod(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortMethod = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_pop));
        return sortMethod;
    }

    private void updateMoviesFav(){
        try {
            objects = getFavourites();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //putting poster urls and updating UI
        String[] urls = null;
        try {
            urls = getPosters();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateUI(urls);
        progressDialog.dismiss();
    }

    private void updateMovies(String sortMethod) {
        new AsyncMovie().execute(sortMethod);
    }
    private void updateUI(String[] urls){
        Adapter = new ImageAdapter(getActivity(),urls);
        lvMovies.setAdapter(Adapter);
        lvMovies.smoothScrollToPosition(selected_id);
    }

        private JSONObject[] getObjects(JSONObject mainJson) throws JSONException {
        String root = "results";
        JSONArray roots = mainJson.getJSONArray(root);
        JSONObject[] arr = new JSONObject[roots.length()];
        for (int i = 0; i < roots.length(); i++) {
            arr[i] = roots.getJSONObject(i);
        }
        return arr;
    }

    private JSONObject[] getFavourites() throws JSONException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> hs = sharedPreferences.getStringSet(getString(R.string.pref_sort_fav), new HashSet<String>());
        JSONObject[] arr = new JSONObject[hs.size()];
        int i = 0;
        for (String s : hs)
            arr[i++] = new JSONObject(s);
        return arr;
    }

    private String[] getPosters() throws JSONException {
        String poster = "poster_path";
        String preUrl = "http://image.tmdb.org/t/p/w185/";

        String[] urls = new String[objects.length];

        for (int i = 0; i < objects.length; i++) {
            urls[i] = preUrl + objects[i].getString(poster);
        }

        return urls;
    }

    public class AsyncMovie extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = AsyncMovie.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String sortMethod = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String response = null;

            try {
                final Uri.Builder builder = new Uri.Builder()
                        .scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", sortMethod + ".desc")
                        .appendQueryParameter("api_key", BuildConfig.MOVIE_API_KEY);

                URL url = new URL(builder.toString());

                // Create the request to moviedb API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line + "\n");

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                response = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            //if there's no connection, prompt the user to open one.

           // if (response.endsWith("videos")) {
             //   response = response.substring(0,response.length() - 6);
                try {
                    objects = getObjects(new JSONObject(response));
                    String[] urls = getPosters();
                    updateUI(urls);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();

        }
    }

    //shows dialog to prompt the user to open internet connection.
   /* private void showDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        System.exit(0);
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You have no internet connection service on, Do you want to go to your Settings and turn it on?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No & Exit", dialogClickListener).show();
    }

    //checks availability of any internet connection.
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }*/
}
