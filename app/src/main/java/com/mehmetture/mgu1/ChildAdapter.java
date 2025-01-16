package com.mehmetture.mgu1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Child> childList;
    private OnChildClickListener onChildClickListener;

    public interface OnChildClickListener {
        void onChildClick(Child child);
    }

    public ChildAdapter(List<Child> childList, OnChildClickListener onChildClickListener) {
        this.childList = childList;
        this.onChildClickListener = onChildClickListener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = childList.get(position);
        holder.emailTextView.setText(child.getEmail());
        holder.roleTextView.setText(child.getRole());

        // Tıklama olayı
        holder.itemView.setOnClickListener(v -> {
            if (onChildClickListener != null) {
                onChildClickListener.onChildClick(child);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView, roleTextView;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.childEmailTextView);
            roleTextView = itemView.findViewById(R.id.childRoleTextView);
        }
    }
}
