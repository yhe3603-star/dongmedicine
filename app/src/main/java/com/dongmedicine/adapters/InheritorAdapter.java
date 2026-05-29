package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.databinding.ItemInheritorBinding;

import java.util.Objects;

public class InheritorAdapter extends ListAdapter<Inheritor, InheritorAdapter.InheritorViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Inheritor inheritor);
    }

    public InheritorAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Inheritor>() {
            @Override
            public boolean areItemsTheSame(@NonNull Inheritor oldItem, @NonNull Inheritor newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Inheritor oldItem, @NonNull Inheritor newItem) {
                return Objects.equals(oldItem.getName(), newItem.getName())
                        && Objects.equals(oldItem.getTitle(), newItem.getTitle())
                        && Objects.equals(oldItem.getSpecialization(), newItem.getSpecialization())
                        && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public InheritorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInheritorBinding binding = ItemInheritorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new InheritorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InheritorViewHolder holder, int position) {
        Inheritor inheritor = getCurrentList().get(position);
        holder.bind(inheritor);
    }

    class InheritorViewHolder extends RecyclerView.ViewHolder {
        private final ItemInheritorBinding binding;

        InheritorViewHolder(@NonNull ItemInheritorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Inheritor inheritor) {
            binding.inheritorName.setText(inheritor.getName());
            binding.inheritorTitle.setText(inheritor.getTitle());
            binding.inheritorSpecialization.setText(inheritor.getSpecialization());

            binding.inheritorImage.setTransitionName("inheritor_image_" + inheritor.getId());

            if (inheritor.getImageUrl() != null && !inheritor.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(inheritor.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .circleCrop()
                        .into(binding.inheritorImage);
            } else {
                binding.inheritorImage.setImageResource(R.drawable.ic_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(inheritor);
                }
            });
        }
    }
}
