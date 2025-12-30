package com.aplikasi.bioskopku.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.activity.TicketDetailActivity;
import com.aplikasi.bioskopku.model.Ticket;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final Context context;
    private final List<Ticket> ticketList;
    private final boolean isHistoryMode;
    private final OnTicketActionListener actionListener;

    public interface OnTicketActionListener {
        void onTicketClick(Ticket ticket);
        void onCancelTicket(String ticketId);
        void onDeleteHistory(String ticketId);
    }

    public TicketAdapter(Context context, List<Ticket> ticketList, boolean isHistoryMode) {
        this.context = context;
        this.ticketList = ticketList;
        this.isHistoryMode = isHistoryMode;

        if (context instanceof OnTicketActionListener) {
            this.actionListener = (OnTicketActionListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnTicketActionListener");
        }
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        if (ticket == null) return;

        holder.tvTitle.setText(ticket.getMovieTitle());
        holder.tvSeats.setText("Kursi: " + TextUtils.join(", ", ticket.getSeats()));
        
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        holder.tvTotalPrice.setText(rupiah.format(ticket.getTotalPrice()));

        String showTime = ticket.getShowTime();
        holder.tvShowTime.setText(showTime != null ? "Jadwal: " + showTime : "Jadwal: -");
        holder.tvShowTime.setVisibility(showTime != null ? View.VISIBLE : View.GONE);
        
        long timestamp = ticket.getShowTimestamp();
        if (timestamp != -1) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
            holder.tvDate.setText(dateFormat.format(new Date(timestamp)));
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            if (ticket.purchaseDate > 0) {
                 SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy (Pembelian)", new Locale("id", "ID"));
                 holder.tvDate.setText(dateFormat.format(new Date(ticket.purchaseDate)));
                 holder.tvDate.setVisibility(View.VISIBLE);
            } else {
                 holder.tvDate.setVisibility(View.GONE);
            }
        }

        String posterUrl = ticket.getMoviePoster();
        if (posterUrl != null) {
            if (posterUrl.startsWith("gs://")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(posterUrl);
                Glide.with(context)
                        .load(storageReference)
                        .placeholder(R.drawable.ic_image_broken)
                        .into(holder.ivPoster);
            } else {
                Glide.with(context)
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_image_broken)
                        .into(holder.ivPoster);
            }
        } else {
            holder.ivPoster.setImageResource(R.drawable.ic_image_broken);
        }

        if (isHistoryMode) {
            holder.tvCancel.setText("Hapus Riwayat");
            holder.tvCancel.setOnClickListener(v -> actionListener.onDeleteHistory(ticket.getTicketId()));
        } else {
            holder.tvCancel.setText("Batalkan Tiket");
            holder.tvCancel.setOnClickListener(v -> actionListener.onCancelTicket(ticket.getTicketId()));
        }
        
        // PERBAIKAN: Mengarahkan klik item ke TicketDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            intent.putExtra("ticket_data", ticket);
            // Jika ticket ID tersimpan di model, ambil dari situ
            if (ticket.getTicketId() != null) {
                intent.putExtra("ticket_key", ticket.getTicketId());
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvShowTime, tvDate, tvSeats, tvTotalPrice, tvCancel;
        ImageView ivPoster;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_movie_title_ticket);
            tvShowTime = itemView.findViewById(R.id.tv_showtime_ticket);
            tvDate = itemView.findViewById(R.id.tv_date_ticket); 
            tvSeats = itemView.findViewById(R.id.tv_seats_ticket);
            tvTotalPrice = itemView.findViewById(R.id.tv_price_ticket);
            ivPoster = itemView.findViewById(R.id.iv_poster_ticket);
            tvCancel = itemView.findViewById(R.id.tv_cancel_ticket);
        }
    }
}
