package com.aplikasi.bioskopku.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.adapter.TicketAdapter;
import com.aplikasi.bioskopku.model.Ticket;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TicketAdapter ticketAdapter;
    private List<Ticket> historyList;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private DatabaseReference ticketsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Update referensi ke "tickets" (lowercase) jika ingin mengambil data
        ticketsRef = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference("tickets");

        rvHistory = findViewById(R.id.rv_history);
        emptyState = findViewById(R.id.empty_state);
        progressBar = findViewById(R.id.progress_bar_history);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        // Gunakan adapter dengan isHistoryMode = true
        ticketAdapter = new TicketAdapter(this, historyList, true);
        rvHistory.setAdapter(ticketAdapter);

        // Fetch Data
        fetchHistoryTickets();

        // Navigation
        bottomNav.setSelectedItemId(R.id.nav_history);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) {
                return true; 
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (itemId == R.id.nav_ticket) {
                intent = new Intent(this, TicketActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    private void fetchHistoryTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        // Logika: Ambil semua tiket user, lalu filter mana yang sudah lewat waktu
        
        ticketsRef.orderByChild("userId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                Ticket ticket = data.getValue(Ticket.class);
                                if (ticket != null) {
                                    // TODO: Tambahkan logika filter waktu di sini
                                    // Misal: if (isPast(ticket.getShowTime())) ...
                                    
                                    // Untuk sekarang, kita masukkan semua ke history agar tidak kosong
                                    historyList.add(ticket);
                                }
                            }
                        }
                        
                        progressBar.setVisibility(View.GONE);
                        if (historyList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            rvHistory.setVisibility(View.GONE);
                        } else {
                            Collections.reverse(historyList); // Yang terbaru di atas
                            emptyState.setVisibility(View.GONE);
                            rvHistory.setVisibility(View.VISIBLE);
                        }
                        ticketAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(HistoryActivity.this, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_history);
    }
}