package com.aplikasi.bioskopku.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.text.NumberFormat; 
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale; 
import java.util.Map;

public class PilihKursiActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private TextView tvTotalHarga;
    private final ArrayList<String> kursiTerpilih = new ArrayList<>();
    private DatabaseReference mDatabase;
    private Movie movie; 
    private String selectedTime; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_kursi);

        movie = (Movie) getIntent().getSerializableExtra("movie");
        selectedTime = getIntent().getStringExtra("selected_time"); 

        if (movie == null) {
            Toast.makeText(this, "Gagal memuat data film!", Toast.LENGTH_LONG).show();
            finish(); 
            return;
        }

        mDatabase = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();

        tvTotalHarga = findViewById(R.id.tvTotalPrice);
        Button btnBeliTiket = findViewById(R.id.btnBook);

        int[] seatIds = {R.id.seatA1, R.id.seatA2, R.id.seatA3, R.id.seatA4, R.id.seatA5, R.id.seatA6, R.id.seatB1, R.id.seatB2, R.id.seatB3, R.id.seatB4, R.id.seatB5, R.id.seatB6, R.id.seatC1, R.id.seatC2, R.id.seatC3, R.id.seatC4, R.id.seatC5, R.id.seatC6, R.id.seatD1, R.id.seatD2, R.id.seatD3, R.id.seatD4, R.id.seatD5, R.id.seatD6};
        for (int id : seatIds) {
            CheckBox checkBox = findViewById(id);
            checkBox.setOnCheckedChangeListener(this);
        }

        checkOccupiedSeats();

        btnBeliTiket.setOnClickListener(v -> {
            if (isTimePassed(selectedTime)) {
                Toast.makeText(this, "Maaf, jam tayang sudah lewat. Tidak bisa memesan tiket.", Toast.LENGTH_LONG).show();
                return;
            }

            if (kursiTerpilih.isEmpty()) {
                Toast.makeText(this, "Silakan pilih kursi terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else {
                showPaymentDialog();
            }
        });

        updateTotalHarga();
    }

    // Fungsi helper untuk mengecek apakah waktu sudah lewat
    private boolean isTimePassed(String timeString) {
        if (timeString == null || timeString.isEmpty()) return false;
        
        try {
            String[] parts = timeString.split(":");
            int showHour = Integer.parseInt(parts[0]);
            int showMinute = Integer.parseInt(parts[1]);

            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);

            if (currentHour > showHour) {
                return true;
            } else if (currentHour == showHour && currentMinute >= showMinute) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // PERBAIKAN 1: Optimasi Query (Filter by Movie Title)
    private void checkOccupiedSeats() {
        // Hanya ambil tiket yang judul filmnya sama dengan film yang sedang dibuka
        mDatabase.child("Tickets").orderByChild("movieTitle").equalTo(movie.getTitle())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> occupiedSeats = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    // Judul film sudah pasti sama karena difilter di query
                    String bookedTime = data.child("showTime").getValue(String.class);
                    
                    // Cek apakah jamnya sama
                    if (selectedTime != null && selectedTime.equals(bookedTime)) {
                        Object seatsObj = data.child("seats").getValue();
                        if (seatsObj instanceof List) {
                            List<String> seats = (List<String>) seatsObj;
                            if (seats != null) {
                                occupiedSeats.addAll(seats);
                            }
                        }
                    }
                }
                disableOccupiedSeats(occupiedSeats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void disableOccupiedSeats(ArrayList<String> occupiedSeats) {
        int[] seatIds = {R.id.seatA1, R.id.seatA2, R.id.seatA3, R.id.seatA4, R.id.seatA5, R.id.seatA6, 
                         R.id.seatB1, R.id.seatB2, R.id.seatB3, R.id.seatB4, R.id.seatB5, R.id.seatB6, 
                         R.id.seatC1, R.id.seatC2, R.id.seatC3, R.id.seatC4, R.id.seatC5, R.id.seatC6, 
                         R.id.seatD1, R.id.seatD2, R.id.seatD3, R.id.seatD4, R.id.seatD5, R.id.seatD6};

        for (int id : seatIds) {
            CheckBox checkBox = findViewById(id);
            if (checkBox == null) continue;
            
            String seatLabel = checkBox.getText().toString(); 
            
            if (occupiedSeats.contains(seatLabel)) {
                checkBox.setEnabled(false); // Tidak bisa diklik
                checkBox.setChecked(false); // Pastikan tidak tercentang
                checkBox.setButtonTintList(ColorStateList.valueOf(Color.GRAY)); // Ubah warna jadi abu-abu
            } else {
                // PERBAIKAN 3: Mengembalikan status kursi jika data berubah
                checkBox.setEnabled(true);
                checkBox.setButtonTintList(null); // Kembalikan warna default
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String seatNumber = buttonView.getText().toString();
        if (isChecked) {
            kursiTerpilih.add(seatNumber);
        } else {
            kursiTerpilih.remove(seatNumber);
        }
        updateTotalHarga();
    }

    private void updateTotalHarga() {
        int total = kursiTerpilih.size() * movie.getPrice();
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvTotalHarga.setText("Total: " + rupiahFormat.format(total));
    }

    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pembayaran, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        TextView tvMovieTitle = dialogView.findViewById(R.id.tv_movie_title);
        TextView tvSeats = dialogView.findViewById(R.id.tv_seats);
        TextView tvTotalPrice = dialogView.findViewById(R.id.tv_total_price);
        Button btnConfirm = dialogView.findViewById(R.id.btn_konfirmasi_bayar);
        Button btnCancel = dialogView.findViewById(R.id.btn_batal_bayar);

        int total = kursiTerpilih.size() * movie.getPrice();
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        tvMovieTitle.setText(movie.getTitle());
        
        StringBuilder seatsBuilder = new StringBuilder();
        for (String seat : kursiTerpilih) {
            seatsBuilder.append(seat).append(", ");
        }
        String seatsStr = seatsBuilder.length() > 0 ? seatsBuilder.substring(0, seatsBuilder.length() - 2) : "-";
        tvSeats.setText("Kursi: " + seatsStr);
        
        tvTotalPrice.setText("Total: " + rupiahFormat.format(total));

        btnConfirm.setOnClickListener(v -> {
            // PERBAIKAN 2: Panggil validasi sebelum proses beli
            validateAndPurchaseTicket(dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // PERBAIKAN 2: Validasi Akhir (Race Condition Check)
    private void validateAndPurchaseTicket(AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Cek sekali lagi ke database sebelum menyimpan
        mDatabase.child("Tickets").orderByChild("movieTitle").equalTo(movie.getTitle())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isConflict = false;
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    String bookedTime = data.child("showTime").getValue(String.class);
                    
                    if (selectedTime != null && selectedTime.equals(bookedTime)) {
                        Object seatsObj = data.child("seats").getValue();
                        if (seatsObj instanceof List) {
                            List<String> bookedSeats = (List<String>) seatsObj;
                            if (bookedSeats != null) {
                                // Cek apakah ada kursi pilihan user yang sudah terbooking
                                for (String mySeat : kursiTerpilih) {
                                    if (bookedSeats.contains(mySeat)) {
                                        isConflict = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (isConflict) break;
                }

                if (isConflict) {
                    dialog.dismiss();
                    Toast.makeText(PilihKursiActivity.this, "Maaf, salah satu kursi baru saja dipesan orang lain!", Toast.LENGTH_LONG).show();
                    // UI akan otomatis update karena ada listener checkOccupiedSeats
                } else {
                    processTicketPurchase(dialog);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PilihKursiActivity.this, "Gagal memverifikasi kursi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processTicketPurchase(AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Anda harus login untuk membeli tiket", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String orderId = mDatabase.child("Tickets").push().getKey();

        if (orderId == null) {
            Toast.makeText(this, "Gagal membuat pesanan, coba lagi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("userId", userId);
        ticketData.put("movieTitle", movie.getTitle()); 
        ticketData.put("moviePoster", movie.getPoster()); 
        ticketData.put("seats", kursiTerpilih);
        ticketData.put("totalPrice", kursiTerpilih.size() * movie.getPrice());
        ticketData.put("purchaseDate", System.currentTimeMillis());
        if (selectedTime != null) {
            ticketData.put("showTime", selectedTime);
        }

        mDatabase.child("Tickets").child(orderId).setValue(ticketData)
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    Toast.makeText(PilihKursiActivity.this, "Tiket berhasil dibeli!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(PilihKursiActivity.this, TicketActivity.class); 
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(PilihKursiActivity.this, "Gagal menyimpan pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
