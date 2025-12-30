package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.adapter.MovieAdapter;
import com.aplikasi.bioskopku.model.Movie;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private DatabaseReference mDatabase;
    private RecyclerView rvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private TextView tvGreeting;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvGreeting = findViewById(R.id.tv_greeting);
        TextView tvDate = findViewById(R.id.tv_date);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvMovies = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        tvDate.setText(dateFormat.format(new Date()));

        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, movieList);
        rvMovies.setAdapter(movieAdapter);

        // PERBAIKAN: Pastikan URL database benar
        mDatabase = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();

        fetchUsername();
        fetchMovies();

        // --- NAVIGATION ---
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true; 
            }

            Intent intent = null;
            if (itemId == R.id.nav_ticket) {
                intent = new Intent(this, TicketActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void fetchUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && tvGreeting != null) {
            // Cek path users. Kadang user disimpan langsung di root users/{uid}
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Coba ambil username, kalau gak ada ambil name
                    String username = snapshot.child("username").getValue(String.class);
                    if (username == null) {
                         username = snapshot.child("name").getValue(String.class);
                    }
                    
                    if (username != null && !username.isEmpty()) {
                        tvGreeting.setText("Halo, " + username);
                    } else {
                        tvGreeting.setText("Halo, " + (user.getDisplayName() != null ? user.getDisplayName() : "Pengguna"));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Gagal ambil username: " + error.getMessage());
                    tvGreeting.setText("Halo, Pengguna");
                }
            });
        }
    }

    private void fetchMovies() {
        progressBar.setVisibility(View.VISIBLE);
        // PERBAIKAN: Gunakan addListenerForSingleValueEvent untuk debug awal, atau tetap addValueEventListener
        mDatabase.child("movies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movieList.clear();
                if (!snapshot.exists()) {
                    Log.d(TAG, "Snapshot movies kosong");
                    progressBar.setVisibility(View.GONE);
                    // Jangan tampilkan toast dulu, siapa tau loading lambat
                    return;
                }

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Movie movie = data.getValue(Movie.class);
                        if (movie != null) {
                            movie.setKey(data.getKey());
                            // Handle rating yang bisa berupa String atau Long/Double dari Firebase
                            Object ratingObj = data.child("rating").getValue();
                            movie.rating = ratingObj;
                            
                            movieList.add(movie);
                            Log.d(TAG, "Movie added: " + movie.getTitle());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing movie: " + e.getMessage());
                    }
                }
                movieAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Gagal memuat data film: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Firebase fetch failed: ", error.toException());
            }
        });
    }
}