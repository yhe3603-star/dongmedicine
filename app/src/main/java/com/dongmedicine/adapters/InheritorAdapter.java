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
import com.dongmedicine.data.model.Inheritor;

public class InheritorAdapter extends ListAdapter<Inheritor, InheritorAdapter.InheritorViewHolder> {

    private OnItemClickListener listener;

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
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public InheritorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inheritor, parent, false);
        return new InheritorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InheritorViewHolder holder, int position) {
        Inheritor inheritor = getCurrentList().get(position);
        holder.bind(inheritor);
    }

    class InheritorViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView titleText;
        private TextView specializationText;
        private ImageView inheritorImage;

        InheritorViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.inheritor_name);
            titleText = itemView.findViewById(R.id.inheritor_title);
            specializationText = itemView.findViewById(R.id.inheritor_specialization);
            inheritorImage = itemView.findViewById(R.id.inheritor_image);
        }

        void bind(Inheritor inheritor) {
            nameText.setText(inheritor.getName());
            titleText.setText(inheritor.getTitle());
            specializationText.setText(inheritor.getSpecialization());

            if (inheritor.getImageUrl() != null && !inheritor.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(inheritor.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(inheritorImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(inheritor);
                }
            });
        }
    }
}
