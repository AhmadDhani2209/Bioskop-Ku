package com.aplikasi.bioskopku.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.activity.MovieDetailActivity;
import com.aplikasi.bioskopku.model.Movie;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context context;
    private final List<Movie> movieList;
    private final NumberFormat rupiahFormat;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
        this.rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenre());
        holder.tvRating.setText("⭐ " + movie.getRating());
        holder.btnPrice.setText(rupiahFormat.format(movie.getPrice()));

        // PERBAIKAN: Menangani URL gambar dari Firebase Storage (gs://)
        String posterUrl = movie.getPoster();
        if (posterUrl != null) {
            if (posterUrl.startsWith("gs://")) {
                // Jika format gs://, gunakan FirebaseStorage reference
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl);
                Glide.with(context)
                        .load(storageReference)
                        .placeholder(R.drawable.ic_image_broken)
                        .into(holder.ivPoster);
            } else {
                // Jika format http/https biasa
                Glide.with(context)
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_image_broken)
                        .into(holder.ivPoster);
            }
        } else {
            holder.ivPoster.setImageResource(R.drawable.ic_image_broken);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie", movie);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvGenre, tvRating, btnPrice;
        ImageView ivPoster;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            tvRating = itemView.findViewById(R.id.tv_rating);
            btnPrice = itemView.findViewById(R.id.btn_price);
            ivPoster = itemView.findViewById(R.id.iv_poster);
        }
    }
}
