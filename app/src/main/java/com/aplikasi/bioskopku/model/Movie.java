package com.aplikasi.bioskopku.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Movie implements Serializable {
    public String key; 
    public String title;
    public String genre;
    public String description;
    public String poster;
    
    // PERBAIKAN: Gunakan Object karena di Firebase bisa tersimpan sebagai Number (9.2) atau String ("9.2")
    public Object rating; 
    
    public int price;

    public List<String> schedule = new ArrayList<>();

    public Movie() {}

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getDescription() { return description; }
    public String getPoster() { return poster; }
    
    // PERBAIKAN: Konversi rating ke String dengan aman
    public String getRating() { 
        if (rating == null) return "N/A";
        return String.valueOf(rating); 
    }
    
    public int getPrice() { return price; }

    public List<String> getSchedule() {
        return schedule;
    }
}