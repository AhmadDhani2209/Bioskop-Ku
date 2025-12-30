package com.aplikasi.bioskopku.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements TicketAdapter.OnTicketActionListener {

    private RecyclerView rvHistory;
    private TicketAdapter ticketAdapter;
    private List<Ticket> historyList;
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
        historyList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(this, historyList, true);
        rvHistory.setAdapter(ticketAdapter);

        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_history) return true;

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

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_history);
        fetchHistoryTickets(); 
    }

    private void fetchHistoryTickets() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        if (ticketQuery != null && ticketListener != null) {
            ticketQuery.removeEventListener(ticketListener);
        }

        progressBar.setVisibility(View.VISIBLE);
        String myUid = user.getUid();
        ticketQuery = ticketsRef.orderByChild("userId").equalTo(myUid);

        ticketListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                long currentTime = System.currentTimeMillis();

                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Ticket ticket = data.getValue(Ticket.class);
                        if (ticket != null) {
                            ticket.setTicketId(data.getKey());
                            
                            // LOGIKA HISTORY:
                            // Tampilkan jika jam tayang SUDAH LEWAT
                            long showTime = ticket.getShowTimestamp();
                            
                            // Jika showTime valid dan SUDAH LEWAT (lebih kecil dari sekarang)
                            // Atau jika tidak valid, masuk history juga
                            if (showTime != -1 && showTime < currentTime) {
                                historyList.add(ticket);
                            } 
                            // Untuk amannya, tiket tanpa jam juga dimasukkan ke history (sudah usang)
                            else if (showTime == -1) {
                                historyList.add(ticket);
                            }
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                if (historyList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                } else {
                    Collections.reverse(historyList);
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
        };
        ticketQuery.addValueEventListener(ticketListener);
    }

    @Override
    public void onTicketClick(Ticket ticket) {
        showTicketDetailDialog(ticket);
    }

    @Override
    public void onCancelTicket(String ticketId) { }

    @Override
    public void onDeleteHistory(String ticketId) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat")
                .setMessage("Anda yakin ingin menghapus riwayat tiket ini?")
                .setPositiveButton("Hapus", (dialog, which) -> 
                    ticketsRef.child(ticketId).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(HistoryActivity.this, "Riwayat dihapus", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(HistoryActivity.this, "Gagal menghapus", Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showTicketDetailDialog(Ticket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_detail, null);

        TextView tvHeader = view.findViewById(R.id.dialog_header);
        TextView tvTitle = view.findViewById(R.id.dialog_movie_title);
        TextView tvShowtime = view.findViewById(R.id.dialog_showtime);
        TextView tvSeats = view.findViewById(R.id.dialog_seats);
        ImageView ivQr = view.findViewById(R.id.iv_qr_code);
        ImageView ivPoster = view.findViewById(R.id.iv_movie_poster); // Pastikan ID ini ada di layout dialog
        Button btnClose = view.findViewById(R.id.btn_close_dialog);

        tvHeader.setText("Riwayat Tiket");
        tvTitle.setText(ticket.getMovieTitle());
        
        String showTime = ticket.getShowTime();
        tvShowtime.setText(showTime != null ? "Jadwal: " + showTime : "Jadwal: -");
        
        String seats = ticket.getSeats() != null ? ticket.getSeats().toString().replace("[","").replace("]","") : "-";
        tvSeats.setText("Kursi: " + seats);

        // Load poster logic here for the dialog, similar to Adapter
        String posterUrl = ticket.getMoviePoster();
        if (posterUrl != null && ivPoster != null) {
            if (posterUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl);
                Glide.with(this).load(storageReference).placeholder(R.drawable.ic_image_broken).into(ivPoster);
            } else {
                Glide.with(this).load(posterUrl).placeholder(R.drawable.ic_image_broken).into(ivPoster);
            }
        }
        
        ivQr.setAlpha(0.5f);

        AlertDialog dialog = builder.setView(view).create();
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (ticketQuery != null && ticketListener != null) {
            ticketQuery.removeEventListener(ticketListener);
        }
    }
}