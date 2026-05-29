# Phase 3: UI Polish — Dark Mode + Accessibility + RecyclerView — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete dark mode support, add accessibility content descriptions, and fix RecyclerView spacing.

**Architecture:** Dark mode works by defining all colors in both `values/colors.xml` and `values-night/colors.xml`. The `Theme.Material3.DayNight.NoActionBar` parent theme automatically switches. Layouts already reference `@color/` after Phase 1's color extraction.

**Tech Stack:** Android Resource system (values/ vs values-night/), ItemDecoration, Material Design 3

**Prerequisites:** Phase 1 complete (all hardcoded colors extracted to `colors.xml`), Phase 2 complete.

---

### Task 1: Complete dark mode color definitions

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values-night/colors.xml` (replace existing empty themes)

- [ ] **Step 1: Update values/colors.xml with all app colors**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Base -->
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>

    <!-- Theme colors (light mode) -->
    <color name="primary">#2E7D32</color>
    <color name="primary_dark">#1B5E20</color>
    <color name="primary_light">#81C784</color>
    <color name="accent">#FF6F00</color>

    <!-- Surface and background -->
    <color name="background_primary">#FFFFFF</color>
    <color name="background_secondary">#F5F5F5</color>
    <color name="card_background">#FFFFFF</color>

    <!-- Text -->
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
    <color name="divider">#BDBDBD</color>

    <!-- Stats card backgrounds -->
    <color name="stats_blue_bg">#E3F2FD</color>
    <color name="stats_blue_text">#1565C0</color>
    <color name="stats_orange_bg">#FFF3E0</color>
    <color name="stats_orange_text">#E65100</color>
    <color name="stats_purple_bg">#F3E5F5</color>
    <color name="stats_purple_text">#7B1FA2</color>

    <!-- Quick entry buttons -->
    <color name="button_blue">#1976D2</color>
    <color name="button_orange">#F57C00</color>
    <color name="button_purple">#7B1FA2</color>

    <!-- Category tag -->
    <color name="category_tag_bg">#E8F5E9</color>
</resources>
```

- [ ] **Step 2: Create values-night/colors.xml**

Create `app/src/main/res/values-night/colors.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Base -->
    <color name="black">#FFFFFFFF</color>
    <color name="white">#FF000000</color>

    <!-- Theme colors (dark mode) -->
    <color name="primary">#81C784</color>
    <color name="primary_dark">#1B5E20</color>
    <color name="primary_light">#A5D6A7</color>
    <color name="accent">#FFB74D</color>

    <!-- Surface and background -->
    <color name="background_primary">#121212</color>
    <color name="background_secondary">#1E1E1E</color>
    <color name="card_background">#1E1E1E</color>

    <!-- Text -->
    <color name="text_primary">#E0E0E0</color>
    <color name="text_secondary">#AAAAAA</color>
    <color name="divider">#424242</color>

    <!-- Stats card backgrounds -->
    <color name="stats_blue_bg">#1A237E</color>
    <color name="stats_blue_text">#90CAF9</color>
    <color name="stats_orange_bg">#3E2723</color>
    <color name="stats_orange_text">#FFB74D</color>
    <color name="stats_purple_bg">#4A148C</color>
    <color name="stats_purple_text">#CE93D8</color>

    <!-- Quick entry buttons -->
    <color name="button_blue">#90CAF9</color>
    <color name="button_orange">#FFB74D</color>
    <color name="button_purple">#CE93D8</color>

    <!-- Category tag -->
    <color name="category_tag_bg">#1B5E20</color>
</resources>
```

- [ ] **Step 3: Update dark theme in values-night/themes.xml**

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.Dongmedicine" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryDark">@color/primary_dark</item>
        <item name="colorAccent">@color/accent</item>
        <item name="android:statusBarColor">@color/primary_dark</item>
        <item name="android:navigationBarColor">@color/primary_dark</item>
        <item name="android:colorBackground">@color/background_primary</item>
    </style>

    <style name="Theme.Dongmedicine" parent="Base.Theme.Dongmedicine" />
</resources>
```

- [ ] **Step 4: Update light theme to add android:colorBackground**

Edit `values/themes.xml` to add:
```xml
<item name="android:colorBackground">@color/background_primary</item>
```

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/values/colors.xml app/src/main/res/values-night/ app/src/main/res/values/themes.xml
git commit -m "feat: complete dark mode color scheme for all app surfaces"
```

---

### Task 2: Add placeholder drawable

**Files:**
- Create: `app/src/main/res/drawable/ic_placeholder.xml`

- [ ] **Step 1: Create ic_placeholder.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/text_secondary"
        android:pathData="M21,19V5c0,-1.1 -0.9,-2 -2,-2H5c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2zM8.5,13.5l2.5,3.01L14.5,12l4.5,6H5l3.5,-4.5z" />
