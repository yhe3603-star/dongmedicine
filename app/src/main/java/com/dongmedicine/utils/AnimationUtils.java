package com.dongmedicine.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.AnimRes;
import androidx.recyclerview.widget.RecyclerView;

public class AnimationUtils {

    private AnimationUtils() {}

    public static void runLayoutAnimation(RecyclerView recyclerView, @AnimRes int animRes) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller =
                android.view.animation.AnimationUtils.loadLayoutAnimation(context, animRes);
        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }

    public static void animateCardPress(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator(1.5f))
                            .start();
                    break;
            }
            return false;
        });
    }

    public static void animateCount(TextView textView, int targetValue, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation ->
                textView.setText(String.valueOf(animation.getAnimatedValue())));
        animator.start();
    }

    public static void fadeInUp(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .start();
    }

    public static void expandTextView(TextView textView, int targetLines) {
        ObjectAnimator.ofInt(textView, "maxLines", targetLines)
                .setDuration(300)
                .start();
    }
}
