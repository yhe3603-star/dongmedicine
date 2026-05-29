package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;

import java.util.Objects;

public class PlantAdapter extends ListAdapter<Plant, PlantAdapter.PlantViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Plant plant);
    }

    public PlantAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Plant>() {
            @Override
            public boolean areItemsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
                return Objects.equals(oldItem.getName(), newItem.getName())
                        && Objects.equals(oldItem.getScientificName(), newItem.getScientificName())
                        && Objects.equals(oldItem.getDescription(), newItem.getDescription())
                        && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                        && Objects.equals(oldItem.getEffects(), newItem.getEffects());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = getCurrentList().get(position);
        holder.bind(plant);
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView scientificNameText;
        private TextView descriptionText;
        private ImageView plantImage;

        PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.plant_name);
            scientificNameText = itemView.findViewById(R.id.plant_scientific_name);
            descriptionText = itemView.findViewById(R.id.plant_description);
            plantImage = itemView.findViewById(R.id.plant_image);
        }

        void bind(Plant plant) {
            nameText.setText(plant.getName());
            scientificNameText.setText(plant.getScientificName());
            
            if (plant.getDescription() != null && !plant.getDescription().isEmpty()) {
                descriptionText.setText(plant.getDescription());
            } else if (plant.getEffects() != null && !plant.getEffects().isEmpty()) {
                descriptionText.setText(plant.getEffects());
            } else {
                descriptionText.setText("暂无描述信息");
            }

            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(plant.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(plantImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(plant);
                }
            });
        }
    }
}
