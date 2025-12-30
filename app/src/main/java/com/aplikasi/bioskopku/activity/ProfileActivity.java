package com.aplikasi.bioskopku.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
        
        // PERBAIKAN: Klik Saldo untuk Top Up
        tvBalance.setOnClickListener(v -> showTopUpDialog());

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
            mDatabase.child("users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User userData = snapshot.getValue(User.class);
                    if (userData != null) {
                        tvUsername.setText(userData.username);
                        tvEmail.setText(userData.email);
                        
                        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        // Gunakan object karena balance bisa Long atau Double
                        Object balanceObj = snapshot.child("balance").getValue();
                        long balance = 0;
                        if (balanceObj instanceof Long) {
                            balance = (Long) balanceObj;
                        } else if (balanceObj instanceof Double) {
                            balance = ((Double) balanceObj).longValue();
                        }
                        
                        tvBalance.setText("Saldo: " + rupiah.format(balance));

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
    
    // PERBAIKAN: Dialog Top Up
    private void showTopUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_topup, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        EditText etNominal = dialogView.findViewById(R.id.et_nominal_topup);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_topup);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_topup);
        
        btnConfirm.setOnClickListener(v -> {
            String nominalStr = etNominal.getText().toString().trim();
            if (nominalStr.isEmpty()) {
                etNominal.setError("Masukkan nominal");
                return;
            }
            
            try {
                long nominal = Long.parseLong(nominalStr);
                if (nominal < 10000) {
                     etNominal.setError("Minimal top up Rp10.000");
                     return;
                }
                processTopUp(nominal);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                etNominal.setError("Nominal tidak valid");
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void processTopUp(long jumlahTopUp) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        DatabaseReference userRef = mDatabase.child("users").child(user.getUid());

        userRef.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentBalance = 0;
                Object balanceObj = snapshot.getValue();
                if (balanceObj instanceof Long) {
                    currentBalance = (Long) balanceObj;
                } else if (balanceObj instanceof Double) {
                    currentBalance = ((Double) balanceObj).longValue();
                }

                // Tambahkan saldo baru
                userRef.child("balance").setValue(currentBalance + jumlahTopUp)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Top Up Berhasil!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Gagal Top Up", Toast.LENGTH_SHORT).show();
                    });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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