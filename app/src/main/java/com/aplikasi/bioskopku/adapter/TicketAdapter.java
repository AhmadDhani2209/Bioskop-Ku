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
    private final boolean isHistoryMode;

    // PERBAIKAN: Pisahkan listener untuk klik item dan klik batal
    private final OnTicketActionListener actionListener;

    public interface OnTicketActionListener {
        void onTicketClick(Ticket ticket);
        void onCancelTicket(String ticketId); // Untuk batal
        void onDeleteHistory(String ticketId); // Untuk hapus riwayat
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

        Glide.with(context).load(ticket.getMoviePoster()).placeholder(R.drawable.ic_image_broken).into(holder.ivPoster);

        // PERBAIKAN: Logika tombol berdasarkan mode
        if (isHistoryMode) {
            holder.tvCancel.setText("Hapus Riwayat");
            holder.tvCancel.setOnClickListener(v -> actionListener.onDeleteHistory(ticket.getTicketId()));
        } else {
            holder.tvCancel.setText("Batalkan Tiket");
            holder.tvCancel.setOnClickListener(v -> actionListener.onCancelTicket(ticket.getTicketId()));
        }
        
        // PERBAIKAN: Klik pada seluruh item
        holder.itemView.setOnClickListener(v -> actionListener.onTicketClick(ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvShowTime, tvSeats, tvTotalPrice, tvCancel;
        ImageView ivPoster;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_movie_title_ticket);
            tvShowTime = itemView.findViewById(R.id.tv_showtime_ticket);
            tvSeats = itemView.findViewById(R.id.tv_seats_ticket);
            tvTotalPrice = itemView.findViewById(R.id.tv_price_ticket);
            ivPoster = itemView.findViewById(R.id.iv_poster_ticket);
            tvCancel = itemView.findViewById(R.id.tv_cancel_ticket);
        }
    }
}
