package com.aplikasi.bioskopku.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminAddMovieActivity extends AppCompatActivity {

    private EditText etTitle, etGenre, etDescription, etPosterUrl, etRating, etPrice, etSchedule;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // PERBAIKAN: Validasi role admin di onCreate
        checkAdminRole();
        
        setContentView(R.layout.activity_admin_add_movie);

        databaseReference = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference("movies");

        etTitle = findViewById(R.id.et_title);
        etGenre = findViewById(R.id.et_genre);
        etDescription = findViewById(R.id.et_description);
        etPosterUrl = findViewById(R.id.et_poster_url);
        etRating = findViewById(R.id.et_rating);
        etPrice = findViewById(R.id.et_price);
        etSchedule = findViewById(R.id.et_schedule);
        Button btnSaveMovie = findViewById(R.id.btn_save_movie);

        btnSaveMovie.setOnClickListener(v -> saveMovie());
    }

    private void checkAdminRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference("users").child(user.getUid());
        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if (!"admin".equals(role)) {
                    Toast.makeText(AdminAddMovieActivity.this, "Akses ditolak. Anda bukan admin.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                finish();
            }
        });
    }

    private void saveMovie() {
        String title = etTitle.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String posterUrl = etPosterUrl.getText().toString().trim();
        String rating = etRating.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String scheduleStr = etSchedule.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(genre) || TextUtils.isEmpty(description) ||
                TextUtils.isEmpty(posterUrl) || TextUtils.isEmpty(rating) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(scheduleStr)) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Harga harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> scheduleList = new ArrayList<>(Arrays.asList(scheduleStr.split("\\s*,\\s*")));

        String movieId = databaseReference.push().getKey();

        Movie movie = new Movie();
        movie.title = title;
        movie.genre = genre;
        movie.description = description;
        movie.poster = posterUrl;
        movie.rating = rating;
        movie.price = price;
        movie.schedule = scheduleList;

        if (movieId != null) {
            databaseReference.child(movieId).setValue(movie).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminAddMovieActivity.this, "Film berhasil disimpan", Toast.LENGTH_SHORT).show();
                    finish(); 
                } else {
                    Toast.makeText(AdminAddMovieActivity.this, "Gagal menyimpan film", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
