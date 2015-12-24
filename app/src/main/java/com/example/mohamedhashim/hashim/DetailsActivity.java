package com.example.mohamedhashim.hashim;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);
        // get the intent from which this activity is called.
        Intent intent = getIntent();


    }


    public class JSONTask extends AsyncTask<String, String, List<Result> > {

        @Override
        protected List<Result>  doInBackground(String... params) {
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
                JSONArray parentArray= parentObject.getJSONArray("results");
                List<Result> movieModelList=new ArrayList<>();

                for (int i = 0; i < parentArray.length();i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    Result moviemodel = new Result();
                    moviemodel.setTitle(finalObject.getString("title"));
                    moviemodel.setReleaseDate(finalObject.getString("release_date"));
                    moviemodel.setPopularity(finalObject.getDouble("popularity"));
                    moviemodel.setOverview(finalObject.getString("overview"));
                    moviemodel.setPosterPath(finalObject.getString("poster_path"));
                    moviemodel.setVoteAverage(finalObject.getDouble("vote_average"));
                    moviemodel.setVoteCount(finalObject.getInt("vote_count"));

                    // adding  the final object in the list
                    movieModelList.add(moviemodel);
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
        protected void onPostExecute(List<Result>  result) {
            //TODO need to set data to the list
            super.onPostExecute(result);
            final MovieAdapter adapter=new MovieAdapter(getApplicationContext(),R.layout.details,result);

        }
    }




    public class MovieAdapter extends ArrayAdapter {
        private List<Result> movieModelList;
        private int resource;
        private LayoutInflater inflater;

        private ImageView img;
        private TextView movie_name;
        private TextView movie_year;
        private TextView movie_rate;
        private TextView movie_vote_count;
        private ImageButton movie_favourite;
        private RatingBar movie_ratingbar;
        private Button movie_trailer;
        private TextView movie_description;
        private TextView movie_view;

        public MovieAdapter(Context context, int resource, List<Result> objects) {
            super(context, resource, objects);
            movieModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=inflater.inflate(resource,null);
            }



            img= (ImageView) findViewById(R.id.IconImage);
            movie_name = (TextView) findViewById(R.id.MovieName);
            movie_year=(TextView) findViewById(R.id.Year);
            movie_rate=(TextView) findViewById(R.id.rate);
            movie_vote_count=(TextView) findViewById(R.id.votecount);
            movie_favourite = (ImageButton) findViewById(R.id.favorite);
            movie_ratingbar= (RatingBar) findViewById(R.id.RatingBar);
            movie_trailer= (Button) findViewById(R.id.trailer);
            movie_description= (TextView) findViewById(R.id.description);
            movie_view= (TextView) findViewById(R.id.view);


            movie_name.setText(movieModelList.get(position).getTitle());
            movie_year.setText(movieModelList.get(position).getReleaseDate());
            movie_rate.setText(movieModelList.get(position).getPopularity().toString());
            movie_vote_count.setText(movieModelList.get(position).getVoteCount());
            // favourite Button
            movie_ratingbar.setRating((float) (movieModelList.get(position).getPopularity()/4));
            // Video Trailer Button
            movie_description.setText(movieModelList.get(position).getOverview());
            //views Text View

            Picasso.with(this.getContext()).load("http://image.tmdb.org/t/p/w185/" + movieModelList.get(position).getPosterPath()).into(img);

            return convertView;
        }
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.details, container, false);
            return rootView;
        }

    }
}