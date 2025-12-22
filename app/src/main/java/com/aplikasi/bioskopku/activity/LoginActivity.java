package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.aplikasi.bioskopku.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private SwitchCompat switchAdmin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReference;

    @Override
    protected void onStart() {
        super.onStart();
        // --- PERBAIKAN: LOGIKA AUTO-LOGIN YANG BENAR ---
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Jika sudah ada user, cek rolenya dari database lalu arahkan
            checkUserRoleAndRedirect(currentUser);
        }
        // Jika tidak ada user, biarkan pengguna melihat layar login.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference();

        // Konfigurasi Google Client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ambil dari string resource lebih aman
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Hubungkan semua view
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        switchAdmin = findViewById(R.id.switch_admin);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvDaftar = findViewById(R.id.tvDaftar);
        CardView btnGoogle = findViewById(R.id.btnGoogle);

        // Set listener
        tvDaftar.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        btnLogin.setOnClickListener(v -> loginManual());
        btnGoogle.setOnClickListener(v -> signInGoogle());
    }

    private void loginManual() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleLoginSuccess();
                    } else {
                        Toast.makeText(LoginActivity.this, "Gagal Login: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        launcherGoogle.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> launcherGoogle = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleLoginSuccess();
                    } else {
                        Toast.makeText(LoginActivity.this, "Auth Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleLoginSuccess() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        boolean isAdmin = switchAdmin.isChecked();
        String role = isAdmin ? "admin" : "user";

        // Update role di database
        databaseReference.child("users").child(user.getUid()).child("role").setValue(role)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndRedirect(user); // Panggil fungsi redirect yang sudah ada
                    } else {
                        Toast.makeText(LoginActivity.this, "Gagal update role, masuk sebagai user", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    // --- FUNGSI UNTUK AUTO-LOGIN & REDIRECT ---
    private void checkUserRoleAndRedirect(FirebaseUser user) {
        databaseReference.child("users").child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                Intent intent;
                if ("admin".equals(role)) {
                    intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                }
                Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Gagal verifikasi, masuk sebagai user.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}