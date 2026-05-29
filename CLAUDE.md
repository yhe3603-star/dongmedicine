# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dong Medicine (dongmedicine) is an Android app for digitizing Dong ethnic minority traditional medicine heritage. It displays medicinal plants, intangible cultural heritage inheritors, knowledge articles, and a Q&A community. The app consumes a Spring Boot backend API (default `http://10.0.2.2:8080/`).

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug

# Unit tests
./gradlew test

# Single test class
./gradlew test --tests "com.dongmedicine.ui.plants.PlantsViewModelTest"

# Instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

**Single-activity + Fragment** pattern using Navigation Component. `MainActivity` hosts a `NavHostFragment`; all screens are Fragments navigating via `nav_graph.xml` Safe Args.

**MVVM with Hilt DI stack:**
- `DongmedicineApplication` annotated `@HiltAndroidApp` — Hilt entry point
- `ui/` Fragments observe `@HiltViewModel` ViewModels via LiveData
- ViewModels receive `DongMedicineRepository` via constructor `@Inject`
- Repository is `@Singleton`, injects `ApiService` + Room DAOs
- `Resource<T>` wrapper (SUCCESS/ERROR/LOADING) for UI consumption

**Dependency injection** (`di/`):
- `NetworkModule` — provides `OkHttpClient`, `Retrofit`, `ApiService` as singletons
- `DatabaseModule` — provides `DongMedicineDatabase` and individual DAOs (`PlantDao`, `InheritorDao`, `KnowledgeDao`)

**Data layer:**
- **Remote:** `ApiService` (Retrofit interface) defines all endpoints. `ApiResponse<T>` is an inner class of `ApiService` with `{code, message, data}`.
- **Local:** Room database `DongMedicineDatabase` with entities `Plant`, `Inheritor`, `KnowledgeItem`. Repository writes API responses to Room via an `ExecutorService`.

**Adapters** (`adapters/`): `PlantAdapter`, `InheritorAdapter`, `KnowledgeAdapter`, `QaAdapter` — standard RecyclerView ViewHolder pattern with ViewBinding.

**UI Design System** (Organic Biophilic):
- Nature-inspired palette: forest green primary (`#3A7D44`), terracotta accent (`#D4A574`), warm off-white background (`#F7F4EF`)
- Gradient AppBar backgrounds (`bg_gradient_primary.xml`), organic card shapes (16-24dp corners)
- CollapsingToolbarLayout with parallax hero images on plant/inheritor detail screens
- RecyclerView entrance animations via `AnimationUtils.runLayoutAnimation()`
- Semantic color tokens: `on_surface`, `on_surface_variant`, `outline_variant` (with night variants)

**Animation utilities** (`utils/AnimationUtils.java`): Reusable animation methods — `animateCardPress()`, `animateCount()`, `fadeInUp()`, `runLayoutAnimation()`, `expandTextView()`.

## Package Structure

```
com.dongmedicine/
  DongmedicineApplication.java  — @HiltAndroidApp
  MainActivity.java
  adapters/          — RecyclerView adapters
  data/api/          — ApiService (Retrofit interface + ApiResponse inner class)
  data/local/        — DongMedicineDatabase, PlantDao, InheritorDao, KnowledgeDao
  data/model/        — Plant, Inheritor, KnowledgeItem POJOs (Room @Entity)
  data/repository/   — DongMedicineRepository (@Singleton), Resource<T> wrapper
  di/                — NetworkModule, DatabaseModule (Hilt modules)
  ui/home/           — HomeFragment, HomeViewModel
  ui/plants/         — PlantsFragment, PlantDetailFragment + ViewModels
  ui/inheritors/     — InheritorsFragment, InheritorDetailFragment + ViewModels
  ui/knowledge/      — KnowledgeFragment, KnowledgeDetailFragment + ViewModels
  ui/qa/             — QaFragment, QaViewModel
  utils/             — AnimationUtils (entrance/press/count animations)
```

## Key Technical Details

- **Language:** Java 11 (not Kotlin), compileSdk/targetSdk 35, minSdk 28
- **AGP:** 8.7.2, Gradle 8.9
- **DI:** Hilt 2.51.1 — `@HiltAndroidApp`, `@HiltViewModel`, `@Module` + `@InstallIn`
- **ViewBinding** enabled — use `FragmentXxxBinding.inflate()`, not `findViewById`
- **Networking:** Retrofit 2.9 + OkHttp 4.12, Gson converter, logging interceptor on BODY level in debug only
- **Room:** 2.6.1 — entities with DAOs, repository caches API results to local DB
- **Images:** Glide 4.16 with annotation processor, RoundedCorners transform for list items
- **Charts:** MPAndroidChart 3.1 for HomeFragment visualizations
- **Design tokens:** `res/values/dimens.xml` (spacing, radii, elevations), `res/values/styles.xml` (card, text, chip styles)
- **Drawables:** Gradient backgrounds (`bg_gradient_primary.xml`), organic shapes (`bg_card_organic.xml`), themed vector icons (`ic_nav_*.xml`, `ic_stat_*.xml`)
- **Detail screens:** `CollapsingToolbarLayout` with parallax hero images and gradient scrim overlays
- **Dependencies:** managed via `gradle/libs.versions.toml` version catalog
- **Navigation:** Safe Args for passing IDs between fragments (plantId, inheritorId, knowledgeId)
- **Testing:** JUnit 4 + Mockito + Hilt testing + AndroidX core-testing (LiveData testing)

## Backend API Structure

All endpoints return `ApiService.ApiResponse<T>` with `{code, message, data}` where success is `code == 200`.

```
GET /api/plants/list[?category=...]   → ApiResponse<List<Plant>>
GET /api/plants/{id}                  → ApiResponse<Plant>
GET /api/inheritors/list              → ApiResponse<List<Inheritor>>
GET /api/inheritors/{id}              → ApiResponse<Inheritor>
GET /api/knowledge/list[?category=...] → ApiResponse<List<KnowledgeItem>>
GET /api/knowledge/{id}               → ApiResponse<KnowledgeItem>
```

BASE_URL is in `di/NetworkModule.java`. Emulator host mapping: `10.0.2.2` maps to localhost.

## Adding a New Feature Module

1. Create Fragment + `@HiltViewModel` in `ui/<feature>/`
2. Add Retrofit endpoint(s) to `ApiService`
3. Add Room entity + DAO if new data model, register in `DongMedicineDatabase`
4. Add repository methods in `DongMedicineRepository` (API call + Room cache)
5. Add fragment destination + Safe Args in `nav_graph.xml`
6. Create adapter if displaying a list
