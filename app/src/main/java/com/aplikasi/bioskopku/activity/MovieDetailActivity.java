package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.Movie;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MovieDetailActivity extends AppCompatActivity {

    private String selectedTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView ivBackdrop = findViewById(R.id.iv_backdrop);
        ImageView ivPoster = findViewById(R.id.iv_poster_detail);
        CardView cardPoster = findViewById(R.id.card_poster);
        TextView tvTitle = findViewById(R.id.tv_title_detail);
        TextView tvGenre = findViewById(R.id.tv_genre_detail);
        TextView tvRating = findViewById(R.id.tv_rating_detail);
        TextView tvSynopsis = findViewById(R.id.tv_synopsis_content);
        ExtendedFloatingActionButton btnSelectSeat = findViewById(R.id.btn_select_seat);
        ChipGroup chipGroupTime = findViewById(R.id.chip_group_time);

        Movie movie = (Movie) getIntent().getSerializableExtra("movie");

        if (movie != null) {
            tvTitle.setText(movie.getTitle());
            tvGenre.setText(movie.getGenre());
            tvRating.setText(movie.getRating());
            tvSynopsis.setText(movie.getDescription());

            // PERBAIKAN: Gunakan logika yang sama dengan Adapter untuk load gambar
            String posterUrl = movie.getPoster();
            if (posterUrl != null) {
                if (posterUrl.startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl);
                    
                    Glide.with(this).load(storageReference).into(ivPoster);
                    
                    ivBackdrop.setColorFilter(Color.rgb(80, 80, 80), PorterDuff.Mode.MULTIPLY);
                    Glide.with(this).load(storageReference).into(ivBackdrop);
                } else {
                    Glide.with(this).load(posterUrl).into(ivPoster);
                    
                    ivBackdrop.setColorFilter(Color.rgb(80, 80, 80), PorterDuff.Mode.MULTIPLY);
                    Glide.with(this).load(posterUrl).into(ivBackdrop);
                }
            }

            cardPoster.setAlpha(0f);
            cardPoster.setScaleX(0.8f);
            cardPoster.setScaleY(0.8f);
            cardPoster.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(300).start();

            chipGroupTime.removeAllViews();
            List<String> schedules = movie.getSchedule();
            if (schedules != null && !schedules.isEmpty()) {
                for (String time : schedules) {
                    Chip chip = new Chip(this);
                    chip.setText(time);
                    chip.setCheckable(true);
                    chip.setOnClickListener(v -> selectedTime = chip.getText().toString());
                    chipGroupTime.addView(chip);
                }
            } else {
                findViewById(R.id.tv_schedule_label).setVisibility(View.GONE);
                findViewById(R.id.schedule_scroll).setVisibility(View.GONE);
            }

            NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            btnSelectSeat.setText("Beli Tiket - " + rupiah.format(movie.getPrice()));

            btnSelectSeat.setOnClickListener(v -> {

                if ((schedules != null && !schedules.isEmpty()) && selectedTime == null) {
                     Toast.makeText(this, "Pilih jadwal terlebih dahulu!", Toast.LENGTH_SHORT).show();
                     return;
                }
                
                Intent intent = new Intent(this, PilihKursiActivity.class);
                intent.putExtra("movie", movie);
                // Kirim jadwal terpilih jika ada
                if (selectedTime != null) {
                    intent.putExtra("selected_time", selectedTime);
                }
                startActivity(intent);
            });

        } else {
            Toast.makeText(this, "Gagal memuat detail film.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
