# dongmedicine 全面重构设计方案

## 概述

对 dongmedicine Android 应用进行全面重构，涵盖 4 个阶段：Bug 修复与代码质量提升、架构升级（Hilt DI + Room 离线缓存）、UI 完善（暗色模式 + 无障碍）、单元测试。

语言保持 Java，分阶段实施，每阶段可独立编译验证。

## 阶段 1：Bug 修复 + 代码质量

### 1.1 修复 ViewModel LiveData 观察者丢失

**问题**：5 个 ViewModel 的 `loadXxx()` 方法每次创建新的 `LiveData` 对象赋值给字段，Fragment 的 `observe()` 绑定的是旧对象，导致刷新后观察者丢失。

**影响文件**：
- `PlantsViewModel.java`
- `InheritorsViewModel.java`
- `KnowledgeViewModel.java`
- `PlantDetailViewModel.java`
- `HomeViewModel.java`

**方案**：ViewModel 持有稳定的 `MutableLiveData<Resource<T>>` 字段，`loadXxx()` 方法通过 Repository 更新该已有对象，而非替换引用。

```java
// Before:
private LiveData<Resource<List<Plant>>> plants;
public void loadPlants() {
    plants = repository.getPlants(); // 新 LiveData，观察者丢失
}

// After:
private final MutableLiveData<Resource<List<Plant>>> plants = new MutableLiveData<>();
public void loadPlants() {
    repository.getPlants(plants); // 更新已有 LiveData 的值
}
```

同时修复筛选逻辑依赖过时数据的问题：`applyFilters()` 应在数据加载成功回调中执行，而非在 `loadXxx()` 之后立即调用。

### 1.2 Repository 泛型重构

**问题**：4 个方法包含完全相同的回调模板代码（创建 LiveData → enqueue → onResponse/onFailure 处理）。

**方案**：提取泛型方法 `executeCall()`，所有 API 调用复用：

```java
private <T> void executeCall(Call<ApiResponse<T>> call, MutableLiveData<Resource<T>> liveData) {
    liveData.postValue(Resource.loading(null));
    call.enqueue(new Callback<ApiResponse<T>>() {
        @Override
        public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                liveData.postValue(Resource.success(response.body().getData()));
            } else {
                liveData.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
            }
        }
        @Override
        public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
            liveData.postValue(Resource.error("网络错误: " + t.getMessage(), null));
        }
    });
}
```

Repository 公开方法签名改为接收目标 LiveData 参数：

```java
public void getPlants(MutableLiveData<Resource<List<Plant>>> liveData) {
    executeCall(apiService.getPlants(), liveData);
}
```

> **与阶段 2 的衔接**：阶段 1 的 `executeCall()` 模式是过渡方案。阶段 2 引入 Room 后，Repository 方法签名将改为返回 `LiveData<Resource<T>>`，内部通过 `Transformations.map()` 将 Room DAO 的 LiveData 包装为 Resource，同时异步触发网络请求更新缓存。阶段 1 的 ViewModel 代码在阶段 2 中需要小幅调整以适配新签名。

### 1.3 修复 setValue() 线程安全问题

Retrofit 回调在 OkHttp 后台线程执行，所有 `MutableLiveData.setValue()` 改为 `postValue()`。此修复已在 1.2 的 `executeCall()` 中统一处理。

### 1.4 Safe Args 启用

**当前状态**：`nav_graph.xml` 定义了 `plantId`、`inheritorId`、`knowledgeId` 参数，但代码中使用原始 Bundle 传递。

**方案**：
- `nav_graph.xml` 中为参数添加 `android:defaultValue="0"`
- 发送端使用生成的 Directions 类：
  ```java
  NavDirections action = PlantsFragmentDirections.actionPlantsToPlantDetail(plant.getId());
  Navigation.findNavController(view).navigate(action);
  ```
- 接收端使用 Args 类：
  ```java
  int plantId = PlantDetailFragmentArgs.fromBundle(getArguments()).getPlantId();
  ```
- `build.gradle.kts` 添加 Safe Args 插件：
  ```kotlin
  id("androidx.navigation.safeargs")
  ```
- `settings.gradle.kts` 或根 `build.gradle.kts` 的 `pluginManagement` 中添加 safeargs classpath

### 1.5 修复详情页硬编码数据

