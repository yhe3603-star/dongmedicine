# 侗乡医药 Android 数字平台

> 基于 Android 的侗族医药文化遗产数字化展示移动应用

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [API 配置](#api-配置)
- [数据模型](#数据模型)

---

## 项目简介

本项目是一个毕业设计项目，旨在通过移动端应用保护和传承侗族医药文化遗产。Android 应用提供了药用植物展示、非遗传承人介绍、知识库、互动问答等功能模块，让用户能够随时随地了解侗族传统医药文化。

### 核心价值

- **文化传承**：数字化保存侗族传统医药知识
- **知识普及**：让更多人了解侗族医药文化
- **移动优先**：随时随地访问医药知识
- **智能问答**：解答侗医药相关问题

---

## 功能特性

### 功能模块

| 模块 | 功能 | 说明 |
|------|------|------|
| **首页** | 数据概览 | 展示平台统计数据、快速导航入口 |
| **药用植物** | 植物图鉴 | 展示侗族传统药用植物，支持分类筛选、搜索 |
| **传承人** | 传承人档案 | 展示各级非遗传承人信息 |
| **知识库** | 文献资料 | 侗医药知识条目，支持分类浏览 |
| **问答社区** | 知识问答 | 用户提问、智能回答 |

### 功能详情

- **首页**：统计数据展示（植物数量、传承人数量、知识条目数、用户数）、快捷入口卡片
- **药用植物列表**：下拉刷新、搜索功能、分类筛选（清热解毒、补益类、活血化瘀等）
- **植物详情**：植物图片、学名、功效描述、分布区域
- **传承人列表**：下拉刷新、级别筛选（国家级、省级、市级等）
- **传承人详情**：个人简介、擅长领域
- **知识库列表**：分类浏览、列表展示
- **知识详情**：文章内容展示
- **互动问答**：问题提交、智能回答、常见问题展示

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────┐
│              Android 客户端                      │
├─────────────────────────────────────────────────┤
│  UI Layer (Activity/Fragment + ViewBinding)      │
├─────────────────────────────────────────────────┤
│  ViewModel Layer (LiveData)                     │
├─────────────────────────────────────────────────┤
│  Repository Layer (Data Repository)             │
├─────────────────────────────────────────────────┤
│  Data Layer (Retrofit + OkHttp + Gson)           │
└─────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────┐
│              Spring Boot 后端 API                 │
│              (http://localhost:8080)            │
└─────────────────────────────────────────────────┘
```

### 技术栈

| 技术 | 说明 |
|------|------|
| Java 11 | 开发语言 |
| Android SDK 34 | Android 平台 |
| Android Jetpack | Android 开发组件套件 |
| Navigation Component | 导航组件 |
| ViewModel + LiveData | MVVM 架构 |
| Retrofit 2.9 | REST API 调用 |
| OkHttp 4.12 | HTTP 客户端 |
| Glide 4.16 | 图片加载 |
| Room | 本地数据库（预留） |
| Material Design 3 | UI 设计规范 |

### 开发环境

| 环境 | 版本要求 |
|------|----------|
| JDK | 11+ |
| Android Studio | 2024.0+ |
| Android Gradle Plugin | 8.7.2 |
| Gradle | 8.7+ |

---

## 项目结构

```
app/
├── src/main/
│   ├── java/com/dongmedicine/
│   │   ├── MainActivity.java           # 主入口
│   │   ├── adapters/                   # RecyclerView 适配器
│   │   │   ├── PlantAdapter.java       # 植物列表适配器
│   │   │   ├── InheritorAdapter.java  # 传承人适配器
│   │   │   ├── KnowledgeAdapter.java   # 知识库适配器
│   │   │   └── QaAdapter.java          # 问答适配器
│   │   ├── data/
│   │   │   ├── api/                    # 网络请求
│   │   │   │   ├── ApiService.java     # Retrofit 接口
│   │   │   │   └── ApiClient.java      # 网络客户端
│   │   │   ├── model/                  # 数据模型
│   │   │   │   ├── Plant.java          # 植物实体
│   │   │   │   ├── Inheritor.java      # 传承人实体
│   │   │   │   └── KnowledgeItem.java  # 知识条目实体
│   │   │   └── repository/             # 数据仓库
│   │   │       ├── DongMedicineRepository.java
│   │   │       └── Resource.java       # 统一响应封装
│   │   └── ui/                         # 界面层
│   │       ├── home/                   # 首页模块
│   │       ├── plants/                 # 植物模块
│   │       ├── inheritors/             # 传承人模块
│   │       ├── knowledge/              # 知识库模块
│   │       └── qa/                     # 问答模块
│   │
│   ├── res/
│   │   ├── layout/                     # 布局文件
│   │   ├── navigation/                # 导航图
│   │   ├── values/                    # 资源文件
│   │   └── drawable/                  # 图片资源
│   │
│   └── AndroidManifest.xml             # 应用清单
│
├── build.gradle.kts                    # 构建配置
└── README.md                           # 本文档
```

---

## 快速开始

### 1. 环境配置

确保已安装以下开发工具：

```bash
# 检查 Java 版本
java -version

# 检查 Gradle 版本
gradle -v
```

### 2. 克隆项目

```bash
git clone <项目仓库地址>
cd dongmedicine
```

### 3. 配置 Android SDK

在 Android Studio 中：
1. File → Project Structure → SDK Location
2. 配置 Android SDK 路径

### 4. 同步项目

```bash
# 在 Android Studio 中点击 Sync Now
# 或命令行同步
./gradlew dependencies
```

### 5. 运行项目

1. 连接 Android 设备或启动模拟器
2. 在 Android Studio 中点击 Run → Run 'app'
3. 或使用命令行：

```bash
./gradlew assembleDebug
```

### 6. 安装 APK

```bash
# 安装调试 APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## API 配置

### 后端服务配置

应用默认连接本地后端服务。如需修改后端地址，编辑文件：

**文件**: `app/src/main/java/com/dongmedicine/data/api/ApiClient.java`

```java
private static String BASE_URL = "http://10.0.2.2:8080/";
```

> 注意：Android 模拟器访问宿主机使用 `10.0.2.2`

### 模拟器网络配置

```bash
# 模拟器访问 localhost
http://10.0.2.2:8080

# 物理设备需要确保在同一网络
```

---

## 数据模型

### Plant (药用植物)

```java
- id: int              // 植物ID
- name: String         // 中文名称
- scientificName: String // 学名
- description: String  // 描述
- imageUrl: String      // 图片URL
- effects: String       // 功效
- distribution: String  // 分布区域
- category: String      // 分类
- nameDong: String      // 侗语名称
```

### Inheritor (传承人)

```java
- id: int              // 传承人ID
- name: String         // 姓名
- title: String        // 级别（国家级/省级/市级）
- specialization: String // 擅长领域
- introduction: String  // 个人简介
- imageUrl: String      // 头像URL
```

### KnowledgeItem (知识条目)

```java
- id: int              // 知识ID
- title: String        // 标题
- content: String       // 内容
- category: String      // 分类
- publishDate: String   // 发布日期
- author: String       // 作者
```

---

## 页面预览

| 首页 | 植物列表 | 植物详情 |
|------|----------|----------|
| 统计数据展示 | 搜索+筛选 | 完整信息 |
| 快捷入口卡片 | 下拉刷新 | 图片展示 |

| 传承人列表 | 知识库 | 问答 |
|-----------|--------|------|
| 级别筛选 | 分类浏览 | 智能问答 |
| 详情跳转 | 内容展示 | 常见问题 |

---

## 常见问题

### 1. 编译失败

```bash
# 清除缓存重新构建
./gradlew clean
./gradlew assembleDebug
```

### 2. 网络请求失败

- 检查后端服务是否启动
- 检查 BASE_URL 配置是否正确
- 检查 AndroidManifest.xml 是否添加 INTERNET 权限

### 3. 图片加载失败

- 检查网络连接
- Glide 会显示占位图

### 4. 导航点击无反应

- 检查 nav_graph.xml 中的 action 配置
- 确保 fragment 类名正确

---

## 开发规范

- **架构模式**: MVVM (Model-View-ViewModel)
- **包名规范**: com.dongmedicine.*
- **资源命名**: 小写字母 + 下划线
- **代码风格**: 遵循 Google Java Style Guide

---

## 许可证

本项目仅供学习和研究使用。
