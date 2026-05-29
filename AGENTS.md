# AGENTS.md - Dong Medicine Android App

## Overview
This is an Android application for the Dong Medicine Digital Platform, providing mobile access to traditional Dong medicine knowledge, plants, inheritors, and interactive features. Built with modern Android architecture components.

## Architecture
- **Navigation**: Single-activity architecture using Navigation Component with Fragment-based UI
- **Data Layer**: Retrofit for API calls, Room for local caching, OkHttp for networking
- **UI Layer**: MVVM with ViewModel/LiveData, RecyclerView for lists, Glide for image loading
- **Visualization**: MPAndroidChart for data charts in the visualization fragment

## Key Components
- **MainActivity**: Entry point with NavHostFragment setup
- **Fragments**: 10+ feature fragments defined in `res/navigation/nav_graph.xml` (e.g., HomeFragment, PlantListFragment, QAFragment)
- **Data Models**: API entities for plants, inheritors, knowledge articles
- **ViewModels**: Business logic separation for each feature screen

## Developer Workflows
- **Build**: `./gradlew assembleDebug` or `./gradlew build`
- **Test**: `./gradlew test` for unit tests, `./gradlew connectedAndroidTest` for instrumentation
- **Debug**: Use Android Studio debugger; logs via `android.util.Log`
- **Dependencies**: Managed via `gradle/libs.versions.toml` version catalog

## Conventions
- **Language**: Java 11 with Android Gradle Plugin 8.7.2
- **Package**: `com.example.dongmedicine` with feature-based subpackages (ui.home, ui.plants, etc.)
- **Layouts**: ConstraintLayout primary, ViewBinding preferred over findViewById
- **Networking**: Retrofit with Gson converter, OkHttp logging interceptor for debug builds
- **Database**: Room entities with DAOs, use KTX extensions for coroutines
- **Navigation**: Safe Args for fragment arguments (e.g., plantId as integer)

## Examples
- **Fragment Creation**: Extend `Fragment`, inflate layout, observe ViewModel LiveData
- **API Call**: Use Retrofit service interface with suspend functions for coroutines
- **RecyclerView**: Adapter with ViewHolder pattern, DiffUtil for efficient updates
- **Navigation**: `findNavController().navigate(R.id.action_home_to_plantList)`

Reference: `app/src/main/res/navigation/nav_graph.xml` for complete screen flow</content>
<parameter name="filePath">D:\AndroidTool\AndroidProject\dongmedicine\AGENTS.md
