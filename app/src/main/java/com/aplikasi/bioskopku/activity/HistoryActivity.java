package com.aplikasi.bioskopku.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements TicketAdapter.OnTicketCancelListener {

    private RecyclerView rvHistory;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private DatabaseReference ticketsRef;
    private Query ticketQuery;
    private ValueEventListener ticketListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ticketsRef = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference("Tickets");

        rvHistory = findViewById(R.id.rv_history);
        emptyState = findViewById(R.id.empty_state);
        progressBar = findViewById(R.id.progress_bar_history);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(this, ticketList);
        rvHistory.setAdapter(ticketAdapter);

        // --- PERBAIKAN NAVIGASI ---
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_ticket) {
                return true; // Sudah di halaman ini, tidak perlu lakukan apa-apa
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                // Hilangkan animasi berkedip agar transisi mulus
                overridePendingTransition(0, 0);
            }
            return true;
        });
        // -------------------------
    }

    // --- PERBAIKAN SINKRONISASI WARNA NAVBAR ---
    @Override
    protected void onResume() {
        super.onResume();
        // Setiap kali halaman ini muncul, paksa item 'Tiket' yang terpilih
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_ticket);
    }
    // ----------------------------------------

    private void fetchMyTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvHistory.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        String myUid = user.getUid();
        ticketQuery = ticketsRef.orderByChild("userId").equalTo(myUid);

        ticketListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Ticket ticket = data.getValue(Ticket.class);
                        if (ticket != null) {
                            ticket.ticketId = data.getKey();
                            ticketList.add(ticket);
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (ticketList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                } else {
                    Collections.reverse(ticketList);
                    emptyState.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                }
                ticketAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (!isFinishing()) {
                    Toast.makeText(HistoryActivity.this, "Gagal memuat tiket.", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchMyTickets();
        if (ticketQuery != null && ticketListener != null) {
            ticketQuery.addValueEventListener(ticketListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ticketQuery != null && ticketListener != null) {
            ticketQuery.removeEventListener(ticketListener);
        }
    }

    @Override
    public void onCancelTicket(String ticketId) {
        new AlertDialog.Builder(this)
                .setTitle("Batalkan Tiket")
                .setMessage("Apakah Anda yakin ingin membatalkan tiket ini?")
                .setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                    ticketsRef.child(ticketId).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(HistoryActivity.this, "Tiket berhasil dibatalkan", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(HistoryActivity.this, "Gagal membatalkan tiket", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}