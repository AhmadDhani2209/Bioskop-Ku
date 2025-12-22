package com.aplikasi.bioskopku.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ticket implements Serializable {

    // PERBAIKAN: Menambahkan field untuk menyimpan ID unik tiket
    public String ticketId;

    public String userId;
    public String movieTitle;
    public String moviePoster;
    public long totalPrice;
    public long purchaseDate;
    public List<String> seats = new ArrayList<>();

    // Constructor kosong wajib untuk Firebase
    public Ticket() {}

    // --- GETTER YANG DIBUTUHKAN ADAPTER ---
    public String getTicketId() { return ticketId; }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public List<String> getSeats() {
        return seats;
    }
}