**问题**：
- `InheritorDetailFragment.loadSampleData(int id)` 忽略 id 参数，始终显示硬编码的"杨秀华"数据
- `KnowledgeDetailFragment.loadSampleData(int id)` 忽略 id 参数，始终显示硬编码的"侗医药概述"

**方案**：
- 创建 `InheritorDetailViewModel` 和 `KnowledgeDetailViewModel`
- 在 ViewModel 中调用 `repository.getInheritorById(id)` / `repository.getKnowledgeById(id)`
- Fragment 通过 ViewModel 观察数据变化并动态展示

### 1.6 ViewBinding 迁移

**影响文件**：所有 10 个 Fragment

**方案**：
- `build.gradle.kts` 已启用 `viewBinding = true`，无需额外配置
- 每个 Fragment 改为使用 `FragmentXxxBinding.inflate(inflater, container, false)`
- 移除所有 `findViewById()` 调用
- 在 `onDestroyView()` 中置空 binding 引用：

```java
@Override
public void onDestroyView() {
    super.onDestroyView();
    binding = null;
}
```

### 1.7 ApiClient 线程安全 + 日志条件化

- `getRetrofit()` 和 `getApiService()` 加 `synchronized` 关键字
- `setBaseUrl()` 方法内部同步重置
- 日志拦截器仅在 `BuildConfig.DEBUG` 时启用 `Level.BODY`，Release 时禁用或设为 `Level.NONE`

### 1.8 资源规范化

**字符串提取**：所有 Java 硬编码中文和 XML `android:text` 中的界面文本提取到 `strings.xml`，包括：
- 错误消息："加载失败"、"网络错误: "
- 界面标题："植物详情"、"传承人详情"、"知识详情"
- 列表项占位文本
- 分类名称数组（从 ViewModel 移到 `string-array` 资源）
- `nav_graph.xml` 中的 `android:label` 改用 `@string/` 引用

**颜色提取**：所有布局 XML 中的硬编码 hex 颜色提取到 `colors.xml`，包括：
- 首页统计卡片背景色和文字色（6 个颜色）
- 首页功能入口背景色（3 个颜色）
- 分类标签背景色
- Item 布局中的 `@android:color/black` 和 `@android:color/darker_gray` 改为 `@color/text_primary` 和 `@color/text_secondary`

### 1.9 适配器修复

- DiffUtil `areContentsTheSame` 比较所有相关字段，使用 `Objects.equals()` 防 NPE
- `OnItemClickListener` 字段声明为 `final`
- Toolbar 回退按钮改用 `setNavigationOnClickListener()`
- `activity_main.xml` 的 `<fragment>` 标签替换为 `<FragmentContainerView>`

---

## 阶段 2：架构升级（Hilt DI + Room 离线缓存）

### 2.1 Hilt 依赖注入

**Gradle 依赖**（`app/build.gradle.kts`）：
```kotlin
implementation("com.google.dagger:hilt-android:2.51.1")
annotationProcessor("com.google.dagger:hilt-android-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
```

根 `build.gradle.kts` 添加 Hilt 插件：
```kotlin
id("com.google.dagger.hilt.android") version "2.51.1" apply false
```

`app/build.gradle.kts` 应用插件：
```kotlin
id("com.google.dagger.hilt.android")
```

**改造点**：

| 组件 | 注解 | 说明 |
|------|------|------|
| `DongmedicineApplication` (新建) | `@HiltAndroidApp` | Application 入口 |
| `MainActivity` | `@AndroidEntryPoint` | Activity 注入 |
| 所有 Fragment | `@AndroidEntryPoint` | Fragment 注入 |
| 所有 ViewModel | `@HiltViewModel` + `@Inject constructor` | ViewModel 注入 |

**DI 模块**（`di/` 包）：

`NetworkModule.java`：
```java
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides @Singleton
    OkHttpClient provideOkHttpClient() { ... }

    @Provides @Singleton
    Retrofit provideRetrofit(OkHttpClient client) { ... }

    @Provides @Singleton
    ApiService provideApiService(Retrofit retrofit) { ... }
}
```

`DatabaseModule.java`：
```java
@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Provides @Singleton
    DongMedicineDatabase provideDatabase(@ApplicationContext Context context) { ... }

    @Provides
    PlantDao providePlantDao(DongMedicineDatabase db) { ... }
    // 同样为 InheritorDao, KnowledgeDao
}
```

