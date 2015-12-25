package com.example.mohamedhashim.hashim;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.security.interfaces.RSAKey;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
String pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details);

         Result r=new Result(null,null,null,null,null,null,null);
        r = (Result)getIntent().getSerializableExtra("data");

        TextView overview= (TextView) findViewById(R.id.description);
        overview.setText(r.getOverview());

        TextView movie_year = (TextView) findViewById(R.id.year);
        movie_year.setText(r.getReleaseDate());

        TextView movie_name = (TextView) findViewById(R.id.MovieName);
        movie_name.setText(r.getTitle());
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
        ratingBar.setRating(r.getVoteAverage().intValue()/2);




  /*
        pos=intent.getExtras().getString("rate");
        final TextView movie_rate = (TextView) findViewById(R.id.rate);
        movie_rate.setText(pos);

        pos=intent.getExtras().getString("vote");
        final TextView movie_vote = (TextView) findViewById(R.id.votecount);
        movie_vote.setText(pos);

       pos=intent.getExtras().getString("image");
        final ImageView movie_image = (ImageView) findViewById(R.id.IconImage);
        movie_image.setImageIcon();

*/
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