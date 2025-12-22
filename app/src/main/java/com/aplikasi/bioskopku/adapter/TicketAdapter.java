package com.aplikasi.bioskopku.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aplikasi.bioskopku.R;
import com.aplikasi.bioskopku.model.Ticket;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final Context context;
    private final List<Ticket> ticketList;
    private final OnTicketCancelListener cancelListener; // Listener baru

    // Interface untuk komunikasi dari Adapter ke Activity
    public interface OnTicketCancelListener {
        void onCancelTicket(String ticketId);
    }

    public TicketAdapter(Context context, List<Ticket> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
        // Pastikan context adalah instance dari listener
        try {
            this.cancelListener = (OnTicketCancelListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTicketCancelListener");
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

        if (ticket.getSeats() != null && !ticket.getSeats().isEmpty()) {
            String seatsFormatted = TextUtils.join(", ", ticket.getSeats());
            holder.tvSeats.setText("Kursi: " + seatsFormatted);
        } else {
            holder.tvSeats.setText("Kursi: -");
        }

        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        holder.tvTotalPrice.setText(rupiah.format(ticket.getTotalPrice()));

        Glide.with(context)
                .load(ticket.getMoviePoster())
                .placeholder(R.drawable.ic_image_broken)
                .into(holder.ivPoster);

        // PERBAIKAN: Menambahkan OnClickListener untuk tombol batal
        holder.tvCancel.setOnClickListener(v -> {
            if (ticket.getTicketId() != null) {
                cancelListener.onCancelTicket(ticket.getTicketId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSeats, tvTotalPrice, tvCancel; // tvCancel ditambahkan
        ImageView ivPoster;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_movie_title_ticket);
            tvSeats = itemView.findViewById(R.id.tv_seats_ticket);
            tvTotalPrice = itemView.findViewById(R.id.tv_price_ticket);
            ivPoster = itemView.findViewById(R.id.iv_poster_ticket);
            tvCancel = itemView.findViewById(R.id.tv_cancel_ticket); // Hubungkan dengan ID tombol batal
        }
    }
}
