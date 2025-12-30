package com.aplikasi.bioskopku.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Ticket implements Serializable {

    public String ticketId;
    public String userId;
    public String movieTitle;
    public String moviePoster;
    public long totalPrice;
    public long purchaseDate;
    public String showTime;

    public List<String> seats = new ArrayList<>();

    public Ticket() {}

    // --- GETTER & SETTER ---
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getMovieTitle() { return movieTitle; }
    public String getMoviePoster() { return moviePoster; }
    public long getTotalPrice() { return totalPrice; }
    public String getShowTime() { return showTime; }
    public List<String> getSeats() { return seats; }

    /**
     * PERBAIKAN: Menghitung timestamp tayang sebenarnya.
     * Menggabungkan tanggal dari `purchaseDate` dengan jam dari `showTime`.
     * @return long timestamp
     */
    @Exclude // Agar tidak disimpan ke Firebase
    public long getShowTimestamp() {
        if (showTime == null || showTime.isEmpty()) {
            return purchaseDate; // Fallback jika tidak ada jam tayang
        }

        try {
            String[] timeParts = showTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(purchaseDate);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTimeInMillis();
        } catch (Exception e) {
            return purchaseDate; // Fallback jika format jam salah
        }
    }
}