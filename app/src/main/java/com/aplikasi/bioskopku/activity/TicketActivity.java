package com.aplikasi.bioskopku.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.adapter.TicketAdapter;
import com.aplikasi.bioskopku.model.Ticket;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TicketActivity extends AppCompatActivity implements TicketAdapter.OnTicketActionListener {

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
        setContentView(R.layout.activity_ticket);

        ticketsRef = FirebaseDatabase.getInstance("https://bioskop-ku-default-rtdb.firebaseio.com/").getReference("tickets");

        rvHistory = findViewById(R.id.rv_history);
        emptyState = findViewById(R.id.empty_state);
        progressBar = findViewById(R.id.progress_bar_history);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        // Mode Tiket Aktif (isHistoryMode = false)
        ticketAdapter = new TicketAdapter(this, ticketList, false);
        rvHistory.setAdapter(ticketAdapter);

        // --- NAVIGATION ---
        bottomNav.setSelectedItemId(R.id.nav_ticket);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_ticket) return true;

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (itemId == R.id.nav_history) {
                intent = new Intent(this, HistoryActivity.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_ticket);
    }

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
                long currentTime = System.currentTimeMillis();

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Ticket ticket = data.getValue(Ticket.class);
                        if (ticket != null) {
                            ticket.setTicketId(data.getKey());
                            
                            // FILTER: Tampilkan hanya jika jadwal BELUM lewat
                            // Jika jadwal kosong, anggap active (atau sesuaikan kebutuhan)
                            if (ticket.getShowTimestamp() > currentTime) {
                                ticketList.add(ticket);
                            }
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (ticketList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                } else {
                    Collections.reverse(ticketList); // Paling baru di atas
                    emptyState.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                }
                ticketAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (!isFinishing()) {
                    Toast.makeText(TicketActivity.this, "Gagal memuat tiket.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        ticketQuery.addValueEventListener(ticketListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ticketListener == null) {
            fetchMyTickets();
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
    public void onTicketClick(Ticket ticket) {
        showTicketDetailDialog(ticket);
    }

    @Override
    public void onCancelTicket(String ticketId) {
        new AlertDialog.Builder(this)
                .setTitle("Batalkan Tiket")
                .setMessage("Apakah Anda yakin ingin membatalkan tiket ini?")
                .setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                    ticketsRef.child(ticketId).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(TicketActivity.this, "Tiket berhasil dibatalkan", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(TicketActivity.this, "Gagal membatalkan tiket", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    @Override
    public void onDeleteHistory(String ticketId) {
        // Tidak dipakai di TicketActivity (Active)
    }

    private void showTicketDetailDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_pembayaran, null); // Gunakan layout yang ada atau buat baru dialog_ticket_detail
        // Kita modifikasi tampilan dialog_pembayaran sedikit secara programatik atau buat layout baru.
        // Agar cepat, saya buat layout sederhana on-the-fly atau reuse dialog_pembayaran tapi ganti teks.
        
        // Sebaiknya reuse dialog_pembayaran tapi sesuaikan isinya
        TextView tvTitle = view.findViewById(R.id.tv_movie_title);
        TextView tvSeats = view.findViewById(R.id.tv_seats);
        TextView tvPrice = view.findViewById(R.id.tv_total_price);
        TextView tvHeader = ((TextView) ((LinearLayout) view).getChildAt(0)); // "Konfirmasi Pembayaran"
        
        ImageView ivQr = ((ImageView) ((LinearLayout) view).getChildAt(4)); // QR Code
        TextView tvScan = ((TextView) ((LinearLayout) view).getChildAt(5)); // "Scan QRIS..."
        
        view.findViewById(R.id.btn_konfirmasi_bayar).setVisibility(View.GONE);
        view.findViewById(R.id.btn_batal_bayar).setVisibility(View.GONE); // Atau ganti jadi "Tutup"

        // Set Data
        tvHeader.setText("E-Tiket Bioskop");
        tvTitle.setText(ticket.getMovieTitle());
        tvSeats.setText("Kursi: " + ticket.getSeats().toString().replace("[","").replace("]",""));
        
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvPrice.setText("Jadwal: " + ticket.getShowTime()); // Ganti total harga jadi jadwal atau tampilkan keduanya
        
        tvScan.setText("Tunjukkan QR Code ini ke petugas bioskop");

        builder.setView(view);
        builder.setPositiveButton("Tutup", null);
        builder.show();
    }
}