package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.aplikasi.bioskopku.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase; // Import wajib untuk fitur cache

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- RAHASIA KECEPATAN: AKTIFKAN DISK PERSISTENCE ---
        // Kode ini memerintahkan Firebase untuk menyimpan data secara Offline di HP.
        // Hasilnya: Saat pindah ke Home, data film muncul INSTAN (tanpa loading).
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Kita pakai try-catch agar aplikasi tidak crash kalau
            // MainActivity dibuka dua kali (karena fitur ini cuma boleh dipanggil sekali).
        }

        // Delay selama 2 detik (2000 ms) sebelum pindah halaman
        new Handler().postDelayed(() -> {
            // Cek apakah User sudah login sebelumnya?
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                // JIKA SUDAH LOGIN -> Ke Home
                intent = new Intent(MainActivity.this, HomeActivity.class);
            } else {
                // JIKA BELUM LOGIN -> Ke Login/Register
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);

            // Tutup Splash Screen agar user gak bisa balik ke sini kalau tekan Back
            finish();
        }, 2000); // 2000 milidetik = 2 detik
    }
}