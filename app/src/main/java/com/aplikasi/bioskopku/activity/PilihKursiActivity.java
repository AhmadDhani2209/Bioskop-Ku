package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.Movie; // Import model Movie
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat; // Import untuk format Rupiah
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale; // Import untuk format Rupiah
import java.util.Map;

// PERBAIKAN: Mengganti nama kelas agar sesuai standar Java
public class PilihKursiActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private TextView tvTotalHarga;
    private final ArrayList<String> kursiTerpilih = new ArrayList<>();
    private DatabaseReference mDatabase;
    private Movie movie; // Variabel untuk menyimpan data film

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_kursi);

        // PERBAIKAN: Mengambil data film dari Intent
        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            Toast.makeText(this, "Gagal memuat data film!", Toast.LENGTH_LONG).show();
            finish(); // Tutup activity jika data film tidak ada
            return;
        }

        mDatabase = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();

        tvTotalHarga = findViewById(R.id.tvTotalPrice);
        Button btnBeliTiket = findViewById(R.id.btnBook);

        // Set listener untuk semua checkbox kursi
        int[] seatIds = {R.id.seatA1, R.id.seatA2, R.id.seatA3, R.id.seatA4, R.id.seatA5, R.id.seatA6, R.id.seatB1, R.id.seatB2, R.id.seatB3, R.id.seatB4, R.id.seatB5, R.id.seatB6, R.id.seatC1, R.id.seatC2, R.id.seatC3, R.id.seatC4, R.id.seatC5, R.id.seatC6, R.id.seatD1, R.id.seatD2, R.id.seatD3, R.id.seatD4, R.id.seatD5, R.id.seatD6};
        for (int id : seatIds) {
            CheckBox checkBox = findViewById(id);
            checkBox.setOnCheckedChangeListener(this);
        }

        btnBeliTiket.setOnClickListener(v -> {
            if (kursiTerpilih.isEmpty()) {
                Toast.makeText(this, "Silakan pilih kursi terlebih dahulu", Toast.LENGTH_SHORT).show();
            } else {
                processTicketPurchase();
            }
        });

        updateTotalHarga();
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
        // PERBAIKAN: Menggunakan harga dari objek film
        int total = kursiTerpilih.size() * movie.getPrice();
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvTotalHarga.setText("Total: " + rupiahFormat.format(total));
    }

    private void processTicketPurchase() {
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

        // PERBAIKAN: Menggunakan data film yang sebenarnya
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("userId", userId);
        ticketData.put("movieTitle", movie.getTitle()); // Menggunakan judul asli
        ticketData.put("moviePoster", movie.getPoster()); // Menambahkan poster untuk riwayat
        ticketData.put("seats", kursiTerpilih);
        ticketData.put("totalPrice", kursiTerpilih.size() * movie.getPrice());
        ticketData.put("purchaseDate", System.currentTimeMillis());

        mDatabase.child("Tickets").child(orderId).setValue(ticketData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PilihKursiActivity.this, "Tiket berhasil dibeli!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PilihKursiActivity.this, HistoryActivity.class); // Arahkan ke Riwayat
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(PilihKursiActivity.this, "Gagal menyimpan pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
