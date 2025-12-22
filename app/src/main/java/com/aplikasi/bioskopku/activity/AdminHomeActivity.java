package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class AdminHomeActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView rvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private TextView tvGreeting;
    private ProgressBar progressBar;

    // PERBAIKAN: Tambahkan GoogleSignInClient untuk proses logout yang bersih
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // --- SETUP GOOGLE SIGN-IN CLIENT ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("480039047684-kebtv1bn370vllu4bsohpr7a2j66seca.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // ------------------------------------

        tvGreeting = findViewById(R.id.tv_greeting);
        TextView tvDate = findViewById(R.id.tv_date);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvMovies = findViewById(R.id.rv_movies);
        progressBar = findViewById(R.id.progress_bar);
        FloatingActionButton fabAddMovie = findViewById(R.id.fab_add_movie);

        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.admin_menu_bottom_nav);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        tvDate.setText(dateFormat.format(new Date()));

        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, movieList);
        rvMovies.setAdapter(movieAdapter);

        mDatabase = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();

        fetchMovies();
        fetchUsername();

        bottomNavigationView.setSelectedItemId(R.id.nav_admin_home);

        // --- PERBAIKAN: LOGIKA NAVIGASI YANG BERSIH ---
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_admin_home) {
                return true; // Tidak melakukan apa-apa karena sudah di halaman home
            }
            if (itemId == R.id.nav_admin_logout) {
                // Proses logout yang benar
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    Toast.makeText(AdminHomeActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
                return true;
            }
            return false;
        });
        // ---------------------------------------------

        fabAddMovie.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, AdminAddMovieActivity.class));
        });
    }

    private void fetchUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName() != null && !user.getDisplayName().isEmpty() ? user.getDisplayName() : "Admin";
            tvGreeting.setText("Halo, " + username);
        }
    }

    private void fetchMovies() {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.child("movies").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movieList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Movie movie = data.getValue(Movie.class);
                        if (movie != null) {
                            movieList.add(movie);
                        }
                    }
                }
                movieAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminHomeActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}