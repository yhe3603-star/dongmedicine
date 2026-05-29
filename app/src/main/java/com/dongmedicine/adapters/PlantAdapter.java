package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.databinding.ItemPlantBinding;

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
        ItemPlantBinding binding = ItemPlantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PlantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = getCurrentList().get(position);
        holder.bind(plant);
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {
        private final ItemPlantBinding binding;

        PlantViewHolder(@NonNull ItemPlantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Plant plant) {
            binding.plantName.setText(plant.getName());
            binding.plantScientificName.setText(plant.getScientificName());

            if (plant.getDescription() != null && !plant.getDescription().isEmpty()) {
                binding.plantDescription.setText(plant.getDescription());
            } else if (plant.getEffects() != null && !plant.getEffects().isEmpty()) {
                binding.plantDescription.setText(plant.getEffects());
            } else {
                binding.plantDescription.setText(itemView.getContext().getString(R.string.no_description));
            }

            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(plant.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(binding.plantImage);
            } else {
                binding.plantImage.setImageResource(R.drawable.ic_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(plant);
                }
            });
        }
    }
}
