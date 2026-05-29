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
import com.dongmedicine.data.model.QaItem;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qa, parent, false);
        return new QaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QaViewHolder holder, int position) {
        QaItem item = getCurrentList().get(position);
        holder.bind(item);
    }

    class QaViewHolder extends RecyclerView.ViewHolder {
        private TextView questionText;
        private TextView answerText;
        private TextView categoryText;

        QaViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.qa_question);
            answerText = itemView.findViewById(R.id.qa_answer);
            categoryText = itemView.findViewById(R.id.qa_category);
        }

        void bind(QaItem item) {
            questionText.setText(item.getQuestion());
            answerText.setText(item.getAnswer());
            categoryText.setText(item.getCategory());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
