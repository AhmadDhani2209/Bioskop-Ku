package com.aplikasi.bioskopku.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    public String key; // Key unik dari Firebase
    public String title;
    public String genre;
    public String description;
    public String poster;
    
    // PERBAIKAN: Gunakan Object agar bisa menerima String ("9.2") atau Number (9.2) dari Firebase
    public Object rating; 
    
    public int price;

    public List<String> schedule = new ArrayList<>();

    // Constructor Kosong (WAJIB ADA untuk Firebase)
    public Movie() {}

    // Getter dan Setter untuk key
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    // Getter
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getPoster() { return poster; }
    
    // PERBAIKAN: Getter Rating yang aman, mengonversi Object ke String
    public String getRating() { 
        if (rating == null) return "N/A";
        return String.valueOf(rating); 
    }
    
    public int getPrice() { return price; }

    // Getter Jadwal
    public List<String> getSchedule() {
        return schedule;
    }
}