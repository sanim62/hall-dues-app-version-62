package com.example.halldues;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder> {

    private List<PaymentRecord> paymentRecords;

    public PaymentHistoryAdapter(List<PaymentRecord> paymentRecords) {
        this.paymentRecords = paymentRecords;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_history, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentRecord record = paymentRecords.get(position);
        holder.tvMonth.setText(record.getMonth());
        holder.tvTotalAmount.setText(String.format("$%.2f", record.getTotal()));
    }

    @Override
    public int getItemCount() {
        return paymentRecords.size();
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvMonth;
        TextView tvTotalAmount;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
        }
    }
}
