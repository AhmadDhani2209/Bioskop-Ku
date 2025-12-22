package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aplikasi.bioskopku.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvUsername = findViewById(R.id.tv_username_profile);
        TextView tvEmail = findViewById(R.id.tv_email_profile);
        Button btnLogout = findViewById(R.id.btn_logout);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvUsername.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Tambahkan logout Google jika perlu
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });

        // --- PERBAIKAN NAVIGASI ---
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                return true; // Sudah di halaman ini
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                if (isAdmin) {
                    intent = new Intent(this, AdminHomeActivity.class);
                } else {
                    intent = new Intent(this, HomeActivity.class);
                }
            } else if (itemId == R.id.nav_ticket) {
                intent = new Intent(this, HistoryActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); // Hilangkan animasi kedip
            }
            return true;
        });
        // -------------------------
    }

    // --- PERBAIKAN SINKRONISASI WARNA NAVBAR ---
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }
    // ----------------------------------------

    @Override
    public void onBackPressed() {
        Intent intent;
        if (isAdmin) {
            intent = new Intent(this, AdminHomeActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        super.onBackPressed();
    }
}
