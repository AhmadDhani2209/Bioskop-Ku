package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // PENTING: Untuk tombol Google

import com.aplikasi.bioskopku.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnDaftar;
    private TextView tvLogin;

    // Variabel Tambahan untuk Google
    private CardView btnGoogle;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Sambungkan ID XML (Harus sesuai dengan activity_register.xml yang baru)
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDaftar = findViewById(R.id.btnDaftar);
        tvLogin = findViewById(R.id.tvLogin);
        btnGoogle = findViewById(R.id.btnGoogle); // ID tombol Google

        // 3. SETTING GOOGLE SIGN IN (Client ID Tipe 3 Kamu)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Kode Client ID "Web Server" (Tipe 3) dari JSON kamu:
                .requestIdToken("480039047684-kebtv1bn370vllu4bsohpr7a2j66seca.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 4. Logika Klik Tombol
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Klik Daftar Manual
        btnDaftar.setOnClickListener(v -> registerManual());

        // Klik Daftar Google
        btnGoogle.setOnClickListener(v -> signInGoogle());
    }

    // --- LOGIKA 1: DAFTAR MANUAL ---
    private void registerManual() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Isi data lengkap dulu bos!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        simpanDataKeDatabase(user, username); // Simpan ke DB
                    } else {
                        Toast.makeText(RegisterActivity.this, "Gagal: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- LOGIKA 2: MASUK DENGAN GOOGLE ---
    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        launcherGoogle.launch(signInIntent);
    }

    // Penangkap Hasil dari Halaman Login Google
    private final ActivityResultLauncher<Intent> launcherGoogle = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google sukses, sekarang lapor ke Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // Tukar Token Google dengan Akun Firebase
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Pakai nama asli dari Google
                        String namaGoogle = user.getDisplayName();
                        simpanDataKeDatabase(user, namaGoogle);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Auth Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- LOGIKA 3: SIMPAN DATA KE DATABASE (Dipakai keduanya) ---
    private void simpanDataKeDatabase(FirebaseUser user, String username) {
        String userId = user.getUid();
        String email = user.getEmail();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("balance", 0); // Saldo awal

        mDatabase.child("users").child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Berhasil Masuk!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        // Hapus history biar gak bisa back ke register
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}