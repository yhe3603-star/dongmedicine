package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.databinding.ItemKnowledgeBinding;

import java.util.Objects;

public class KnowledgeAdapter extends ListAdapter<KnowledgeItem, KnowledgeAdapter.KnowledgeViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(KnowledgeItem item);
    }

    public KnowledgeAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<KnowledgeItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull KnowledgeItem oldItem, @NonNull KnowledgeItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull KnowledgeItem oldItem, @NonNull KnowledgeItem newItem) {
                return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                        && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                        && Objects.equals(oldItem.getContent(), newItem.getContent())
                        && Objects.equals(oldItem.getAuthor(), newItem.getAuthor());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public KnowledgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemKnowledgeBinding binding = ItemKnowledgeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new KnowledgeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull KnowledgeViewHolder holder, int position) {
        KnowledgeItem item = getCurrentList().get(position);
        holder.bind(item);
    }

    class KnowledgeViewHolder extends RecyclerView.ViewHolder {
        private final ItemKnowledgeBinding binding;

        KnowledgeViewHolder(@NonNull ItemKnowledgeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(KnowledgeItem item) {
            binding.knowledgeTitle.setText(item.getTitle());
            binding.knowledgeCategory.setText(item.getCategory());
            binding.knowledgeAuthor.setText(item.getAuthor());
            binding.knowledgeDate.setText(item.getPublishDate());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