</vector>
```

- [ ] **Step 2: Replace all ic_launcher_foreground usages with ic_placeholder**

In these files, replace `R.drawable.ic_launcher_foreground` with `R.drawable.ic_placeholder`:
- `PlantAdapter.java` — Glide placeholder + error
- `InheritorAdapter.java` — Glide placeholder + error
- `PlantDetailFragment.java` — Glide placeholder + error
- `InheritorDetailFragment.java` — Glide placeholder + error

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/drawable/ic_placeholder.xml app/src/main/java/com/dongmedicine/adapters/ app/src/main/java/com/dongmedicine/ui/
git commit -m "feat: add dedicated placeholder drawable for image loading"
```

---

### Task 3: Add accessibility content descriptions

**Files:**
- Modify: all layout XML files with ImageViews

- [ ] **Step 1: Add contentDescription to item layouts**

In `item_plant.xml`, add to the ImageView:
```xml
android:contentDescription="@string/desc_plant_image"
```

In `item_inheritor.xml`, add to the ImageView:
```xml
android:contentDescription="@string/desc_inheritor_image"
```

- [ ] **Step 2: Add contentDescription to detail layouts**

In `fragment_plant_detail.xml`, add to the ImageView:
```xml
android:contentDescription="@string/desc_plant_image"
```

In `fragment_inheritor_detail.xml`, add to the ImageView:
```xml
android:contentDescription="@string/desc_inheritor_image"
```

- [ ] **Step 3: Add contentDescription to home fragment icons**

In `fragment_home.xml`, add `contentDescription` to each ImageView using the appropriate string resource (`@string/desc_home_plants`, etc.).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/
git commit -m "a11y: add contentDescription to all ImageViews for accessibility"
```

---

### Task 4: RecyclerView spacing fix

**Files:**
- Modify: `item_plant.xml`, `item_inheritor.xml`, `item_knowledge.xml`, `item_qa.xml`
- Modify: `fragment_plants.xml`, `fragment_inheritors.xml`, `fragment_knowledge.xml`, `fragment_qa.xml`

- [ ] **Step 1: Remove item margins from item layouts**

In `item_plant.xml`, `item_inheritor.xml`, `item_knowledge.xml`, `item_qa.xml`:
Remove `android:layout_margin="8dp"` from the root element.

- [ ] **Step 2: Remove RecyclerView padding from fragment layouts**

In `fragment_plants.xml`, `fragment_inheritors.xml`, `fragment_knowledge.xml`, `fragment_qa.xml`:
Remove `android:padding="8dp"` from the RecyclerView element.

- [ ] **Step 3: Create SpaceItemDecoration.java**

New file: `app/src/main/java/com/dongmedicine/adapters/SpaceItemDecoration.java`

```java
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
```

- [ ] **Step 4: Add ItemDecoration to each list Fragment**

In `PlantsFragment.java`, `InheritorsFragment.java`, `KnowledgeFragment.java`, `QaFragment.java`:
After `setupRecyclerView()`, add:

```java
int spacing = (int) (8 * getResources().getDisplayMetrics().density);
binding.recyclerView.addItemDecoration(new SpaceItemDecoration(spacing));
```

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/dongmedicine/adapters/ app/src/main/java/com/dongmedicine/ui/ app/src/main/res/layout/
git commit -m "fix: use ItemDecoration for RecyclerView spacing, remove double padding"
```

---

### Task 5: Add QaAdapter click listener

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/adapters/QaAdapter.java`

- [ ] **Step 1: Add OnItemClickListener to QaAdapter**

```java
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
import com.dongmedicine.ui.qa.QaViewModel;

import java.util.Objects;

public class QaAdapter extends ListAdapter<QaViewModel.QaItem, QaAdapter.QaViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(QaViewModel.QaItem item);
    }

    public QaAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<QaViewModel.QaItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull QaViewModel.QaItem oldItem, @NonNull QaViewModel.QaItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull QaViewModel.QaItem oldItem, @NonNull QaViewModel.QaItem newItem) {
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
        holder.bind(getCurrentList().get(position));
    }

    class QaViewHolder extends RecyclerView.ViewHolder {
        private final TextView questionText;
        private final TextView answerText;
        private final TextView categoryText;

        QaViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.qa_question);
            answerText = itemView.findViewById(R.id.qa_answer);
            categoryText = itemView.findViewById(R.id.qa_category);
        }

        void bind(QaViewModel.QaItem item) {
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
```

- [ ] **Step 2: Update QaFragment to pass listener to adapter**

Read `QaFragment.java`, then update the adapter instantiation to pass a listener (even if it's a no-op for now):

```java
adapter = new QaAdapter(item -> {
    // Expand/collapse or navigate to detail
});
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/adapters/QaAdapter.java app/src/main/java/com/dongmedicine/ui/qa/QaFragment.java
git commit -m "feat: add click listener to QaAdapter for consistency"
```

---

### Task 6: Final Phase 3 verification

- [ ] **Step 1: Clean build**

```bash
./gradlew clean assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify no hardcoded colors remain**

```bash
grep -rn 'android:color/black\|android:color/darker_gray\|#[0-9A-Fa-f]\{6\}' app/src/main/res/layout/ app/src/main/res/drawable/ || echo "No hardcoded colors found"
```
Expected: No hardcoded colors found (except in `tools:` attributes which are OK)

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "chore: Phase 3 complete — dark mode, accessibility, RecyclerView polish"
```
