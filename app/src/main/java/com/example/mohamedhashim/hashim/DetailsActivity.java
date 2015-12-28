package com.example.mohamedhashim.hashim;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
String pos;
    ReviewKey REV=new ReviewKey();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);


        Result r=new Result(null,null,null,null,null,null,null,null);
        r = (Result)getIntent().getSerializableExtra("data");

        new JSONTaskReview().execute("http://api.themoviedb.org/3/movie/" + r.getId() + "/reviews?api_key=5bf92cd209aa47161a39f6ab96f0e0fe");


//        Log.d("ID JSON",r.getId());
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
                Result r;
                r = (Result) getIntent().getSerializableExtra("data");
                new JSONTaskTrailer().execute("http://api.themoviedb.org/3/movie/" + r.getId() + "/videos?api_key=5bf92cd209aa47161a39f6ab96f0e0fe");
                Log.d("ID : ", "http://api.themoviedb.org/3/movie/" + r.getId() + "/videos?api_key=5bf92cd209aa47161a39f6ab96f0e0fe");
//                Intent intent=new Intent(getApplicationContext(),Trailer.class);
                // startActivity(i);
            }
        });

/*
       TextView votecount= (TextView) findViewById(R.id.votecount);
        votecount.setText(r.getVoteCount());
*/
        TextView rate= (TextView) findViewById(R.id.rate);
        rate.setText(r.getVoteAverage().toString());

        ImageView image= (ImageView) findViewById(R.id.IconImage);
       // Toast.makeText(this,r.getPosterPath(),Toast.LENGTH_SHORT).show();
        Picasso.with(this.getApplication()).load("http://image.tmdb.org/t/p/w185/"+r.getPosterPath()).into(image);

        RatingBar ratingBar= (RatingBar) findViewById(R.id.RatingBar);
        ratingBar.setRating(r.getVoteAverage().intValue() / 2);



  /*

        pos=intent.getExtras().getString("vote");
        final TextView movie_vote = (TextView) findViewById(R.id.votecount);
        movie_vote.setText(pos);

*/
    }


    public class JSONTaskTrailer extends AsyncTask<String, String, List<TrailerKey>> {

        @Override
        protected List<TrailerKey> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                //PARSING

                String finalJson = buffer.toString();
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("results");

                List<TrailerKey> movieModelList = new ArrayList<>();

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    TrailerKey mtrailermodel = new TrailerKey();
                    mtrailermodel.setKey(finalObject.getString("key"));
                    // adding  the final object in the list
                    movieModelList.add(mtrailermodel);
                }

                return movieModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<TrailerKey> trailerKeys) {
            super.onPostExecute(trailerKeys);
            Log.d("link", "https://www.youtube.com/watch?v=" + trailerKeys.get(0).getKey());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + trailerKeys.get(0).getKey())));
        }
    }
    public class JSONTaskReview extends AsyncTask<String, String, List<ReviewKey>> {

        @Override
        protected List<ReviewKey> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                //PARSING

                String finalJson = buffer.toString();
                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("results");

                List<ReviewKey> movieModelList = new ArrayList<>();

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    ReviewKey mtrailermodel = new ReviewKey();
                    mtrailermodel.setReview(finalObject.getString("content"));
                    // adding  the final object in the list
                    movieModelList.add(mtrailermodel);
                }

                return movieModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<ReviewKey> Reviewkeys) {
            super.onPostExecute(Reviewkeys);
            TextView Review = (TextView) findViewById(R.id.review);
            Review.setText("REVIEW :\n\n"+Reviewkeys.get(0).getReview());
            Log.d("review", Reviewkeys.get(0).getReview());
        }
    }
        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



}

