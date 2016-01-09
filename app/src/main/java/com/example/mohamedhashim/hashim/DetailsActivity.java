package com.example.mohamedhashim.hashim;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DetailsActivity extends AppCompatActivity {
    String pos;
    private final String API_KEY = "5bf92cd209aa47161a39f6ab96f0e0fe";

    ReviewKey REV=new ReviewKey();
    Result r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);


        r = (Result)getIntent().getSerializableExtra("data");
        new AsyncReview().execute("reviews");

        // new JSONTaskReview().execute("http://api.themoviedb.org/3/movie/" + r.getId() + "/reviews?api_key=5bf92cd209aa47161a39f6ab96f0e0fe");

        ImageView backdrop_path= (ImageView) findViewById(R.id.backdrop_path);
        Picasso.with(this.getApplication()).load("http://image.tmdb.org/t/p/w185/"+r.getBackdropPath()).into(backdrop_path);
        Log.d("backpath : ","http://image.tmdb.org/t/p/w185/"+r.getBackdropPath());

        TextView overview= (TextView) findViewById(R.id.description);
        overview.setText("DESCRIPTION :\n\n"+r.getOverview()+"\n\n\n");

        TextView movie_year = (TextView) findViewById(R.id.year);
        movie_year.setText(r.getReleaseDate());

        TextView movie_name = (TextView) findViewById(R.id.MovieName);
        movie_name.setText(r.getTitle());


        LinearLayout btn= (LinearLayout) findViewById(R.id.trailer);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new JSONTaskTrailer().execute("videos");
                Log.d("TEST ID : ", "http://api.themoviedb.org/3/movie/" + r.getId() + "/videos?api_key=5bf92cd209aa47161a39f6ab96f0e0fe");
            }
        });

        TextView rate= (TextView) findViewById(R.id.rate);
        rate.setText(r.getVoteAverage().toString());

        ImageView image= (ImageView) findViewById(R.id.IconImage);
        // Toast.makeText(this,r.getPosterPath(),Toast.LENGTH_SHORT).show();
        Picasso.with(this.getApplication()).load("http://image.tmdb.org/t/p/w185/"+r.getPosterPath()).into(image);
        Log.d("image: ", "http://image.tmdb.org/t/p/w185/" + r.getPosterPath());

        RatingBar ratingBar= (RatingBar) findViewById(R.id.RatingBar);
        ratingBar.setRating((r.getVoteAverage().intValue()/2));

        ImageButton btnFavourite= (ImageButton) findViewById(R.id.btnfav);

        }



    private void ShowTrailers(final ArrayList<TrailerKey> trailers){
        final String[] items = new String[trailers.size()];
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < trailers.size(); i++) {
            items[i] = trailers.get(i).getName()+"               Quality - "+trailers.get(i).getSize()+"p";
            list.add(items[i]);
        }

        ListView lv= (ListView) findViewById(R.id.TrailerlistView);
        lv.setAdapter(new ArrayAdapter<String>(this,R.layout.trailer_listview, list));
        if(list.size()==0){
            Toast toast = Toast.makeText(getApplicationContext(), "This movie has NO Trailers !", Toast.LENGTH_SHORT);
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
                    .appendPath(r.getId())
                    .appendPath(type)
                    .appendQueryParameter("api_key", API_KEY);

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
    public class AsyncReview extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = AsyncReview.class.getSimpleName();

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
                        .appendPath(r.getId())
                        .appendPath(type)
                        .appendQueryParameter("api_key",API_KEY);

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
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error", e);
                    }
                }
            }
            return response + type;
        }
        @Override
        protected void onPostExecute(String response) {
            try {
                response = response.substring(0, response.length() - 7);
                Reviews(new JSONObject(response));
            } catch (JSONException e) {
                e.printStackTrace();
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
        TextView Review = (TextView) findViewById(R.id.review);
        Review.setText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}

