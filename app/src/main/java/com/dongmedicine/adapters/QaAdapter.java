package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dongmedicine.data.model.QaItem;
import com.dongmedicine.databinding.ItemQaBinding;

import java.util.Objects;

public class QaAdapter extends ListAdapter<QaItem, QaAdapter.QaViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QaItem item);
    }

    public QaAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<QaItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull QaItem oldItem, @NonNull QaItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull QaItem oldItem, @NonNull QaItem newItem) {
                return Objects.equals(oldItem.getQuestion(), newItem.getQuestion())
                        && Objects.equals(oldItem.getAnswer(), newItem.getAnswer())
                        && Objects.equals(oldItem.getCategory(), newItem.getCategory());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public QaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQaBinding binding = ItemQaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QaViewHolder holder, int position) {
        QaItem item = getCurrentList().get(position);
        holder.bind(item);
    }

    class QaViewHolder extends RecyclerView.ViewHolder {
        private final ItemQaBinding binding;

        QaViewHolder(@NonNull ItemQaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(QaItem item) {
            binding.qaQuestion.setText(item.getQuestion());
            binding.qaAnswer.setText(item.getAnswer());
            binding.qaCategory.setText(item.getCategory());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
