# 遗留问题修复计划 (Phase 2)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复分析报告中剩余的中低优先级问题

**Architecture:** Phase A 修复资源/布局问题；Phase B 清理构建配置和代码；Phase C 将 Adapters 迁移到 ViewBinding

**Tech Stack:** Java 11, Android SDK 35, ViewBinding, ProGuard

---

## Phase A: 资源/布局修复

### Task 1: RecyclerView item 占位符改用 tools:text

**Files:**
- Modify: `app/src/main/res/layout/item_plant.xml`
- Modify: `app/src/main/res/layout/item_inheritor.xml`
- Modify: `app/src/main/res/layout/item_knowledge.xml`
- Modify: `app/src/main/res/layout/item_qa.xml`

**说明:** RecyclerView item 布局中的 `android:text` 设置了占位中文文本。这些文本在运行时会被 Adapter 覆盖，但在设计预览中显示。应改用 `tools:text` 以避免在运行时短暂显示占位文本，并正确支持本地化预览。

- [ ] **Step 1: 修改 item_plant.xml**

在根 `CardView` 标签中添加 tools namespace：
```xml
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
```

替换 3 处：
- 第 34 行 `android:text="植物名称"` → `tools:text="植物名称"`
- 第 44 行 `android:text="学名"` → `tools:text="学名"`
- 第 56 行 `android:text="功效描述"` → `tools:text="功效描述"`

- [ ] **Step 2: 修改 item_inheritor.xml**

添加 tools namespace，替换 3 处：
- 第 34 行 `android:text="传承人姓名"` → `tools:text="传承人姓名"`
- 第 44 行 `android:text="国家级传承人"` → `tools:text="国家级传承人"`
- 第 53 行 `android:text="擅长领域"` → `tools:text="擅长领域"`

- [ ] **Step 3: 修改 item_knowledge.xml**

添加 tools namespace，替换 4 处：
- 第 20 行 `android:text="知识标题"` → `tools:text="知识标题"`
- 第 37 行 `android:text="分类"` → `tools:text="分类"`
- 第 46 行 `android:text="作者"` → `tools:text="作者"`
- 第 55 行 `android:text="2024-01-01"` → `tools:text="2024-01-01"`

- [ ] **Step 4: 修改 item_qa.xml**

添加 tools namespace，替换 3 处：
- 第 26 行 `android:text="问题"` → `tools:text="问题"`
- 第 37 行 `android:text="分类"` → `tools:text="分类"`
- 第 49 行 `android:text="回答"` → `tools:text="回答"`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/item_plant.xml \
       app/src/main/res/layout/item_inheritor.xml \
       app/src/main/res/layout/item_knowledge.xml \
       app/src/main/res/layout/item_qa.xml
git commit -m "fix: replace android:text placeholders with tools:text in RecyclerView items"
```

---

### Task 2: 首页统计默认值改用 tools:text

**Files:**
- Modify: `app/src/main/res/layout/fragment_home.xml`

**说明:** 统计卡片中的数字（156, 23, 89, 1024）使用 `android:text` 作为默认值。HomeFragment 在 `setupStatistics()` 中通过 ViewModel 设置实际值。应改用 `tools:text` 以避免显示硬编码数字。

- [ ] **Step 1: 修改 fragment_home.xml**

添加 tools namespace 到根标签：
```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
```

替换 4 处：
- 第 75 行 `android:text="156"` → `tools:text="156"`
- 第 108 行 `android:text="23"` → `tools:text="23"`
- 第 141 行 `android:text="89"` → `tools:text="89"`
- 第 174 行 `android:text="1024"` → `tools:text="1024"`

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/fragment_home.xml
git commit -m "fix: use tools:text for hardcoded stats defaults in home layout"
```

---

### Task 3: 移除未使用的依赖

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml` (optional — remove entries)

**说明:** `MPAndroidChart` 和 `ViewPager2` 已声明为依赖但无任何源码引用。

- [ ] **Step 1: 从 build.gradle.kts 注释/移除**

移除或注释掉第 57 行和第 65 行：
```kotlin
// 移除: implementation(libs.mpandroidchart)
// 移除: implementation(libs.viewpager2)
```

- [ ] **Step 2: 验证**

运行 `./gradlew assembleDebug` 确认编译通过（无源码引用这些库）。

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "chore: remove unused MPAndroidChart and ViewPager2 dependencies"
```

---

### Task 4: 启用 ProGuard/R8 并添加规则

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/proguard-rules.pro`

**说明:** Release 构建未启用代码混淆和压缩。

- [ ] **Step 1: 在 build.gradle.kts 中启用 minification**

将第 29 行 `isMinifyEnabled = false` 改为 `isMinifyEnabled = true`。

- [ ] **Step 2: 添加 ProGuard 规则**

将 `app/proguard-rules.pro` 的内容替换为：

```proguard
# === Retrofit ===
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# === Gson ===
# Keep Gson TypeToken and its subclasses with generic signatures.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
# Keep classes that Gson uses for serialization/deserialization.
-keep class com.dongmedicine.data.model.** { *; }
-keep class com.dongmedicine.data.api.ApiService$ApiResponse { *; }

