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
./gradlew test --tests "com.dongmedicine.ExampleUnitTest"

# Instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

**Single-activity + Fragment** pattern using Navigation Component. `MainActivity` hosts a `NavHostFragment`; all screens are Fragments navigating via `nav_graph.xml` Safe Args.

**MVVM stack:**
- `ui/` Fragments observe ViewModel LiveData
- ViewModels call into `DongMedicineRepository`
- Repository wraps API responses in `Resource<T>` (SUCCESS/ERROR/LOADING)
- `ApiService` (Retrofit interface) defines all endpoints; `ApiClient` is the singleton Retrofit builder

**Key pattern — API response wrapping:** All backend responses are `ApiResponse<T>` with `{code, message, data}`. The repository converts these to `Resource<T>` for the UI layer.

**Data models** (`data/model/`): `Plant`, `Inheritor`, `KnowledgeItem` — POJOs matching Spring Boot entity JSON.

**Adapters** (`adapters/`): `PlantAdapter`, `InheritorAdapter`, `KnowledgeAdapter`, `QaAdapter` — standard RecyclerView ViewHolder pattern with ViewBinding.

## Package Structure

```
com.dongmedicine/
  MainActivity.java
  adapters/          — RecyclerView adapters
  data/api/          — ApiClient (Retrofit singleton), ApiService (endpoint interface)
  data/model/        — Plant, Inheritor, KnowledgeItem POJOs
  data/repository/   — DongMedicineRepository, Resource<T> wrapper
  ui/home/           — HomeFragment, HomeViewModel (stats dashboard)
  ui/plants/         — PlantsFragment, PlantDetailFragment + ViewModels
  ui/inheritors/     — InheritorsFragment, InheritorDetailFragment + ViewModel
  ui/knowledge/      — KnowledgeFragment, KnowledgeDetailFragment + ViewModel
  ui/qa/             — QaFragment, QaViewModel (Q&A community)
```

## Key Technical Details

- **Language:** Java 11 (not Kotlin), compileSdk/targetSdk 35, minSdk 28
- **AGP:** 8.7.2, Gradle 8.9
- **ViewBinding** enabled in `build.gradle.kts` — use `FragmentXxxBinding.inflate()` pattern, not `findViewById`
- **Networking:** Retrofit 2.9 + OkHttp 4.12, Gson converter, logging interceptor always on (BODY level)
- **Images:** Glide 4.16 with annotation processor
- **Charts:** MPAndroidChart 3.1 for HomeFragment visualizations
- **Room:** declared but minimal usage (reserved for offline caching)
- **Dependencies:** managed via `gradle/libs.versions.toml` version catalog
- **Navigation:** Safe Args for passing IDs between fragments (e.g., plantId, inheritorId, knowledgeId)

## Backend API Structure

```
GET /api/plants/list[?category=...]   → ApiResponse<List<Plant>>
GET /api/plants/{id}                  → ApiResponse<Plant>
GET /api/inheritors/list              → ApiResponse<List<Inheritor>>
GET /api/inheritors/{id}              → ApiResponse<Inheritor>
GET /api/knowledge/list[?category=...] → ApiResponse<List<KnowledgeItem>>
GET /api/knowledge/{id}               → ApiResponse<KnowledgeItem>
```

Emulator host mapping: `10.0.2.2` maps to localhost on the development machine.
