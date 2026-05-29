package com.dongmedicine.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int spacePx;

    public SpaceItemDecoration(int spacePx) {
        this.spacePx = spacePx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        outRect.left = spacePx;
        outRect.right = spacePx;
        outRect.top = spacePx;
        if (position == itemCount - 1) {
            outRect.bottom = spacePx;
        }
    }
}