# === Room ===
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
}
-keep class com.dongmedicine.data.local.** { *; }

# === Hilt ===
# Hilt generates code that references application class.
-keep class com.dongmedicine.DongmedicineApplication { *; }

# === Glide ===
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder { *** rewind(); }
```

- [ ] **Step 3: 验证**

运行 `./gradlew assembleRelease` 确认混淆构建通过。

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "chore: enable ProGuard/R8 with rules for Retrofit, Gson, Room, Hilt, Glide"
```

---

## Phase B: 代码和配置清理

### Task 5: 修复 Material3 主题属性

**Files:**
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/res/values-night/themes.xml`

**说明:** `colorAccent` 是 Material Components 的旧属性，在 Material3 中已弃用。应改用 `colorSecondary`。夜间主题文件与日间完全相同，应删除以让 Material3.DayNight 自动处理，或至少更新属性。

- [ ] **Step 1: 修改 values/themes.xml**

将第 5 行：
```xml
        <item name="colorAccent">@color/accent</item>
```
替换为：
```xml
        <item name="colorSecondary">@color/accent</item>
```

- [ ] **Step 2: 更新 values-night/themes.xml**

将第 5 行同样替换：
```xml
        <item name="colorSecondary">@color/accent</item>
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/themes.xml app/src/main/res/values-night/themes.xml
git commit -m "fix: replace deprecated colorAccent with colorSecondary for Material3"
```

---

### Task 6: 设置 allowBackup="false"

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 修改 AndroidManifest.xml**

将第 10 行：
```xml
        android:allowBackup="true"
```
替换为：
```xml
        android:allowBackup="false"
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "fix: disable allowBackup to protect local Room database"
```

---

### Task 7: 移除未使用的 API 端点

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/api/ApiService.java`

**说明:** `getPlantsByCategory()` 和 `getKnowledgeByCategory()` 已定义但从未被调用。筛选在客户端完成。

- [ ] **Step 1: 删除未使用的端点**

从 `ApiService.java` 中删除：
```java
    @GET("api/plants/list")
    Call<ApiResponse<List<Plant>>> getPlantsByCategory(@Query("category") String category);
```
（第 22-23 行）

和：
```java
    @GET("api/knowledge/list")
    Call<ApiResponse<List<KnowledgeItem>>> getKnowledgeByCategory(@Query("category") String category);
```
（第 38-39 行）

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/dongmedicine/data/api/ApiService.java
git commit -m "chore: remove unused API endpoints getPlantsByCategory and getKnowledgeByCategory"
```

---

## Phase C: Adapters ViewBinding 迁移

### Task 8: 将 PlantAdapter 和 InheritorAdapter 迁移到 ViewBinding

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/adapters/PlantAdapter.java`
- Modify: `app/src/main/java/com/dongmedicine/adapters/InheritorAdapter.java`

**说明:** 项目已启用 ViewBinding，但 Adapters 仍使用 `findViewById`。迁移到 ViewBinding 提供类型安全和消除 ID 错误风险。

- [ ] **Step 1: 重写 PlantAdapter**

将 `PlantAdapter.java` 替换为：

```java
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
```

- [ ] **Step 2: 重写 InheritorAdapter**

将 `InheritorAdapter.java` 替换为：

```java
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
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/adapters/PlantAdapter.java \
       app/src/main/java/com/dongmedicine/adapters/InheritorAdapter.java
git commit -m "refactor: migrate PlantAdapter and InheritorAdapter to ViewBinding"
```

---

### Task 9: 将 KnowledgeAdapter 和 QaAdapter 迁移到 ViewBinding

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/adapters/KnowledgeAdapter.java`
- Modify: `app/src/main/java/com/dongmedicine/adapters/QaAdapter.java`

- [ ] **Step 1: 重写 KnowledgeAdapter**

将 `KnowledgeAdapter.java` 替换为：

```java
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
```

- [ ] **Step 2: 重写 QaAdapter**

将 `QaAdapter.java` 替换为：

```java
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
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/adapters/KnowledgeAdapter.java \
       app/src/main/java/com/dongmedicine/adapters/QaAdapter.java
git commit -m "refactor: migrate KnowledgeAdapter and QaAdapter to ViewBinding"
```

---

## 遗留项（不在本计划中）

以下问题风险较高或收益较低，留待后续处理：

1. **ViewModel/Fragment 基类提取** — 3 组类各自重复，但提取基类涉及 Hilt 注入兼容性，需单独规划
2. **ApiResponse 提取为独立类** — 低耦合收益，改动范围小但影响所有 Repository 方法签名
3. **导航转场动画** — 用户体验改进，不影响功能
4. **DAO 测试** — 需要配置 in-memory Room 数据库，测试基础设施较复杂
5. **UI/Fragment 测试** — 需要 Espresso + Hilt 测试配置