`DongMedicineRepository` 改为：
```java
@Singleton
public class DongMedicineRepository {
    @Inject
    public DongMedicineRepository(ApiService apiService, PlantDao plantDao, ...) { ... }
}
```

**移除**：`ApiClient.java`（职责移入 NetworkModule）、`DongMedicineRepository.getInstance()` 静态单例。

### 2.2 Room 离线缓存

**Gradle 依赖**（已有 `room-runtime` 和 `room-ktx`，新增）：
```kotlin
annotationProcessor("androidx.room:room-compiler:2.6.1")
```

> **注**：Hilt 的 `hilt-android-compiler` 已在阶段 2.1 中配置为 annotationProcessor，它会自动处理 Room 的注解。如果 Hilt 编译器已覆盖 Room 注解处理，则此行可省略。

**实体类**：为 `Plant`、`Inheritor`、`KnowledgeItem` 添加 Room 注解：

```java
@Entity(tableName = "plants")
public class Plant {
    @PrimaryKey
    private int id;
    private String name;
    private String scientificName;
    // ...其他字段
}
```

**DAO 接口**（`data/local/` 包）：

```java
@Dao
public interface PlantDao {
    @Query("SELECT * FROM plants")
    LiveData<List<Plant>> getAllPlants();

    @Query("SELECT * FROM plants WHERE id = :id")
    LiveData<Plant> getPlantById(int id);

    @Query("SELECT * FROM plants WHERE category = :category")
    LiveData<List<Plant>> getPlantsByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Plant> plants);

    @Query("DELETE FROM plants")
    void deleteAll();
}
```

同样为 `Inheritor` 和 `KnowledgeItem` 创建 `InheritorDao` 和 `KnowledgeDao`。

**Database 类**：
```java
@Database(entities = {Plant.class, Inheritor.class, KnowledgeItem.class}, version = 1)
public abstract class DongMedicineDatabase extends RoomDatabase {
    public abstract PlantDao plantDao();
    public abstract InheritorDao inheritorDao();
    public abstract KnowledgeDao knowledgeDao();
}
```

**Repository 网络优先 + 本地缓存策略**：

```java
public LiveData<Resource<List<Plant>>> getPlants() {
    MutableLiveData<Resource<List<Plant>>> result = new MutableLiveData<>();
    result.setValue(Resource.loading(null));

    // 1. 立即返回本地缓存
    LiveData<List<Plant>> cachedData = plantDao.getAllPlants();

    // 2. 异步拉取网络数据
    apiService.getPlants().enqueue(new Callback<ApiResponse<List<Plant>>>() {
        @Override
        public void onResponse(...) {
            if (response.isSuccessful() && body != null && body.isSuccess()) {
                // 3. 写入 Room 缓存，LiveData 自动通知
                executor.execute(() -> plantDao.insertAll(body.getData()));
            }
        }
        @Override
        public void onFailure(...) {
            // 网络失败时，缓存数据仍可用
            result.postValue(Resource.error("网络错误", null));
        }
    });

    // 4. 返回缓存数据的 LiveData（Room 查询结果变化时自动更新）
    return Transformations.map(cachedData, data -> Resource.success(data));
}
```

使用 `Executors.newSingleThreadExecutor()` 执行 Room 写入操作（避免在主线程）。

### 2.3 删除硬编码示例数据

从 `DongMedicineRepository` 中删除 `getSamplePlants()`、`getSampleInheritors()`、`getSampleKnowledge()` 方法。Room 缓存替代其"无网络时展示数据"的角色。

### 2.4 FragmentContainerView 升级

`activity_main.xml` 中的 `<fragment>` 替换为：
```xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/nav_host_fragment"
    android:name="androidx.navigation.fragment.NavHostFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:defaultNavHost="true"
    app:navGraph="@navigation/nav_graph" />
```

---

## 阶段 3：UI 完善

### 3.1 暗色模式支持

完善 `values/colors.xml` 和 `values-night/colors.xml` 对应关系：

