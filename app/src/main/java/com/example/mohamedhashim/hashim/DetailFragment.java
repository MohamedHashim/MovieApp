package com.example.mohamedhashim.hashim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class DetailFragment extends Fragment {
    private JSONObject jsonObject;
    private ImageView backdrop_path;
    private TextView overview;
    private TextView movie_year;
    private TextView movie_name;
    private LinearLayout btn;
    private TextView Review;
    private TextView rate;
    private ImageView image;
    private RatingBar ratingBar;
    private ImageView btnFavourite;
    static final String DETAIL_URI = "URI";
    private String movie_ID;
    private ListView  lv;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v2 = inflater.inflate(R.layout.fragment_detail, container, false);
        try {
            jsonObject = new JSONObject(getArguments().getString(Intent.EXTRA_TEXT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.e(LOG_TAG, jsonObject.toString());

        //get movie id
         movie_ID = getID();
         backdrop_path = (ImageView) v2.findViewById(R.id.backdrop_path);
         overview = (TextView) v2.findViewById(R.id.description);
         movie_year = (TextView) v2.findViewById(R.id.year);
         movie_name = (TextView) v2.findViewById(R.id.MovieName);
         btn = (LinearLayout) v2.findViewById(R.id.trailer);
         rate = (TextView) v2.findViewById(R.id.rate);
         image = (ImageView) v2.findViewById(R.id.IconImage);
         ratingBar = (RatingBar) v2.findViewById(R.id.RatingBar);
         btnFavourite = (ImageButton) v2.findViewById(R.id.btnfav);
         Review = (TextView) v2.findViewById(R.id.review);
        lv= (ListView) v2.findViewById(R.id.TrailerlistView);
        if (isFav())
            btnFavourite.setImageResource(R.drawable.redheart);


        return v2;
    }

    @Override
    public void onStart() {
        super.onStart();

        movie_name.setText(getTitle());

        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185/" + getBackdropPath()).into(backdrop_path);
        Log.d("backpath : ", "http://image.tmdb.org/t/p/w185/" + getBackdropPath());

        Picasso.with(this.getContext()).load("http://image.tmdb.org/t/p/w185/" + getPoster()).into(image);
        Log.d("image: ", "http://image.tmdb.org/t/p/w185/" + getPoster());

        overview.setText("DESCRIPTION :\n\n" + getOverView() + "\n\n\n");

        movie_year.setText(getDate());

        rate.setText(getVoteAverage()+" / 10");

        ratingBar.setRating(Float.parseFloat(getVote()) / 2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONTaskTrailer().execute("videos");
            }
        });

        new JSONTaskTrailer().execute("reviews");

        btnFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFav()) {
                    removeFromFav();
                    Toast.makeText(getActivity(), "Removed Successfully!", Toast.LENGTH_SHORT).show();
                    ImageButton remove = (ImageButton)v;
                    remove.setImageResource(R.drawable.ic_favorite_outline);
                } else {
                    addToFav();
                    Toast.makeText(getActivity(), "Added Successfully!", Toast.LENGTH_SHORT).show();
                    ImageButton add = (ImageButton)v;
                    add.setImageResource(R.drawable.redheart);

                }
                //automatically updates the favourites list on twoPane mode.
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm.findFragmentById(R.id.frame_list) != null && getSortMethod() == getString(R.string.pref_sort_fav)) {
                    ListFragment movieFragment = (ListFragment) fm.findFragmentById(R.id.frame_list);
                    movieFragment.onStart();
                }
            }
        });

    }

    private void ShowTrailers(final ArrayList<TrailerKey> trailers){
        final String[] items = new String[trailers.size()];
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < trailers.size(); i++) {
            items[i] = trailers.get(i).getName()+"               Quality - "+trailers.get(i).getSize()+"p";
            list.add(items[i]);
        }
        lv.setAdapter(new ArrayAdapter<String>(this.getContext(),R.layout.trailer_listview, list));
        if(list.size()==0){
            Toast toast = Toast.makeText(getActivity(), "This movie has NO Trailers !", Toast.LENGTH_SHORT);
            toast.show();
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Uri.Builder builder = new Uri.Builder()
                        .scheme("http")
                        .authority("youtube.com")
                        .appendPath("watch")
                        .appendQueryParameter("v", trailers.get(position).getKey());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(builder.toString())));
            }
        });



    }
    private ArrayList<TrailerKey> getmeURLS(JSONObject root) throws JSONException {
        String jsonResults = "results";
        String jsonName = "name";
        String jsonURL = "key";
        String jsonRES="size";
        ArrayList<TrailerKey> ans = new ArrayList<>();
        JSONArray arr = root.getJSONArray(jsonResults);
        for (int i = 0; i < arr.length(); i++) {
            String url,name,size;
            url = arr.getJSONObject(i).getString(jsonURL);
            name = arr.getJSONObject(i).getString(jsonName);
            size=arr.getJSONObject(i).getString(jsonRES);
            ans.add(new TrailerKey(url,name,size));
        }
        return ans;
    }
    public class JSONTaskTrailer extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = JSONTaskTrailer.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String response = null;

            try {
                final Uri.Builder builder = new Uri.Builder()
                        .scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movie_ID)
                        .appendPath(type)
                        .appendQueryParameter("api_key", BuildConfig.MOVIE_API_KEY);

                URL url = new URL(builder.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line + "\n");

                if (buffer.length() == 0) {
                    return null;
                }
                response = buffer.toString();
            } catch (IOException d) {
                Log.d(LOG_TAG, "Error ", d);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException d) {
                        Log.d(LOG_TAG, "Error", d);
                    }
                }
            }
            return response + type;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.endsWith("videos")) {
                response = response.substring(0,response.length() - 6);
                try {
                    ArrayList<TrailerKey> keys = getmeURLS(new JSONObject(response));
                    ShowTrailers(keys);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    response = response.substring(0, response.length() - 7);
                    Reviews(new JSONObject(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void Reviews(JSONObject response) throws JSONException {
        String jsonResults = "results";
        String jsonAuthor = "author";
        String jsonContent = "content";
        String text = "";
        JSONArray arr = response.getJSONArray(jsonResults);
        for (int i = 0; i < arr.length(); i++) {
            text += "REVIEWS : \n\nAuthor : " + arr.getJSONObject(i).getString(jsonAuthor) + ",\n\n";
            text += arr.getJSONObject(i).getString(jsonContent) + "\n";
        }
        if (text.equals(""))
            text = "There are no reviews for this film.";
        Review.setText(text);
    }

    private boolean isFav(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> hs = sharedPreferences.getStringSet(getString(R.string.pref_sort_fav), new HashSet<String>());
        return hs.contains(jsonObject.toString());
    }
    private void removeFromFav(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        Set<String> hs = sharedPreferences.getStringSet(getString(R.string.pref_sort_fav), new HashSet<String>());
        hs.remove(jsonObject.toString());
        edit.commit();
    }
    private void addToFav(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        Set<String> hs = sharedPreferences.getStringSet(getString(R.string.pref_sort_fav),new HashSet<String>());
        hs.add(jsonObject.toString());
        edit.commit();
    }
    private String getSortMethod(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortMethod = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_pop));
        return sortMethod;
    }
    private String getID(){
        String jsonID = "id";
        try {
            return jsonObject.getString(jsonID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getTitle(){
        String jsonTitle = "title";
        try {
            return jsonObject.getString(jsonTitle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getPoster(){
        String jsonPoster = "poster_path";
        String preUrl = "http://image.tmdb.org/t/p/w185/";
        try {
            return preUrl + jsonObject.getString(jsonPoster);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getBackdropPath(){
        String jsonPoster = "backdrop_path";
        String preUrl = "http://image.tmdb.org/t/p/w185/";
        try {
            return preUrl + jsonObject.getString(jsonPoster);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getOverView(){
        String jsonOverview = "overview";
        try {
            return jsonObject.getString(jsonOverview);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getVote(){
        String jsonVote = "vote_average";
        try {
            return jsonObject.getString(jsonVote);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getDate(){
        String jsonDate = "release_date";
        try {
            return jsonObject.getString(jsonDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getVoteAverage(){
        String jsonID = "vote_average";
        try {
            return jsonObject.getString(jsonID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
