package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private boolean isAdmin = false;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView tvUsername, tvEmail, tvBalance;
    private EditText etFullName, etAddress, etAge;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDatabase = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("480039047684-kebtv1bn370vllu4bsohpr7a2j66seca.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        tvUsername = findViewById(R.id.tv_username_profile);
        tvEmail = findViewById(R.id.tv_email_profile);
        tvBalance = findViewById(R.id.tv_balance_profile);
        etFullName = findViewById(R.id.et_fullname);
        etAddress = findViewById(R.id.et_address);
        etAge = findViewById(R.id.et_age);
        btnSave = findViewById(R.id.btn_save_profile);
        Button btnLogout = findViewById(R.id.btn_logout);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        fetchUserData();

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            });
        });

        // --- NAVIGATION ---
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                return true; 
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                if (isAdmin) {
                    intent = new Intent(this, AdminHomeActivity.class);
                } else {
                    intent = new Intent(this, HomeActivity.class);
                }
            } else if (itemId == R.id.nav_ticket) {
                intent = new Intent(this, TicketActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); 
            }
            return true;
        });
    }

    private void fetchUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userData = snapshot.getValue(User.class);
                    if (userData != null) {
                        tvUsername.setText(userData.username);
                        tvEmail.setText(userData.email);
                        
                        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        tvBalance.setText("Saldo: " + rupiah.format(userData.balance));

                        if (userData.fullName != null) etFullName.setText(userData.fullName);
                        if (userData.address != null) etAddress.setText(userData.address);
                        if (userData.age != null) etAge.setText(userData.age);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Gagal memuat profil", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String fullName = etFullName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String age = etAge.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("address", address);
        updates.put("age", age);

        mDatabase.child("users").child(user.getUid()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Gagal menyimpan profil", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }

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