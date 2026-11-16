package com.rather.ecosort.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rather.ecosort.R;
import com.rather.ecosort.models.Placemark;

import java.util.Collections;
import java.util.List;

public class CollectionPointsAdapter extends RecyclerView.Adapter<CollectionPointsAdapter.Holder> {
    public interface OnPointClick {
        void onClick(Placemark point);
    }

    private final OnPointClick onPointClick;
    private List<Placemark> points = Collections.emptyList();

    public CollectionPointsAdapter(OnPointClick onPointClick) {
        this.onPointClick = onPointClick;
    }

    public void showPoints(List<Placemark> points) {
        this.points = points;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collection_point_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Placemark point = points.get(position);
        holder.name.setText(point.getTitle());
        holder.address.setText(point.getContent());
        holder.itemView.setOnClickListener(view -> onPointClick.onClick(point));
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView address;

        Holder(View view) {
            super(view);
            name = view.findViewById(R.id.collection_point_name);
            address = view.findViewById(R.id.collection_point_address);
        }
    }
}
