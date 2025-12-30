package com.aplikasi.bioskopku.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.Movie;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DeleteMovieAdapter extends RecyclerView.Adapter<DeleteMovieAdapter.MovieViewHolder> {

    private final Context context;
    private final List<Movie> movieList;
    private final OnMovieClickListener onMovieClickListener;
    private final NumberFormat rupiahFormat;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public DeleteMovieAdapter(Context context, List<Movie> movieList, OnMovieClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.onMovieClickListener = listener;
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

        Glide.with(context)
                .load(movie.getPoster())
                .placeholder(R.drawable.ic_image_broken)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> {
            if (onMovieClickListener != null) {
                onMovieClickListener.onMovieClick(movie);
            }
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
