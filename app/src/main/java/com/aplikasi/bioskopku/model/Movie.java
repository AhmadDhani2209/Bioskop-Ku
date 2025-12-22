package com.aplikasi.bioskopku.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    // Ubah jadi PUBLIC agar Firebase mudah membaca & menulis
    public String title;
    public String genre;
    public String description;
    public String poster;
    public String rating;
    public int price;

    // TAMBAHAN: Untuk menampung jadwal dari Firebase
    public List<String> schedule = new ArrayList<>();

    // Constructor Kosong (WAJIB ADA untuk Firebase)
    public Movie() {}

    // Getter (Opsional jika sudah public, tapi biarkan agar kodingan lama aman)
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getPoster() { return poster; }
    public String getRating() { return rating; }
    public int getPrice() { return price; }

    // Getter Jadwal (PENTING untuk MovieDetailActivity)
    public List<String> getSchedule() {
        return schedule;
    }
}