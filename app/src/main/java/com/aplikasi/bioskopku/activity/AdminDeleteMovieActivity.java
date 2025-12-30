package com.aplikasi.bioskopku.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.adapter.MovieAdapter;
import com.aplikasi.bioskopku.model.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminDeleteMovieActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView rvMoviesDelete;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_delete_movie);

        rvMoviesDelete = findViewById(R.id.rv_movies_delete);
        progressBar = findViewById(R.id.progress_bar_delete);

        rvMoviesDelete.setLayoutManager(new LinearLayoutManager(this));
        movieList = new ArrayList<>();

        movieAdapter = new MovieAdapter(this, movieList);
        rvMoviesDelete.setAdapter(movieAdapter);

        mDatabase = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();

        fetchMovies();
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

                            movie.setKey(data.getKey()); 
                            movieList.add(movie);
                        }
                    }
                }

                setupAdapter();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminDeleteMovieActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapter() {

        
        DeleteMovieAdapter deleteAdapter = new DeleteMovieAdapter(this, movieList, this::showDeleteDialog);
        rvMoviesDelete.setAdapter(deleteAdapter);
    }

    private void showDeleteDialog(Movie movie) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Film")
                .setMessage("Apakah Anda yakin ingin menghapus film '" + movie.getTitle() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> deleteMovie(movie))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteMovie(Movie movie) {
        if (movie.getKey() == null) {
            Toast.makeText(this, "Error: ID film tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.child("movies").child(movie.getKey()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminDeleteMovieActivity.this, "Film berhasil dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminDeleteMovieActivity.this, "Gagal menghapus film", Toast.LENGTH_SHORT).show());
    }
}