| 颜色名 | 亮色值 | 暗色值 | 用途 |
|--------|--------|--------|------|
| `background_primary` | `#FFFFFF` | `#121212` | 主背景 |
| `background_secondary` | `#F5F5F5` | `#1E1E1E` | 卡片背景 |
| `text_primary` | `#212121` | `#E0E0E0` | 主文本 |
| `text_secondary` | `#757575` | `#AAAAAA` | 次要文本 |
| `primary` | `#1976D2` | `#90CAF9` | 主色调 |
| `stats_blue_bg` | `#E3F2FD` | `#1A237E` | 统计卡片蓝色背景 |
| `stats_blue_text` | `#1565C0` | `#90CAF9` | 统计卡片蓝色文字 |
| `stats_orange_bg` | `#FFF3E0` | `#3E2723` | 统计卡片橙色背景 |
| `stats_orange_text` | `#E65100` | `#FFB74D` | 统计卡片橙色文字 |
| `stats_purple_bg` | `#F3E5F5` | `#4A148C` | 统计卡片紫色背景 |
| `stats_purple_text` | `#7B1FA2` | `#CE93D8` | 统计卡片紫色文字 |
| `category_tag_bg` | `#E8F5E9` | `#1B5E20` | 分类标签背景 |
| `button_blue` | `#1976D2` | `#90CAF9` | 功能入口蓝色 |
| `button_orange` | `#F57C00` | `#FFB74D` | 功能入口橙色 |
| `button_purple` | `#7B1FA2` | `#CE93D8` | 功能入口紫色 |

所有布局中的硬编码颜色引用改为 `@color/xxx`。drawable 中的 `category_tag_bg.xml` 和 `search_bg.xml` 也改为引用颜色资源。

### 3.2 无障碍改进

- 所有 ImageView 添加 `android:contentDescription="@string/xxx"`
- 需要新增的字符串资源：`plant_image_desc`、`inheritor_image_desc`、`home_icon_plants_desc` 等
- 确保文本与背景对比度符合 WCAG AA（4.5:1），暗色模式下的颜色值需验证对比度

### 3.3 RecyclerView 优化

- 移除 item 布局中的 `android:layout_margin`，改为在 Fragment 中使用 `DividerItemDecoration` 或自定义 `SpaceItemDecoration`
- ViewHolder 内部类统一改为 `static`

### 3.4 适配器补充

- `QaAdapter` 补充 `OnItemClickListener` 接口
- 创建专用 placeholder drawable（`ic_placeholder.xml`），替代所有 `ic_launcher_foreground` 用作 Glide 占位图
- `android:tint` 改为 `app:tint`

---

## 阶段 4：单元测试

### 4.1 测试依赖

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.11.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("com.google.dagger:hilt-android-testing:2.51.1")
testAnnotationProcessor("com.google.dagger:hilt-android-compiler:2.51.1")
testImplementation("org.robolectric:robolectric:4.12")
```

### 4.2 测试范围

| 测试文件 | 测试内容 |
|---------|---------|
| `DongMedicineRepositoryTest.java` | API 调用成功/失败时 Resource 状态正确；Room 缓存写入和读取；网络失败时返回缓存数据 |
| `PlantsViewModelTest.java` | 加载数据后 LiveData 状态流转 SUCCESS/LOADING/ERROR；筛选逻辑正确；刷新不丢失观察者 |
| `InheritorsViewModelTest.java` | 同上模式 |
| `KnowledgeViewModelTest.java` | 同上模式 |
| `PlantDetailViewModelTest.java` | 按 ID 加载正确数据；ID 无效时返回错误状态 |
| `HomeViewModelTest.java` | 统计数据加载正确 |

### 4.3 测试基础设施

- 使用 `InstantTaskExecutorRule` 同步 LiveData
- Mock Repository 接口（如果抽取了接口）或直接 Mock `DongMedicineRepository`
- 使用 `@HiltAndroidTest` 进行 Hilt 集成测试
- 不测试：Fragment UI（Espresso）、Adapter、QaViewModel 的关键词匹配逻辑

### 4.4 测试文件结构

```
app/src/test/java/com/dongmedicine/
  data/repository/DongMedicineRepositoryTest.java
  ui/plants/PlantsViewModelTest.java
  ui/inheritors/InheritorsViewModelTest.java
  ui/knowledge/KnowledgeViewModelTest.java
  ui/plants/PlantDetailViewModelTest.java
  ui/home/HomeViewModelTest.java
```

---

## 不在范围内

以下内容本次重构不涉及：
- Kotlin 迁移
- Espresso UI 测试
- 后端 API 改造
- 新增功能模块
- QaViewModel 关键词匹配逻辑重构（保持现状）
- ProGuard/R8 混淆配置
