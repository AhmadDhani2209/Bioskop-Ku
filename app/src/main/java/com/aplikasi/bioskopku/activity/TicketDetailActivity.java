package com.aplikasi.bioskopku.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.Ticket;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.NumberFormat;
import java.util.Locale;

public class TicketDetailActivity extends AppCompatActivity {

    private ImageView ivPoster, ivQRCode;
    private TextView tvJudul, tvJadwal, tvKursi, tvHarga, tvTicketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        // Inisialisasi View
        ivPoster = findViewById(R.id.ivPosterDetail);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvJudul = findViewById(R.id.tvJudulDetail);
        tvJadwal = findViewById(R.id.tvJadwalDetail);
        tvKursi = findViewById(R.id.tvKursiDetail);
        tvHarga = findViewById(R.id.tvHargaDetail);
        tvTicketId = findViewById(R.id.tvTicketId);

        // Tombol Back di toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detail Tiket");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Ambil data tiket dari Intent
        Ticket ticket = (Ticket) getIntent().getSerializableExtra("ticket_data");
        // Ambil ID Tiket (Key Firebase) jika dikirim terpisah
        String ticketKey = getIntent().getStringExtra("ticket_key");
        
        // Jika ticket key tidak dikirim terpisah, coba ambil dari object ticket
        if (ticketKey == null && ticket != null) {
            ticketKey = ticket.getTicketId();
        }

        if (ticket != null) {
            tampilkanData(ticket);
            // Gunakan ticketKey untuk QR Code, jika null gunakan movieTitle sebagai fallback
            generateQRCode(ticketKey != null ? ticketKey : ticket.movieTitle);
            tvTicketId.setText("ID Tiket: " + (ticketKey != null ? ticketKey : "-"));
        } else {
            Toast.makeText(this, "Data tiket tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void tampilkanData(Ticket ticket) {
        tvJudul.setText(ticket.movieTitle);
        tvJadwal.setText("Jam Tayang: " + ticket.showTime);
        // Gabungkan list kursi menjadi string: "A1, A2, B3"
        if (ticket.seats != null) {
            tvKursi.setText("Kursi: " + TextUtils.join(", ", ticket.seats));
        } else {
             tvKursi.setText("Kursi: -");
        }
        
        // Format Rupiah
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        tvHarga.setText(formatRupiah.format(ticket.totalPrice));

        // Load gambar poster dengan penanganan gs://
        String posterUrl = ticket.moviePoster;
        if (posterUrl != null) {
            if (posterUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl);
                Glide.with(this)
                        .load(storageReference)
                        .placeholder(R.drawable.ic_image_broken) 
                        .into(ivPoster);
            } else {
                Glide.with(this)
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_image_broken)
                        .into(ivPoster);
            }
        }
    }

    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Generate QR Code Bitmap
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            ivQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
