package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dongmedicine.R;
import com.dongmedicine.data.model.KnowledgeItem;

public class KnowledgeAdapter extends ListAdapter<KnowledgeItem, KnowledgeAdapter.KnowledgeViewHolder> {

    private OnItemClickListener listener;

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
                return oldItem.getTitle().equals(newItem.getTitle());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public KnowledgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_knowledge, parent, false);
        return new KnowledgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KnowledgeViewHolder holder, int position) {
        KnowledgeItem item = getCurrentList().get(position);
        holder.bind(item);
    }

    class KnowledgeViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView categoryText;
        private TextView authorText;
        private TextView dateText;

        KnowledgeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.knowledge_title);
            categoryText = itemView.findViewById(R.id.knowledge_category);
            authorText = itemView.findViewById(R.id.knowledge_author);
            dateText = itemView.findViewById(R.id.knowledge_date);
        }

        void bind(KnowledgeItem item) {
            titleText.setText(item.getTitle());
            categoryText.setText(item.getCategory());
            authorText.setText(item.getAuthor());
            dateText.setText(item.getPublishDate());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
