# 清理管家 (CleanMaster) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android storage cleanup and duplicate file analysis app with screenshot analysis, trash protection, ad monetization, and multi-language support.

**Architecture:** Multi-module Android app (app, core, scanner, duplicates, cleaner, trash) using Kotlin + Jetpack Compose + Material 3. Each module has a single responsibility and communicates through well-defined interfaces.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Coroutines, Flow, Room, Hilt, AdMob + CSJ Mediation

---

## File Structure Overview

```
CleanMaster/
├── build.gradle.kts                          # Root build file
├── settings.gradle.kts                       # Module declarations
├── gradle.properties
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/cleanmaster/
│       │   ├── CleanMasterApp.kt             # Application class
│       │   ├── MainActivity.kt
│       │   ├── navigation/
│       │   │   └── AppNavigation.kt
│       │   ├── ui/
│       │   │   ├── home/
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   └── HomeViewModel.kt
│       │   │   ├── screenshot/
│       │   │   │   ├── ScreenshotScreen.kt
│       │   │   │   └── ScreenshotViewModel.kt
│       │   │   ├── trash/
│       │   │   │   ├── TrashScreen.kt
│       │   │   │   └── TrashViewModel.kt
│       │   │   ├── settings/
│       │   │   │   ├── SettingsScreen.kt
│       │   │   │   └── SettingsViewModel.kt
│       │   │   └── theme/
│       │   │       ├── Theme.kt
│       │   │       ├── Color.kt
│       │   │       └── Type.kt
│       │   ├── ad/
│       │   │   ├── AdManager.kt
│       │   │   ├── AdMobProvider.kt
│       │   │   └── CSJProvider.kt
│       │   └── di/
│       │       └── AppModule.kt
│       └── res/
│           ├── values/strings.xml            # English
│           └── values-zh/strings.xml         # Chinese
├── core/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/cleanmaster/core/
│       │   ├── model/
│       │   │   ├── FileInfo.kt
│       │   │   ├── DuplicateGroup.kt
│       │   │   ├── ScanResult.kt
│       │   │   ├── TrashItem.kt
│       │   │   ├── ScreenshotInfo.kt
│       │   │   └── CleanableItem.kt
│       │   ├── util/
│       │   │   ├── HashUtil.kt
│       │   │   ├── FileSizeFormatter.kt
│       │   │   └── PermissionManager.kt
│       │   └── ui/
│       │       ├── components/
│       │       │   ├── StorageCard.kt
│       │       │   ├── CategoryCard.kt
│       │       │   └── ProgressRing.kt
│       │       └── theme/
│       │           └── CleanMasterTheme.kt
│       └── test/java/com/cleanmaster/core/
│           ├── util/HashUtilTest.kt
│           └── util/FileSizeFormatterTest.kt
├── scanner/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/cleanmaster/scanner/
│       │   ├── Scanner.kt                    # Interface
│       │   ├── ScanProgress.kt
│       │   ├── FileScanner.kt
│       │   └── MediaScanner.kt
│       └── test/java/com/cleanmaster/scanner/
│           ├── FileScannerTest.kt
│           └── MediaScannerTest.kt
├── duplicates/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/cleanmaster/duplicates/
│       │   ├── QuickFilter.kt
│       │   └── HashVerifier.kt
│       └── test/java/com/cleanmaster/duplicates/
│           ├── QuickFilterTest.kt
│           └── HashVerifierTest.kt
├── cleaner/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/cleanmaster/cleaner/
│       │   ├── Cleaner.kt                    # Interface
│       │   ├── CacheCleaner.kt
│       │   ├── JunkCleaner.kt
│       │   └── LargeFileFinder.kt
│       └── test/java/com/cleanmaster/cleaner/
│           ├── CacheCleanerTest.kt
│           └── LargeFileFinderTest.kt
└── trash/
    ├── build.gradle.kts
    └── src/
        ├── main/java/com/cleanmaster/trash/
        │   ├── TrashManager.kt
        │   ├── TrashDao.kt
        │   ├── TrashDatabase.kt
        │   └── TrashConfig.kt
        └── test/java/com/cleanmaster/trash/
            └── TrashManagerTest.kt
```

---

## Task 1: Project Setup — Gradle Multi-Module

**Files:**
- Create: `build.gradle.kts` (root)
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `core/build.gradle.kts`
- Create: `scanner/build.gradle.kts`
- Create: `duplicates/build.gradle.kts`
- Create: `cleaner/build.gradle.kts`
- Create: `trash/build.gradle.kts`
- Create: `gradle/libs.versions.toml`

- [ ] **Step 1: Create version catalog**

```toml
# gradle/libs.versions.toml
[versions]
agp = "8.5.0"
kotlin = "2.0.0"
compose-bom = "2024.06.00"
hilt = "2.51.1"
room = "2.6.1"
coroutines = "1.8.1"
junit = "4.13.2"
mockk = "1.13.11"
admob = "23.2.0"
csj = "6.4.0.5"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
admob = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "admob" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.0-1.0.22" }
```

- [ ] **Step 2: Create root build.gradle.kts**

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 3: Create settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CleanMaster"
include(":app")
include(":core")
include(":scanner")
include(":duplicates")
include(":cleaner")
include(":trash")
```

- [ ] **Step 4: Create gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 5: Create core/build.gradle.kts**

```kotlin
// core/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cleanmaster.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.compose.bom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 6: Create scanner/build.gradle.kts**

```kotlin
// scanner/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cleanmaster.scanner"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 7: Create duplicates/build.gradle.kts**

```kotlin
// duplicates/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cleanmaster.duplicates"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 8: Create cleaner/build.gradle.kts**

```kotlin
// cleaner/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cleanmaster.cleaner"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":scanner"))
    implementation(libs.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 9: Create trash/build.gradle.kts**

```kotlin
// trash/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cleanmaster.trash"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.coroutines.core)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 10: Create app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.cleanmaster"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cleanmaster"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":scanner"))
    implementation(project(":duplicates"))
    implementation(project(":cleaner"))
    implementation(project(":trash"))

    implementation(libs.compose.bom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.coroutines.android)

    implementation(libs.admob)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
```

- [ ] **Step 11: Verify project syncs**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew tasks`
Expected: BUILD SUCCESSFUL

- [ ] **Step 12: Commit**

```bash
git add .
git commit -m "feat: initialize multi-module Gradle project structure"
```

---

## Task 2: Core Data Models

**Files:**
- Create: `core/src/main/java/com/cleanmaster/core/model/FileInfo.kt`
- Create: `core/src/main/java/com/cleanmaster/core/model/DuplicateGroup.kt`
- Create: `core/src/main/java/com/cleanmaster/core/model/ScanResult.kt`
- Create: `core/src/main/java/com/cleanmaster/core/model/TrashItem.kt`
- Create: `core/src/main/java/com/cleanmaster/core/model/ScreenshotInfo.kt`
- Create: `core/src/main/java/com/cleanmaster/core/model/CleanableItem.kt`

- [ ] **Step 1: Create FileInfo**

```kotlin
// core/src/main/java/com/cleanmaster/core/model/FileInfo.kt
package com.cleanmaster.core.model

data class FileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val mimeType: String,
    val hash: String? = null
)
```

- [ ] **Step 2: Create FileType enum and DuplicateGroup**

```kotlin
// core/src/main/java/com/cleanmaster/core/model/DuplicateGroup.kt
package com.cleanmaster.core.model

enum class FileType {
    CACHE, JUNK, LARGE_FILE, DUPLICATE, MEDIA, EMPTY_FOLDER
}

data class DuplicateGroup(
    val id: String,
    val files: List<FileInfo>,
    val totalSize: Long,
    val fileHash: String
)
```

- [ ] **Step 3: Create ScanResult**

```kotlin
// core/src/main/java/com/cleanmaster/core/model/ScanResult.kt
package com.cleanmaster.core.model

data class ScanResult(
    val cacheFiles: List<FileInfo>,
    val junkFiles: List<FileInfo>,
    val largeFiles: List<FileInfo>,
    val emptyFolders: List<String>,
    val duplicateGroups: List<DuplicateGroup>,
    val mediaFiles: List<FileInfo>,
    val totalSize: Long
)
```

- [ ] **Step 4: Create TrashItem**

```kotlin
// core/src/main/java/com/cleanmaster/core/model/TrashItem.kt
package com.cleanmaster.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_items")
data class TrashItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val size: Long,
    val deletedAt: Long,
    val fileType: FileType
)
```

- [ ] **Step 5: Create ScreenshotInfo**

```kotlin
// core/src/main/java/com/cleanmaster/core/model/ScreenshotInfo.kt
package com.cleanmaster.core.model

data class ScreenshotInfo(
    val fileInfo: FileInfo,
    val width: Int,
    val height: Int,
    val phash: String? = null,
    val blurScore: Float? = null,
    val isBlackScreen: Boolean = false,
    val isSolidColor: Boolean = false
)

data class SimilarGroup(
    val screenshots: List<ScreenshotInfo>,
    val similarity: Float
)

data class ExpiredScreenshot(
    val screenshot: ScreenshotInfo,
    val ageInDays: Int
)

data class ScreenshotAnalysisResult(
    val duplicates: List<DuplicateGroup>,
    val similar: List<SimilarGroup>,
    val lowQuality: List<ScreenshotInfo>,
    val expired: List<ExpiredScreenshot>,
    val totalCount: Int,
    val recoverableSize: Long
)
```

- [ ] **Step 6: Create CleanableItem**

```kotlin
// core/src/main/java/com/cleanmaster/core/model/CleanableItem.kt
package com.cleanmaster.core.model

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

enum class CleanType {
    CACHE, JUNK, LARGE_FILE, EMPTY_FOLDER, MEDIA, SCREENSHOT
}

data class CleanableItem(
    val fileInfo: FileInfo,
    val type: CleanType,
    val riskLevel: RiskLevel
)
```

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/com/cleanmaster/core/model/
git commit -m "feat(core): add data models"
```

---

## Task 3: Core Utilities

**Files:**
- Create: `core/src/main/java/com/cleanmaster/core/util/HashUtil.kt`
- Create: `core/src/main/java/com/cleanmaster/core/util/FileSizeFormatter.kt`
- Create: `core/src/main/java/com/cleanmaster/core/util/PermissionManager.kt`
- Create: `core/src/test/java/com/cleanmaster/core/util/HashUtilTest.kt`
- Create: `core/src/test/java/com/cleanmaster/core/util/FileSizeFormatterTest.kt`

- [ ] **Step 1: Write HashUtil test**

```kotlin
// core/src/test/java/com/cleanmaster/core/util/HashUtilTest.kt
package com.cleanmaster.core.util

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HashUtilTest {

    @Test
    fun `should calculate consistent hash for same file`() {
        val file = File.createTempFile("test", ".txt")
        file.writeText("hello world")
        val hash1 = HashUtil.calculateSha256(file)
        val hash2 = HashUtil.calculateSha256(file)
        assertEquals(hash1, hash2)
        file.delete()
    }

    @Test
    fun `should calculate different hash for different content`() {
        val file1 = File.createTempFile("test1", ".txt")
        val file2 = File.createTempFile("test2", ".txt")
        file1.writeText("hello")
        file2.writeText("world")
        assertNotEquals(HashUtil.calculateSha256(file1), HashUtil.calculateSha256(file2))
        file1.delete()
        file2.delete()
    }

    @Test
    fun `should return 64 character hex string`() {
        val file = File.createTempFile("test", ".txt")
        file.writeText("test content")
        val hash = HashUtil.calculateSha256(file)
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]+")))
        file.delete()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :core:test --tests "com.cleanmaster.core.util.HashUtilTest"`
Expected: FAIL with "HashUtil not found"

- [ ] **Step 3: Implement HashUtil**

```kotlin
// core/src/main/java/com/cleanmaster/core/util/HashUtil.kt
package com.cleanmaster.core.util

import java.io.File
import java.security.MessageDigest

object HashUtil {

    fun calculateSha256(file: File): String {
        val buffer = ByteArray(8192)
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { stream ->
            var bytes = stream.read(buffer)
            while (bytes != -1) {
                digest.update(buffer, 0, bytes)
                bytes = stream.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :core:test --tests "com.cleanmaster.core.util.HashUtilTest"`
Expected: 3 tests PASSED

- [ ] **Step 5: Write FileSizeFormatter test**

```kotlin
// core/src/test/java/com/cleanmaster/core/util/FileSizeFormatterTest.kt
package com.cleanmaster.core.util

import org.junit.Assert.*
import org.junit.Test

class FileSizeFormatterTest {

    @Test
    fun `should format bytes correctly`() {
        assertEquals("500 B", FileSizeFormatter.format(500))
    }

    @Test
    fun `should format kilobytes correctly`() {
        assertEquals("1.5 KB", FileSizeFormatter.format(1536))
    }

    @Test
    fun `should format megabytes correctly`() {
        assertEquals("2.3 MB", FileSizeFormatter.format(2411724))
    }

    @Test
    fun `should format gigabytes correctly`() {
        assertEquals("1.2 GB", FileSizeFormatter.format(1288490188))
    }

    @Test
    fun `should handle zero`() {
        assertEquals("0 B", FileSizeFormatter.format(0))
    }
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :core:test --tests "com.cleanmaster.core.util.FileSizeFormatterTest"`
Expected: FAIL with "FileSizeFormatter not found"

- [ ] **Step 7: Implement FileSizeFormatter**

```kotlin
// core/src/main/java/com/cleanmaster/core/util/FileSizeFormatter.kt
package com.cleanmaster.core.util

object FileSizeFormatter {

    fun format(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        val gb = mb / 1024.0
        return "%.1f GB".format(gb)
    }
}
```

- [ ] **Step 8: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :core:test --tests "com.cleanmaster.core.util.FileSizeFormatterTest"`
Expected: 5 tests PASSED

- [ ] **Step 9: Implement PermissionManager**

```kotlin
// core/src/main/java/com/cleanmaster/core/util/PermissionManager.kt
package com.cleanmaster.core.util

import android.Manifest
import android.os.Build

object PermissionManager {

    fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
}
```

- [ ] **Step 10: Commit**

```bash
git add core/src/main/java/com/cleanmaster/core/util/ core/src/test/
git commit -m "feat(core): add HashUtil, FileSizeFormatter, PermissionManager"
```

---

## Task 4: Scanner Module — Interface & FileScanner

**Files:**
- Create: `scanner/src/main/java/com/cleanmaster/scanner/Scanner.kt`
- Create: `scanner/src/main/java/com/cleanmaster/scanner/ScanProgress.kt`
- Create: `scanner/src/main/java/com/cleanmaster/scanner/FileScanner.kt`
- Create: `scanner/src/test/java/com/cleanmaster/scanner/FileScannerTest.kt`

- [ ] **Step 1: Create Scanner interface and ScanProgress**

```kotlin
// scanner/src/main/java/com/cleanmaster/scanner/Scanner.kt
package com.cleanmaster.scanner

import kotlinx.coroutines.flow.Flow

interface Scanner<T> {
    suspend fun scan(rootPath: String): Flow<ScanProgress>
    fun getResult(): T
}
```

```kotlin
// scanner/src/main/java/com/cleanmaster/scanner/ScanProgress.kt
package com.cleanmaster.scanner

data class ScanProgress(
    val scannedCount: Int,
    val currentPath: String,
    val foundSize: Long
)
```

- [ ] **Step 2: Write FileScanner test**

```kotlin
// scanner/src/test/java/com/cleanmaster/scanner/FileScannerTest.kt
package com.cleanmaster.scanner

import com.cleanmaster.core.model.FileInfo
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find cache files`() = runBlocking {
        val cacheDir = tempFolder.newFolder("cache")
        File(cacheDir, "test.cache").writeText("cache data")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertTrue(result.cacheFiles.isNotEmpty())
        assertEquals("test.cache", result.cacheFiles[0].name)
    }

    @Test
    fun `should find junk files by extension`() = runBlocking {
        File(tempFolder.root, "temp.tmp").writeText("temp data")
        File(tempFolder.root, "log.log").writeText("log data")
        File(tempFolder.root, "backup.bak").writeText("backup data")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(3, result.junkFiles.size)
    }

    @Test
    fun `should find empty folders`() = runBlocking {
        tempFolder.newFolder("empty1")
        tempFolder.newFolder("empty2")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(2, result.emptyFolders.size)
    }

    @Test
    fun `should not count non-empty folders as empty`() = runBlocking {
        val dir = tempFolder.newFolder("notempty")
        File(dir, "file.txt").writeText("content")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertTrue(result.emptyFolders.none { it.contains("notempty") })
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :scanner:test --tests "com.cleanmaster.scanner.FileScannerTest"`
Expected: FAIL with "FileScanner not found"

- [ ] **Step 4: Implement FileScanner**

```kotlin
// scanner/src/main/java/com/cleanmaster/scanner/FileScanner.kt
package com.cleanmaster.scanner

import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class FileScanner : Scanner<ScanResult> {

    private var result: ScanResult? = null

    private val cacheDirs = listOf("cache", "code_cache")
    private val junkExtensions = setOf(".tmp", ".log", ".bak", ".temp", ".swp")

    override suspend fun scan(rootPath: String): Flow<ScanProgress> = flow {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) {
            result = ScanResult(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0)
            return@flow
        }

        val cacheFiles = mutableListOf<FileInfo>()
        val junkFiles = mutableListOf<FileInfo>()
        val emptyFolders = mutableListOf<String>()
        var scannedCount = 0
        var totalSize = 0L

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++

                when {
                    file.isDirectory && file.listFiles().isNullOrEmpty() && file != root -> {
                        emptyFolders.add(file.absolutePath)
                    }
                    file.isFile -> {
                        val isCache = cacheDirs.any { file.absolutePath.contains(it, ignoreCase = true) }
                        if (isCache) {
                            cacheFiles.add(file.toFileInfo())
                            totalSize += file.length()
                        }

                        val ext = file.extension.lowercase()
                        if (file.parentFile?.name != "cache" && ".$ext" in junkExtensions) {
                            junkFiles.add(file.toFileInfo())
                            totalSize += file.length()
                        }
                    }
                }

                if (scannedCount % 100 == 0) {
                    emit(ScanProgress(scannedCount, file.absolutePath, totalSize))
                }
            }
        }

        result = ScanResult(
            cacheFiles = cacheFiles,
            junkFiles = junkFiles,
            largeFiles = emptyList(),
            emptyFolders = emptyFolders,
            duplicateGroups = emptyList(),
            mediaFiles = emptyList(),
            totalSize = totalSize
        )

        emit(ScanProgress(scannedCount, rootPath, totalSize))
    }

    override fun getResult(): ScanResult = result ?: ScanResult(
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0
    )

    private fun File.toFileInfo() = FileInfo(
        path = absolutePath,
        name = name,
        size = length(),
        lastModified = lastModified(),
        mimeType = extension
    )
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :scanner:test --tests "com.cleanmaster.scanner.FileScannerTest"`
Expected: 4 tests PASSED

- [ ] **Step 6: Commit**

```bash
git add scanner/src/
git commit -m "feat(scanner): add Scanner interface and FileScanner"
```

---

## Task 5: Scanner Module — MediaScanner

**Files:**
- Create: `scanner/src/main/java/com/cleanmaster/scanner/MediaScanner.kt`
- Create: `scanner/src/test/java/com/cleanmaster/scanner/MediaScannerTest.kt`

- [ ] **Step 1: Write MediaScanner test**

```kotlin
// scanner/src/test/java/com/cleanmaster/scanner/MediaScannerTest.kt
package com.cleanmaster.scanner

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MediaScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find image files`() = runBlocking {
        File(tempFolder.root, "photo.jpg").writeBytes(ByteArray(100))
        File(tempFolder.root, "screenshot.png").writeBytes(ByteArray(100))
        File(tempFolder.root, "document.txt").writeText("not media")

        val scanner = MediaScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(2, result.size)
    }

    @Test
    fun `should find video files`() = runBlocking {
        File(tempFolder.root, "video.mp4").writeBytes(ByteArray(100))
        File(tempFolder.root, "clip.mov").writeBytes(ByteArray(100))

        val scanner = MediaScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(2, result.size)
    }

    @Test
    fun `should return empty list for no media`() = runBlocking {
        File(tempFolder.root, "text.txt").writeText("hello")

        val scanner = MediaScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertTrue(result.isEmpty())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :scanner:test --tests "com.cleanmaster.scanner.MediaScannerTest"`
Expected: FAIL with "MediaScanner not found"

- [ ] **Step 3: Implement MediaScanner**

```kotlin
// scanner/src/main/java/com/cleanmaster/scanner/MediaScanner.kt
package com.cleanmaster.scanner

import com.cleanmaster.core.model.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner : Scanner<List<FileInfo>> {

    private var result: List<FileInfo> = emptyList()

    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    private val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp")
    private val allMediaExtensions = imageExtensions + videoExtensions

    override suspend fun scan(rootPath: String): Flow<ScanProgress> = flow {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) {
            result = emptyList()
            return@flow
        }

        val mediaFiles = mutableListOf<FileInfo>()
        var scannedCount = 0

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && file.extension.lowercase() in allMediaExtensions) {
                    mediaFiles.add(
                        FileInfo(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            mimeType = file.extension
                        )
                    )
                }

                if (scannedCount % 100 == 0) {
                    emit(ScanProgress(scannedCount, file.absolutePath, mediaFiles.sumOf { it.size }))
                }
            }
        }

        result = mediaFiles
        emit(ScanProgress(scannedCount, rootPath, mediaFiles.sumOf { it.size }))
    }

    override fun getResult(): List<FileInfo> = result
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :scanner:test --tests "com.cleanmaster.scanner.MediaScannerTest"`
Expected: 3 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add scanner/src/
git commit -m "feat(scanner): add MediaScanner"
```

---

## Task 6: Duplicates Module — QuickFilter

**Files:**
- Create: `duplicates/src/main/java/com/cleanmaster/duplicates/QuickFilter.kt`
- Create: `duplicates/src/test/java/com/cleanmaster/duplicates/QuickFilterTest.kt`

- [ ] **Step 1: Write QuickFilter test**

```kotlin
// duplicates/src/test/java/com/cleanmaster/duplicates/QuickFilterTest.kt
package com.cleanmaster.duplicates

import com.cleanmaster.core.model.FileInfo
import org.junit.Assert.*
import org.junit.Test

class QuickFilterTest {

    @Test
    fun `should group files with same name and size`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/c/photo.jpg", "photo.jpg", 1024, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertEquals(1, groups.size)
        assertEquals(3, groups[0].size)
    }

    @Test
    fun `should not group files with different size`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/photo.jpg", "photo.jpg", 2048, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should not group files with different name`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/image.jpg", "image.jpg", 1024, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should return empty list for single files`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should return multiple groups`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/a/video.mp4", "video.mp4", 2048, 0, "mp4"),
            FileInfo("/b/video.mp4", "video.mp4", 2048, 0, "mp4")
        )

        val groups = QuickFilter.filter(files)
        assertEquals(2, groups.size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.QuickFilterTest"`
Expected: FAIL with "QuickFilter not found"

- [ ] **Step 3: Implement QuickFilter**

```kotlin
// duplicates/src/main/java/com/cleanmaster/duplicates/QuickFilter.kt
package com.cleanmaster.duplicates

import com.cleanmaster.core.model.FileInfo

object QuickFilter {

    /**
     * Groups files by (name, size). Returns only groups with 2+ files.
     */
    fun filter(files: List<FileInfo>): List<List<FileInfo>> {
        return files
            .groupBy { "${it.name}_${it.size}" }
            .values
            .filter { it.size >= 2 }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.QuickFilterTest"`
Expected: 5 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add duplicates/src/
git commit -m "feat(duplicates): add QuickFilter"
```

---

## Task 7: Duplicates Module — HashVerifier

**Files:**
- Create: `duplicates/src/main/java/com/cleanmaster/duplicates/HashVerifier.kt`
- Create: `duplicates/src/test/java/com/cleanmaster/duplicates/HashVerifierTest.kt`

- [ ] **Step 1: Write HashVerifier test**

```kotlin
// duplicates/src/test/java/com/cleanmaster/duplicates/HashVerifierTest.kt
package com.cleanmaster.duplicates

import com.cleanmaster.core.model.DuplicateGroup
import com.cleanmaster.core.model.FileInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HashVerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should verify exact duplicates`() = runBlocking {
        val file1 = File(tempFolder.root, "a.txt")
        val file2 = File(tempFolder.root, "b.txt")
        file1.writeText("same content")
        file2.writeText("same content")

        val candidates = listOf(
            listOf(
                FileInfo(file1.absolutePath, "a.txt", file1.length(), 0, "txt"),
                FileInfo(file2.absolutePath, "b.txt", file2.length(), 0, "txt")
            )
        )

        val groups = HashVerifier.verify(candidates)
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].files.size)
    }

    @Test
    fun `should reject files with different content`() = runBlocking {
        val file1 = File(tempFolder.root, "a.txt")
        val file2 = File(tempFolder.root, "b.txt")
        file1.writeText("content A")
        file2.writeText("content B")

        val candidates = listOf(
            listOf(
                FileInfo(file1.absolutePath, "a.txt", file1.length(), 0, "txt"),
                FileInfo(file2.absolutePath, "b.txt", file2.length(), 0, "txt")
            )
        )

        val groups = HashVerifier.verify(candidates)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should handle multiple groups`() = runBlocking {
        val file1 = File(tempFolder.root, "a1.txt")
        val file2 = File(tempFolder.root, "a2.txt")
        val file3 = File(tempFolder.root, "b1.txt")
        val file4 = File(tempFolder.root, "b2.txt")
        file1.writeText("group A")
        file2.writeText("group A")
        file3.writeText("group B")
        file4.writeText("group B")

        val candidates = listOf(
            listOf(
                FileInfo(file1.absolutePath, "a1.txt", file1.length(), 0, "txt"),
                FileInfo(file2.absolutePath, "a2.txt", file2.length(), 0, "txt")
            ),
            listOf(
                FileInfo(file3.absolutePath, "b1.txt", file3.length(), 0, "txt"),
                FileInfo(file4.absolutePath, "b2.txt", file4.length(), 0, "txt")
            )
        )

        val groups = HashVerifier.verify(candidates)
        assertEquals(2, groups.size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.HashVerifierTest"`
Expected: FAIL with "HashVerifier not found"

- [ ] **Step 3: Implement HashVerifier**

```kotlin
// duplicates/src/main/java/com/cleanmaster/duplicates/HashVerifier.kt
package com.cleanmaster.duplicates

import com.cleanmaster.core.model.DuplicateGroup
import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object HashVerifier {

    /**
     * Takes candidate groups from QuickFilter, computes SHA-256,
     * and returns only groups where all files have the same hash.
     */
    suspend fun verify(candidates: List<List<FileInfo>>): List<DuplicateGroup> {
        return withContext(Dispatchers.IO) {
            candidates.mapNotNull { group ->
                val filesWithHash = group.map { file ->
                    file to HashUtil.calculateSha256(File(file.path))
                }

                val hashGroups = filesWithHash.groupBy { it.second }
                val duplicateHashGroup = hashGroups.entries.find { it.value.size >= 2 }

                duplicateHashGroup?.let { entry ->
                    DuplicateGroup(
                        id = UUID.randomUUID().toString(),
                        files = entry.value.map { it.first.copy(hash = entry.key) },
                        totalSize = entry.value.sumOf { it.first.size },
                        fileHash = entry.key
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.HashVerifierTest"`
Expected: 3 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add duplicates/src/
git commit -m "feat(duplicates): add HashVerifier"
```

---

## Task 8: Screenshot Analysis — PerceptualHasher

**Files:**
- Create: `duplicates/src/main/java/com/cleanmaster/duplicates/PerceptualHasher.kt`
- Create: `duplicates/src/test/java/com/cleanmaster/duplicates/PerceptualHasherTest.kt`

- [ ] **Step 1: Write PerceptualHasher test**

```kotlin
// duplicates/src/test/java/com/cleanmaster/duplicates/PerceptualHasherTest.kt
package com.cleanmaster.duplicates

import org.junit.Assert.*
import org.junit.Test

class PerceptualHasherTest {

    @Test
    fun `hamming distance should be 0 for same hash`() {
        val hash = 0b1010101010101010L
        assertEquals(0, PerceptualHasher.hammingDistance(hash, hash))
    }

    @Test
    fun `hamming distance should count different bits`() {
        val hash1 = 0b1010101010101010L
        val hash2 = 0b0101010101010101L
        assertEquals(32, PerceptualHasher.hammingDistance(hash1, hash2))
    }

    @Test
    fun `similarity should be 1 for same hash`() {
        val hash = 0b1010101010101010L
        assertEquals(1.0f, PerceptualHasher.similarity(hash, hash), 0.01f)
    }

    @Test
    fun `similarity should be 0 for completely different hash`() {
        val hash1 = 0L
        val hash2 = -1L  // all bits set
        assertEquals(0.0f, PerceptualHasher.similarity(hash1, hash2), 0.01f)
    }

    @Test
    fun `similarity should be between 0 and 1`() {
        val hash1 = 0b1111000011110000L
        val hash2 = 0b1111000000001111L
        val sim = PerceptualHasher.similarity(hash1, hash2)
        assertTrue(sim in 0.0f..1.0f)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.PerceptualHasherTest"`
Expected: FAIL with "PerceptualHasher not found"

- [ ] **Step 3: Implement PerceptualHasher**

```kotlin
// duplicates/src/main/java/com/cleanmaster/duplicates/PerceptualHasher.kt
package com.cleanmaster.duplicates

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.PI

object PerceptualHasher {

    private const val HASH_SIZE = 8
    private const val RESIZE_SIZE = 32

    /**
     * Calculates perceptual hash for a bitmap using DCT.
     * Returns a 64-bit hash as Long.
     */
    suspend fun calculatePHash(bitmap: Bitmap): Long = withContext(Dispatchers.Default) {
        // Step 1: Resize to 32x32
        val resized = Bitmap.createScaledBitmap(bitmap, RESIZE_SIZE, RESIZE_SIZE, true)

        // Step 2: Convert to grayscale
        val grayscale = Array(RESIZE_SIZE) { y ->
            IntArray(RESIZE_SIZE) { x ->
                val pixel = resized.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }
        }

        // Step 3: Apply DCT
        val dct = applyDCT(grayscale)

        // Step 4: Extract top-left 8x8
        val lowFreq = Array(HASH_SIZE) { y ->
            DoubleArray(HASH_SIZE) { x -> dct[y][x] }
        }

        // Step 5: Calculate mean (excluding DC component)
        var sum = 0.0
        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                if (y == 0 && x == 0) continue
                sum += lowFreq[y][x]
            }
        }
        val mean = sum / (HASH_SIZE * HASH_SIZE - 1)

        // Step 6: Generate hash
        var hash = 0L
        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                if (lowFreq[y][x] > mean) {
                    hash = hash or (1L shl (y * HASH_SIZE + x))
                }
            }
        }

        hash
    }

    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var xor = hash1 xor hash2
        var count = 0
        while (xor != 0L) {
            count++
            xor = xor and (xor - 1)
        }
        return count
    }

    fun similarity(hash1: Long, hash2: Long): Float {
        val distance = hammingDistance(hash1, hash2)
        return 1.0f - (distance.toFloat() / 64f)
    }

    private fun applyDCT(input: Array<IntArray>): Array<DoubleArray> {
        val n = input.size
        val result = Array(n) { DoubleArray(n) }

        for (u in 0 until n) {
            for (v in 0 until n) {
                var sum = 0.0
                for (i in 0 until n) {
                    for (j in 0 until n) {
                        sum += input[i][j] *
                            cos((2 * i + 1) * u * PI / (2 * n)) *
                            cos((2 * j + 1) * v * PI / (2 * n))
                    }
                }
                val cu = if (u == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
                val cv = if (v == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
                result[u][v] = 0.25 * cu * cv * sum
            }
        }

        return result
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.PerceptualHasherTest"`
Expected: 5 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add duplicates/src/
git commit -m "feat(duplicates): add PerceptualHasher for screenshot similarity"
```

---

## Task 9: Screenshot Analysis — QualityAnalyzer

**Files:**
- Create: `duplicates/src/main/java/com/cleanmaster/duplicates/QualityAnalyzer.kt`
- Create: `duplicates/src/test/java/com/cleanmaster/duplicates/QualityAnalyzerTest.kt`

- [ ] **Step 1: Write QualityAnalyzer test**

```kotlin
// duplicates/src/test/java/com/cleanmaster/duplicates/QualityAnalyzerTest.kt
package com.cleanmaster.duplicates

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class QualityAnalyzerTest {

    @Test
    fun `should detect black screen`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLACK)
        assertTrue(QualityAnalyzer.isBlackScreen(bitmap))
        bitmap.recycle()
    }

    @Test
    fun `should not detect normal image as black screen`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)
        assertFalse(QualityAnalyzer.isBlackScreen(bitmap))
        bitmap.recycle()
    }

    @Test
    fun `should detect solid color`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLUE)
        assertTrue(QualityAnalyzer.isSolidColor(bitmap))
        bitmap.recycle()
    }

    @Test
    fun `blur score should be low for uniform image`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        val score = QualityAnalyzer.blurScore(bitmap)
        assertTrue(score < 0.1f)
        bitmap.recycle()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.QualityAnalyzerTest"`
Expected: FAIL with "QualityAnalyzer not found"

- [ ] **Step 3: Implement QualityAnalyzer**

```kotlin
// duplicates/src/main/java/com/cleanmaster/duplicates/QualityAnalyzer.kt
package com.cleanmaster.duplicates

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix

object QualityAnalyzer {

    private const val BLACK_THRESHOLD = 30
    private const val SOLID_COLOR_THRESHOLD = 0.95

    /**
     * Detects if an image is mostly black (average brightness < threshold).
     */
    fun isBlackScreen(bitmap: Bitmap): Boolean {
        val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
        var totalBrightness = 0L
        var pixelCount = 0

        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                val pixel = scaled.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                totalBrightness += brightness
                pixelCount++
            }
        }

        val avgBrightness = totalBrightness / pixelCount
        scaled.recycle()
        return avgBrightness < BLACK_THRESHOLD
    }

    /**
     * Detects if an image is a solid color (dominant color > 95% of pixels).
     */
    fun isSolidColor(bitmap: Bitmap): Boolean {
        val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
        val colorCounts = mutableMapOf<Int, Int>()
        var totalPixels = 0

        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                // Quantize to reduce color space
                val pixel = scaled.getPixel(x, y)
                val quantized = Color.rgb(
                    Color.red(pixel) / 16 * 16,
                    Color.green(pixel) / 16 * 16,
                    Color.blue(pixel) / 16 * 16
                )
                colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
                totalPixels++
            }
        }

        scaled.recycle()
        val maxCount = colorCounts.values.maxOrNull() ?: 0
        return maxCount.toFloat() / totalPixels > SOLID_COLOR_THRESHOLD
    }

    /**
     * Calculates blur score using Laplacian variance.
     * Lower score = more blurry. 0 = completely uniform.
     */
    fun blurScore(bitmap: Bitmap): Float {
        val scaled = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        val pixels = Array(scaled.height) { y ->
            IntArray(scaled.width) { x ->
                val pixel = scaled.getPixel(x, y)
                (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            }
        }

        scaled.recycle()

        // Laplacian kernel: [0,1,0; 1,-4,1; 0,1,0]
        var sum = 0.0
        var count = 0
        for (y in 1 until pixels.size - 1) {
            for (x in 1 until pixels[0].size - 1) {
                val laplacian = pixels[y - 1][x] + pixels[y + 1][x] +
                    pixels[y][x - 1] + pixels[y][x + 1] - 4 * pixels[y][x]
                sum += laplacian * laplacian
                count++
            }
        }

        return (sum / count).toFloat()
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :duplicates:test --tests "com.cleanmaster.duplicates.QualityAnalyzerTest"`
Expected: 4 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add duplicates/src/
git commit -m "feat(duplicates): add QualityAnalyzer for screenshot quality detection"
```

---

## Task 10: Cleaner Module

**Files:**
- Create: `cleaner/src/main/java/com/cleanmaster/cleaner/Cleaner.kt`
- Create: `cleaner/src/main/java/com/cleanmaster/cleaner/CacheCleaner.kt`
- Create: `cleaner/src/main/java/com/cleanmaster/cleaner/JunkCleaner.kt`
- Create: `cleaner/src/main/java/com/cleanmaster/cleaner/LargeFileFinder.kt`
- Create: `cleaner/src/test/java/com/cleanmaster/cleaner/CacheCleanerTest.kt`
- Create: `cleaner/src/test/java/com/cleanmaster/cleaner/LargeFileFinderTest.kt`

- [ ] **Step 1: Create Cleaner interface**

```kotlin
// cleaner/src/main/java/com/cleanmaster/cleaner/Cleaner.kt
package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanableItem
import kotlinx.coroutines.flow.Flow

data class CleanProgress(
    val scannedCount: Int,
    val foundCount: Int,
    val foundSize: Long
)

data class CleanResult(
    val cleanedCount: Int,
    val freedSize: Long,
    val errors: List<String>
)

interface Cleaner {
    suspend fun scan(): Flow<CleanProgress>
    fun getItems(): List<CleanableItem>
    suspend fun clean(items: List<CleanableItem>): CleanResult
}
```

- [ ] **Step 2: Write CacheCleaner test**

```kotlin
// cleaner/src/test/java/com/cleanmaster/cleaner/CacheCleanerTest.kt
package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class CacheCleanerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find cache files`() = runBlocking {
        val cacheDir = tempFolder.newFolder("cache")
        File(cacheDir, "data.cache").writeText("cache content")

        val cleaner = CacheCleaner(tempFolder.root.absolutePath)
        cleaner.scan().toList()
        val items = cleaner.getItems()

        assertTrue(items.isNotEmpty())
        assertEquals(CleanType.CACHE, items[0].type)
        assertEquals(RiskLevel.LOW, items[0].riskLevel)
    }

    @Test
    fun `should clean cache files`() = runBlocking {
        val cacheDir = tempFolder.newFolder("cache")
        val cacheFile = File(cacheDir, "data.cache")
        cacheFile.writeText("cache content")

        val cleaner = CacheCleaner(tempFolder.root.absolutePath)
        cleaner.scan().toList()
        val items = cleaner.getItems()
        val result = cleaner.clean(items)

        assertEquals(1, result.cleanedCount)
        assertFalse(cacheFile.exists())
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :cleaner:test --tests "com.cleanmaster.cleaner.CacheCleanerTest"`
Expected: FAIL with "CacheCleaner not found"

- [ ] **Step 4: Implement CacheCleaner**

```kotlin
// cleaner/src/main/java/com/cleanmaster/cleaner/CacheCleaner.kt
package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.CleanableItem
import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class CacheCleaner(private val rootPath: String) : Cleaner {

    private var items: List<CleanableItem> = emptyList()

    override suspend fun scan(): Flow<CleanProgress> = flow {
        val root = File(rootPath)
        val cacheItems = mutableListOf<CleanableItem>()
        var scannedCount = 0
        var totalSize = 0L

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && (file.parentFile?.name?.contains("cache", ignoreCase = true) == true)) {
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        mimeType = file.extension
                    )
                    cacheItems.add(CleanableItem(fileInfo, CleanType.CACHE, RiskLevel.LOW))
                    totalSize += file.length()
                }

                if (scannedCount % 100 == 0) {
                    emit(CleanProgress(scannedCount, cacheItems.size, totalSize))
                }
            }
        }

        items = cacheItems
        emit(CleanProgress(scannedCount, cacheItems.size, totalSize))
    }

    override fun getItems(): List<CleanableItem> = items

    override suspend fun clean(items: List<CleanableItem>): CleanResult {
        var cleanedCount = 0
        var freedSize = 0L
        val errors = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            items.forEach { item ->
                try {
                    val file = File(item.fileInfo.path)
                    if (file.exists()) {
                        freedSize += file.length()
                        file.delete()
                        cleanedCount++
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${item.fileInfo.path}: ${e.message}")
                }
            }
        }

        return CleanResult(cleanedCount, freedSize, errors)
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :cleaner:test --tests "com.cleanmaster.cleaner.CacheCleanerTest"`
Expected: 2 tests PASSED

- [ ] **Step 6: Write LargeFileFinder test**

```kotlin
// cleaner/src/test/java/com/cleanmaster/cleaner/LargeFileFinderTest.kt
package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LargeFileFinderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find files larger than threshold`() = runBlocking {
        val largeFile = File(tempFolder.root, "large.dat")
        largeFile.writeBytes(ByteArray(200 * 1024 * 1024)) // 200MB

        val finder = LargeFileFinder(tempFolder.root.absolutePath, thresholdMB = 100)
        finder.scan().toList()
        val items = finder.getItems()

        assertEquals(1, items.size)
        assertEquals(CleanType.LARGE_FILE, items[0].type)
        assertEquals(RiskLevel.MEDIUM, items[0].riskLevel)
    }

    @Test
    fun `should not find files smaller than threshold`() = runBlocking {
        val smallFile = File(tempFolder.root, "small.dat")
        smallFile.writeBytes(ByteArray(50 * 1024 * 1024)) // 50MB

        val finder = LargeFileFinder(tempFolder.root.absolutePath, thresholdMB = 100)
        finder.scan().toList()
        val items = finder.getItems()

        assertTrue(items.isEmpty())
    }
}
```

- [ ] **Step 7: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :cleaner:test --tests "com.cleanmaster.cleaner.LargeFileFinderTest"`
Expected: FAIL with "LargeFileFinder not found"

- [ ] **Step 8: Implement LargeFileFinder and JunkCleaner**

```kotlin
// cleaner/src/main/java/com/cleanmaster/cleaner/LargeFileFinder.kt
package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.CleanableItem
import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class LargeFileFinder(
    private val rootPath: String,
    private val thresholdMB: Int = 100
) : Cleaner {

    private var items: List<CleanableItem> = emptyList()
    private val thresholdBytes = thresholdMB.toLong() * 1024 * 1024

    override suspend fun scan(): Flow<CleanProgress> = flow {
        val root = File(rootPath)
        val largeItems = mutableListOf<CleanableItem>()
        var scannedCount = 0

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && file.length() > thresholdBytes) {
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        mimeType = file.extension
                    )
                    largeItems.add(CleanableItem(fileInfo, CleanType.LARGE_FILE, RiskLevel.MEDIUM))
                }

                if (scannedCount % 100 == 0) {
                    emit(CleanProgress(scannedCount, largeItems.size, largeItems.sumOf { it.fileInfo.size }))
                }
            }
        }

        items = largeItems
        emit(CleanProgress(scannedCount, largeItems.size, largeItems.sumOf { it.fileInfo.size }))
    }

    override fun getItems(): List<CleanableItem> = items

    override suspend fun clean(items: List<CleanableItem>): CleanResult {
        var cleanedCount = 0
        var freedSize = 0L
        val errors = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            items.forEach { item ->
                try {
                    val file = File(item.fileInfo.path)
                    if (file.exists()) {
                        freedSize += file.length()
                        file.delete()
                        cleanedCount++
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${item.fileInfo.path}: ${e.message}")
                }
            }
        }

        return CleanResult(cleanedCount, freedSize, errors)
    }
}
```

```kotlin
// cleaner/src/main/java/com/cleanmaster/cleaner/JunkCleaner.kt
package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.CleanableItem
import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class JunkCleaner(private val rootPath: String) : Cleaner {

    private var items: List<CleanableItem> = emptyList()
    private val junkExtensions = setOf(".tmp", ".log", ".bak", ".temp", ".swp")

    override suspend fun scan(): Flow<CleanProgress> = flow {
        val root = File(rootPath)
        val junkItems = mutableListOf<CleanableItem>()
        var scannedCount = 0

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && ".${file.extension.lowercase()}" in junkExtensions) {
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        mimeType = file.extension
                    )
                    junkItems.add(CleanableItem(fileInfo, CleanType.JUNK, RiskLevel.LOW))
                }

                if (scannedCount % 100 == 0) {
                    emit(CleanProgress(scannedCount, junkItems.size, junkItems.sumOf { it.fileInfo.size }))
                }
            }
        }

        items = junkItems
        emit(CleanProgress(scannedCount, junkItems.size, junkItems.sumOf { it.fileInfo.size }))
    }

    override fun getItems(): List<CleanableItem> = items

    override suspend fun clean(items: List<CleanableItem>): CleanResult {
        var cleanedCount = 0
        var freedSize = 0L
        val errors = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            items.forEach { item ->
                try {
                    val file = File(item.fileInfo.path)
                    if (file.exists()) {
                        freedSize += file.length()
                        file.delete()
                        cleanedCount++
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${item.fileInfo.path}: ${e.message}")
                }
            }
        }

        return CleanResult(cleanedCount, freedSize, errors)
    }
}
```

- [ ] **Step 9: Run all cleaner tests**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :cleaner:test`
Expected: 4 tests PASSED

- [ ] **Step 10: Commit**

```bash
git add cleaner/src/
git commit -m "feat(cleaner): add CacheCleaner, JunkCleaner, LargeFileFinder"
```

---

## Task 11: Trash Module

**Files:**
- Create: `trash/src/main/java/com/cleanmaster/trash/TrashDao.kt`
- Create: `trash/src/main/java/com/cleanmaster/trash/TrashDatabase.kt`
- Create: `trash/src/main/java/com/cleanmaster/trash/TrashManager.kt`
- Create: `trash/src/main/java/com/cleanmaster/trash/TrashConfig.kt`
- Create: `trash/src/test/java/com/cleanmaster/trash/TrashManagerTest.kt`

- [ ] **Step 1: Create TrashConfig**

```kotlin
// trash/src/main/java/com/cleanmaster/trash/TrashConfig.kt
package com.cleanmaster.trash

data class TrashConfig(
    val maxSizeMB: Int = 2048,
    val autoCleanWhenFull: Boolean = true,
    val confirmPermanentDelete: Boolean = true
)
```

- [ ] **Step 2: Create TrashDao**

```kotlin
// trash/src/main/java/com/cleanmaster/trash/TrashDao.kt
package com.cleanmaster.trash

import androidx.room.*
import com.cleanmaster.core.model.TrashItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    fun getAllItems(): Flow<List<TrashItem>>

    @Query("SELECT * FROM trash_items WHERE id = :id")
    suspend fun getById(id: Long): TrashItem?

    @Query("SELECT SUM(size) FROM trash_items")
    suspend fun getTotalSize(): Long?

    @Query("SELECT COUNT(*) FROM trash_items")
    suspend fun getItemCount(): Int

    @Insert
    suspend fun insert(item: TrashItem)

    @Insert
    suspend fun insertAll(items: List<TrashItem>)

    @Delete
    suspend fun delete(item: TrashItem)

    @Query("DELETE FROM trash_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM trash_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM trash_items ORDER BY deletedAt ASC LIMIT 1")
    suspend fun getOldestItem(): TrashItem?
}
```

- [ ] **Step 3: Create TrashDatabase**

```kotlin
// trash/src/main/java/com/cleanmaster/trash/TrashDatabase.kt
package com.cleanmaster.trash

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cleanmaster.core.model.TrashItem

@Database(entities = [TrashItem::class], version = 1, exportSchema = false)
abstract class TrashDatabase : RoomDatabase() {
    abstract fun trashDao(): TrashDao

    companion object {
        @Volatile
        private var INSTANCE: TrashDatabase? = null

        fun getInstance(context: Context): TrashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrashDatabase::class.java,
                    "trash_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

- [ ] **Step 4: Write TrashManager test**

```kotlin
// trash/src/test/java/com/cleanmaster/trash/TrashManagerTest.kt
package com.cleanmaster.trash

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.CleanableItem
import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.model.RiskLevel
import com.cleanmaster.core.model.TrashItem
import com.cleanmaster.core.model.FileType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class TrashManagerTest {

    private lateinit var trashDao: TrashDao
    private lateinit var trashDir: File
    private lateinit var config: TrashConfig

    @Before
    fun setup() {
        trashDao = mockk(relaxed = true)
        trashDir = File.createTempFile("trash", "").apply {
            delete()
            mkdirs()
        }
        config = TrashConfig(maxSizeMB = 100)
    }

    @Test
    fun `should move file to trash`() = runBlocking {
        val sourceFile = File.createTempFile("test", ".txt")
        sourceFile.writeText("test content")

        coEvery { trashDao.getTotalSize() } returns 0L
        coEvery { trashDao.insert(any()) } just Runs

        val manager = TrashManager(trashDao, trashDir.absolutePath, config)
        val item = CleanableItem(
            FileInfo(sourceFile.absolutePath, sourceFile.name, sourceFile.length(), 0, "txt"),
            CleanType.JUNK,
            RiskLevel.LOW
        )

        val result = manager.moveToTrash(listOf(item))
        assertEquals(1, result.movedCount)
        assertFalse(sourceFile.exists())

        sourceFile.delete()
        trashDir.deleteRecursively()
    }

    @Test
    fun `should restore file from trash`() = runBlocking {
        val originalPath = File.createTempFile("original", ".txt").absolutePath
        val trashFile = File(trashDir, "test.txt")
        trashFile.writeText("restored content")

        coEvery { trashDao.getById(1L) } returns TrashItem(
            id = 1,
            originalPath = originalPath,
            trashPath = trashFile.absolutePath,
            fileName = "test.txt",
            size = 100,
            deletedAt = System.currentTimeMillis(),
            fileType = FileType.JUNK
        )
        coEvery { trashDao.delete(any()) } just Runs

        val manager = TrashManager(trashDao, trashDir.absolutePath, config)
        val trashItem = TrashItem(
            id = 1,
            originalPath = originalPath,
            trashPath = trashFile.absolutePath,
            fileName = "test.txt",
            size = 100,
            deletedAt = System.currentTimeMillis(),
            fileType = FileType.JUNK
        )

        val result = manager.restore(listOf(trashItem))
        assertEquals(1, result.restoredCount)

        File(originalPath).delete()
        trashDir.deleteRecursively()
    }
}
```

- [ ] **Step 5: Run test to verify it fails**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :trash:test --tests "com.cleanmaster.trash.TrashManagerTest"`
Expected: FAIL with "TrashManager not found"

- [ ] **Step 6: Implement TrashManager**

```kotlin
// trash/src/main/java/com/cleanmaster/trash/TrashManager.kt
package com.cleanmaster.trash

import com.cleanmaster.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class TrashResult(val movedCount: Long, val totalSize: Long)
data class RestoreResult(val restoredCount: Int, val errors: List<String>)
data class DeleteResult(val deletedCount: Int, val freedSize: Long)
data class TrashInfo(val itemCount: Int, val totalSize: Long, val oldestItemDate: Long)

class TrashManager(
    private val trashDao: TrashDao,
    private val trashDirPath: String,
    private val config: TrashConfig
) {
    private val trashDir = File(trashDirPath)

    init {
        if (!trashDir.exists()) {
            trashDir.mkdirs()
        }
    }

    suspend fun moveToTrash(items: List<CleanableItem>): TrashResult = withContext(Dispatchers.IO) {
        var movedCount = 0L
        var totalSize = 0L

        items.forEach { item ->
            try {
                val source = File(item.fileInfo.path)
                if (!source.exists()) return@forEach

                val trashFile = File(trashDir, "${UUID.randomUUID()}_${item.fileInfo.name}")
                source.copyTo(trashFile, overwrite = true)
                source.delete()

                val trashItem = TrashItem(
                    originalPath = item.fileInfo.path,
                    trashPath = trashFile.absolutePath,
                    fileName = item.fileInfo.name,
                    size = item.fileInfo.size,
                    deletedAt = System.currentTimeMillis(),
                    fileType = when (item.type) {
                        CleanType.CACHE -> FileType.CACHE
                        CleanType.JUNK -> FileType.JUNK
                        CleanType.LARGE_FILE -> FileType.LARGE_FILE
                        CleanType.EMPTY_FOLDER -> FileType.EMPTY_FOLDER
                        CleanType.MEDIA -> FileType.MEDIA
                        CleanType.SCREENSHOT -> FileType.DUPLICATE
                    }
                )

                trashDao.insert(trashItem)
                movedCount++
                totalSize += item.fileInfo.size
            } catch (e: Exception) {
                // Skip files that fail
            }
        }

        autoCleanIfNeeded()
        TrashResult(movedCount, totalSize)
    }

    suspend fun restore(items: List<TrashItem>): RestoreResult = withContext(Dispatchers.IO) {
        var restoredCount = 0
        val errors = mutableListOf<String>()

        items.forEach { item ->
            try {
                val trashFile = File(item.trashPath)
                if (!trashFile.exists()) {
                    errors.add("Trash file not found: ${item.trashPath}")
                    return@forEach
                }

                val originalFile = File(item.originalPath)
                originalFile.parentFile?.mkdirs()
                trashFile.copyTo(originalFile, overwrite = true)
                trashFile.delete()
                trashDao.delete(item)
                restoredCount++
            } catch (e: Exception) {
                errors.add("Failed to restore ${item.fileName}: ${e.message}")
            }
        }

        RestoreResult(restoredCount, errors)
    }

    suspend fun permanentDelete(items: List<TrashItem>): DeleteResult = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var freedSize = 0L

        items.forEach { item ->
            try {
                val trashFile = File(item.trashPath)
                if (trashFile.exists()) {
                    freedSize += trashFile.length()
                    trashFile.delete()
                }
                trashDao.delete(item)
                deletedCount++
            } catch (e: Exception) {
                // Skip files that fail
            }
        }

        DeleteResult(deletedCount, freedSize)
    }

    suspend fun emptyTrash(): DeleteResult = withContext(Dispatchers.IO) {
        val items = trashDao.getAllItems().let { flow ->
            var result = emptyList<TrashItem>()
            flow.collect { result = it }
            result
        }
        permanentDelete(items)
    }

    suspend fun getTrashInfo(): TrashInfo {
        val itemCount = trashDao.getItemCount()
        val totalSize = trashDao.getTotalSize() ?: 0L
        val oldestItem = trashDao.getOldestItem()
        return TrashInfo(itemCount, totalSize, oldestItem?.deletedAt ?: 0L)
    }

    private suspend fun autoCleanIfNeeded() {
        if (!config.autoCleanWhenFull) return

        val currentSize = trashDao.getTotalSize() ?: 0L
        val maxSize = config.maxSizeMB.toLong() * 1024 * 1024

        if (currentSize > maxSize) {
            val oldest = trashDao.getOldestItem() ?: return
            permanentDelete(listOf(oldest))
        }
    }
}
```

- [ ] **Step 7: Run test to verify it passes**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :trash:test --tests "com.cleanmaster.trash.TrashManagerTest"`
Expected: 2 tests PASSED

- [ ] **Step 8: Commit**

```bash
git add trash/src/
git commit -m "feat(trash): add TrashManager with Room database"
```

---

## Task 12: App Theme & Navigation

**Files:**
- Create: `app/src/main/java/com/cleanmaster/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/cleanmaster/ui/theme/Color.kt`
- Create: `app/src/main/java/com/cleanmaster/ui/theme/Type.kt`
- Create: `app/src/main/java/com/cleanmaster/navigation/AppNavigation.kt`
- Create: `app/src/main/java/com/cleanmaster/MainActivity.kt`
- Create: `app/src/main/java/com/cleanmaster/CleanMasterApp.kt`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values-zh/strings.xml`

- [ ] **Step 1: Create Theme files**

```kotlin
// app/src/main/java/com/cleanmaster/ui/theme/Color.kt
package com.cleanmaster.ui.theme

import androidx.compose.ui.graphics.Color

val Blue40 = Color(0xFF1565C0)
val Blue80 = Color(0xFF90CAF9)
val Purple40 = Color(0xFF7B1FA2)
val Purple80 = Color(0xFFCE93D8)
val Pink40 = Color(0xFFC2185B)
val Pink80 = Color(0xFFF48FB1)
```

```kotlin
// app/src/main/java/com/cleanmaster/ui/theme/Type.kt
package com.cleanmaster.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
```

```kotlin
// app/src/main/java/com/cleanmaster/ui/theme/Theme.kt
package com.cleanmaster.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = Purple80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = Purple40,
    tertiary = Pink40
)

@Composable
fun CleanMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 2: Create AppNavigation**

```kotlin
// app/src/main/java/com/cleanmaster/navigation/AppNavigation.kt
package com.cleanmaster.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Screenshots : Screen("screenshots", "截图", Icons.Default.PhotoLibrary)
    object Trash : Screen("trash", "回收站", Icons.Default.Delete)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val screens = listOf(Screen.Home, Screen.Screenshots, Screen.Trash, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { /* HomeScreen placeholder */ }
            composable(Screen.Screenshots.route) { /* ScreenshotScreen placeholder */ }
            composable(Screen.Trash.route) { /* TrashScreen placeholder */ }
            composable(Screen.Settings.route) { /* SettingsScreen placeholder */ }
        }
    }
}
```

- [ ] **Step 3: Create AndroidManifest.xml**

```xml
<!-- app/src/main/AndroidManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

    <application
        android:name=".CleanMasterApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.CleanMaster">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.CleanMaster">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"/>
    </application>
</manifest>
```

- [ ] **Step 4: Create CleanMasterApp and MainActivity**

```kotlin
// app/src/main/java/com/cleanmaster/CleanMasterApp.kt
package com.cleanmaster

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CleanMasterApp : Application()
```

```kotlin
// app/src/main/java/com/cleanmaster/MainActivity.kt
package com.cleanmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cleanmaster.navigation.AppNavigation
import com.cleanmaster.ui.theme.CleanMasterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanMasterTheme {
                AppNavigation()
            }
        }
    }
}
```

- [ ] **Step 5: Create strings.xml (English)**

```xml
<!-- app/src/main/res/values/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">CleanMaster</string>
    <string name="scan_button">Scan</string>
    <string name="clean_button">Clean</string>
    <string name="one_click_clean">One Click Clean</string>
    <string name="storage_used">Used: %1$s</string>
    <string name="storage_available">Available: %1$s</string>
    <string name="cleanable_size">Cleanable: %1$s</string>
    <string name="duplicates_found">Found %1$d duplicate groups</string>
    <string name="screenshots_analysis">Screenshot Analysis</string>
    <string name="similar_screenshots">Similar Screenshots</string>
    <string name="low_quality_screenshots">Low Quality Screenshots</string>
    <string name="expired_screenshots">Expired Screenshots</string>
    <string name="trash_title">Trash</string>
    <string name="trash_empty">Trash is empty</string>
    <string name="trash_full">Trash is full, please clean up</string>
    <string name="restore">Restore</string>
    <string name="permanent_delete">Delete Permanently</string>
    <string name="empty_trash">Empty Trash</string>
    <string name="settings_title">Settings</string>
    <string name="language_setting">Language</string>
    <string name="language_system">System Default</string>
    <string name="language_english">English</string>
    <string name="language_chinese">中文</string>
    <string name="trash_capacity">Trash Capacity</string>
    <string name="expired_days">Expired Days</string>
    <string name="permission_required">Storage permission is required to scan files</string>
    <string name="scan_complete">Scan Complete</string>
    <string name="clean_complete">Clean Complete</string>
    <string name="freed_space">Freed %1$s</string>
    <string name="cache_files">Cache Files</string>
    <string name="junk_files">Junk Files</string>
    <string name="large_files">Large Files</string>
    <string name="empty_folders">Empty Folders</string>
    <string name="duplicate_files">Duplicate Files</string>
    <string name="media_files">Media Files</string>
</resources>
```

- [ ] **Step 6: Create strings.xml (Chinese)**

```xml
<!-- app/src/main/res/values-zh/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">清理管家</string>
    <string name="scan_button">扫描</string>
    <string name="clean_button">清理</string>
    <string name="one_click_clean">一键清理</string>
    <string name="storage_used">已用：%1$s</string>
    <string name="storage_available">可用：%1$s</string>
    <string name="cleanable_size">可清理：%1$s</string>
    <string name="duplicates_found">发现 %1$d 组重复文件</string>
    <string name="screenshots_analysis">截图分析</string>
    <string name="similar_screenshots">相似截图</string>
    <string name="low_quality_screenshots">低质量截图</string>
    <string name="expired_screenshots">过期截图</string>
    <string name="trash_title">回收站</string>
    <string name="trash_empty">回收站为空</string>
    <string name="trash_full">回收站已满，请清理后重试</string>
    <string name="restore">恢复</string>
    <string name="permanent_delete">彻底删除</string>
    <string name="empty_trash">清空回收站</string>
    <string name="settings_title">设置</string>
    <string name="language_setting">语言</string>
    <string name="language_system">跟随系统</string>
    <string name="language_english">English</string>
    <string name="language_chinese">中文</string>
    <string name="trash_capacity">回收站容量</string>
    <string name="expired_days">过期天数</string>
    <string name="permission_required">需要存储权限才能扫描文件</string>
    <string name="scan_complete">扫描完成</string>
    <string name="clean_complete">清理完成</string>
    <string name="freed_space">已释放 %1$s</string>
    <string name="cache_files">缓存文件</string>
    <string name="junk_files">垃圾文件</string>
    <string name="large_files">大文件</string>
    <string name="empty_folders">空文件夹</string>
    <string name="duplicate_files">重复文件</string>
    <string name="media_files">媒体文件</string>
</resources>
```

- [ ] **Step 7: Verify build**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/
git commit -m "feat(app): add theme, navigation, MainActivity, and i18n strings"
```

---

## Task 13: HomeScreen UI

**Files:**
- Create: `app/src/main/java/com/cleanmaster/ui/home/HomeScreen.kt`
- Create: `app/src/main/java/com/cleanmaster/ui/home/HomeViewModel.kt`

- [ ] **Step 1: Create HomeViewModel**

```kotlin
// app/src/main/java/com/cleanmaster/ui/home/HomeViewModel.kt
package com.cleanmaster.ui.home

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanmaster.core.model.ScanResult
import com.cleanmaster.scanner.FileScanner
import com.cleanmaster.scanner.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isScanning: Boolean = false,
    val scanProgress: ScanProgress? = null,
    val scanResult: ScanResult? = null,
    val totalStorage: Long = 0,
    val usedStorage: Long = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val fileScanner = FileScanner()

    init {
        loadStorageInfo()
    }

    private fun loadStorageInfo() {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val available = stat.availableBytes
        _uiState.update {
            it.copy(
                totalStorage = total,
                usedStorage = total - available
            )
        }
    }

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }

            try {
                val rootPath = Environment.getExternalStorageDirectory().absolutePath
                fileScanner.scan(rootPath).collect { progress ->
                    _uiState.update { it.copy(scanProgress = progress) }
                }

                val result = fileScanner.getResult()
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanResult = result,
                        scanProgress = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        error = e.message
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create HomeScreen**

```kotlin
// app/src/main/java/com/cleanmaster/ui/home/HomeScreen.kt
package com.cleanmaster.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cleanmaster.R
import com.cleanmaster.core.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Storage card
        StorageOverviewCard(
            totalStorage = uiState.totalStorage,
            usedStorage = uiState.usedStorage
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scan progress or result
        if (uiState.isScanning) {
            ScanProgressCard(progress = uiState.scanProgress)
        } else if (uiState.scanResult != null) {
            ScanResultCard(result = uiState.scanResult!!)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan button
        Button(
            onClick = { viewModel.startScan() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isScanning,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (uiState.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.CleaningServices, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uiState.isScanning) stringResource(R.string.scan_button) + "..." else stringResource(R.string.scan_button),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun StorageOverviewCard(totalStorage: Long, usedStorage: Long) {
    val progress = if (totalStorage > 0) usedStorage.toFloat() / totalStorage else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.storage_used, FileSizeFormatter.format(usedStorage)),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.storage_available, FileSizeFormatter.format(totalStorage - usedStorage)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanProgressCard(progress: com.cleanmaster.scanner.ScanProgress?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "扫描中...",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            progress?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已扫描 ${it.scannedCount} 个文件",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ScanResultCard(result: com.cleanmaster.core.model.ScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.cleanable_size, FileSizeFormatter.format(result.totalSize)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (result.cacheFiles.isNotEmpty()) {
                ResultRow(stringResource(R.string.cache_files), result.cacheFiles.size, result.cacheFiles.sumOf { it.size })
            }
            if (result.junkFiles.isNotEmpty()) {
                ResultRow(stringResource(R.string.junk_files), result.junkFiles.size, result.junkFiles.sumOf { it.size })
            }
            if (result.emptyFolders.isNotEmpty()) {
                ResultRow(stringResource(R.string.empty_folders), result.emptyFolders.size, 0)
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, count: Int, size: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "$count 项${if (size > 0) " · ${FileSizeFormatter.format(size)}" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

- [ ] **Step 3: Update AppNavigation to use HomeScreen**

```kotlin
// Update the composable call in AppNavigation.kt
composable(Screen.Home.route) { HomeScreen() }
```

- [ ] **Step 4: Verify build**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat(app): add HomeScreen with storage overview and scan"
```

---

## Task 14: SettingsScreen with Language Switch

**Files:**
- Create: `app/src/main/java/com/cleanmaster/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/cleanmaster/ui/settings/SettingsViewModel.kt`

- [ ] **Step 1: Create SettingsViewModel**

```kotlin
// app/src/main/java/com/cleanmaster/ui/settings/SettingsViewModel.kt
package com.cleanmaster.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class AppLanguage(val displayName: String, val code: String) {
    SYSTEM("跟随系统", "system"),
    ENGLISH("English", "en"),
    CHINESE("中文", "zh")
}

data class SettingsUiState(
    val selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
    val trashCapacityMB: Int = 2048,
    val expiredDays: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val langCode = prefs.getString("language", "system") ?: "system"
        val language = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.SYSTEM

        _uiState.update {
            it.copy(
                selectedLanguage = language,
                trashCapacityMB = prefs.getInt("trash_capacity_mb", 2048),
                expiredDays = prefs.getInt("expired_days", 30)
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.code).apply()
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun setTrashCapacity(mb: Int) {
        prefs.edit().putInt("trash_capacity_mb", mb).apply()
        _uiState.update { it.copy(trashCapacityMB = mb) }
    }

    fun setExpiredDays(days: Int) {
        prefs.edit().putInt("expired_days", days).apply()
        _uiState.update { it.copy(expiredDays = days) }
    }
}
```

- [ ] **Step 2: Create SettingsScreen**

```kotlin
// app/src/main/java/com/cleanmaster/ui/settings/SettingsScreen.kt
package com.cleanmaster.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cleanmaster.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Language setting
        SettingCard(title = stringResource(R.string.language_setting)) {
            LanguageSelector(
                selected = uiState.selectedLanguage,
                onSelect = { viewModel.setLanguage(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trash capacity
        SettingCard(title = stringResource(R.string.trash_capacity)) {
            TrashCapacitySelector(
                capacityMB = uiState.trashCapacityMB,
                onChange = { viewModel.setTrashCapacity(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expired days
        SettingCard(title = stringResource(R.string.expired_days)) {
            ExpiredDaysSelector(
                days = uiState.expiredDays,
                onChange = { viewModel.setExpiredDays(it) }
            )
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LanguageSelector(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Column {
        AppLanguage.entries.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == language,
                    onClick = { onSelect(language) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = language.displayName)
            }
        }
    }
}

@Composable
private fun TrashCapacitySelector(capacityMB: Int, onChange: (Int) -> Unit) {
    val options = listOf(1024, 2048, 5120, 10240, 0) // 1GB, 2GB, 5GB, 10GB, Unlimited
    var selectedIndex by remember { mutableIntStateOf(options.indexOf(capacityMB).coerceAtLeast(1)) }

    Column {
        Text(
            text = if (capacityMB == 0) "不限制" else "${capacityMB / 1024} GB",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = {
                selectedIndex = it.toInt()
                onChange(options[selectedIndex])
            },
            valueRange = 0f..(options.size - 1).toFloat(),
            steps = options.size - 2
        )
    }
}

@Composable
private fun ExpiredDaysSelector(days: Int, onChange: (Int) -> Unit) {
    val options = listOf(7, 14, 30, 60, 90)
    var selectedIndex by remember { mutableIntStateOf(options.indexOf(days).coerceAtLeast(2)) }

    Column {
        Text(
            text = "$days 天",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = {
                selectedIndex = it.toInt()
                onChange(options[selectedIndex])
            },
            valueRange = 0f..(options.size - 1).toFloat(),
            steps = options.size - 2
        )
    }
}
```

- [ ] **Step 3: Update AppNavigation to use SettingsScreen**

```kotlin
composable(Screen.Settings.route) { SettingsScreen() }
```

- [ ] **Step 4: Verify build**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat(app): add SettingsScreen with language and capacity settings"
```

---

## Task 15: Ad Integration

**Files:**
- Create: `app/src/main/java/com/cleanmaster/ad/AdManager.kt`
- Create: `app/src/main/java/com/cleanmaster/ad/AdMobProvider.kt`
- Create: `app/src/main/java/com/cleanmaster/ad/CSJProvider.kt`

- [ ] **Step 1: Create AdMobProvider**

```kotlin
// app/src/main/java/com/cleanmaster/ad/AdMobProvider.kt
package com.cleanmaster.ad

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobProvider(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null

    fun initialize() {
        MobileAds.initialize(context) {}
    }

    fun loadBanner(adView: AdView) {
        adView.loadAd(AdRequest.Builder().build())
    }

    fun loadInterstitial(adUnitId: String) {
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onDismissed()
                }
            }
            ad.show(activity)
        } ?: onDismissed()
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null
}
```

- [ ] **Step 2: Create CSJProvider**

```kotlin
// app/src/main/java/com/cleanmaster/ad/CSJProvider.kt
package com.cleanmaster.ad

import android.app.Activity
import android.content.Context

/**
 * CSJ (穿山甲) ad provider placeholder.
 * Actual implementation requires CSJ SDK integration.
 */
class CSJProvider(private val context: Context) {

    fun initialize(appId: String) {
        // CSJ SDK initialization
        // TTAdSdk.init(context, TTAdConfig.Builder().appId(appId).build())
    }

    fun loadInterstitial(adUnitId: String) {
        // Load CSJ interstitial ad
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        // Show CSJ interstitial ad
        onDismissed()
    }

    fun isInterstitialReady(): Boolean = false
}
```

- [ ] **Step 3: Create AdManager**

```kotlin
// app/src/main/java/com/cleanmaster/ad/AdManager.kt
package com.cleanmaster.ad

import android.app.Activity
import com.google.android.gms.ads.AdView

enum class AdTrigger {
    SCAN_COMPLETE,
    CLEAN_COMPLETE,
    RESTORE_COMPLETE,
    TRASH_EMPTIED
}

class AdManager(
    private val admobProvider: AdMobProvider,
    private val csjProvider: CSJProvider
) {
    private var lastInterstitialTime = mutableMapOf<AdTrigger, Long>()
    private val cooldownMs = 5 * 60 * 1000L // 5 minutes

    fun initialize() {
        admobProvider.initialize()
        // CSJ initialization would go here
    }

    fun loadBanner(adView: AdView) {
        admobProvider.loadBanner(adView)
    }

    fun preloadInterstitial() {
        admobProvider.loadInterstitial("ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx")
        // Also preload CSJ as fallback
    }

    fun showInterstitialIfReady(activity: Activity, trigger: AdTrigger, onDismissed: () -> Unit) {
        val lastTime = lastInterstitialTime[trigger] ?: 0
        val now = System.currentTimeMillis()

        if (now - lastTime < cooldownMs) {
            onDismissed()
            return
        }

        if (admobProvider.isInterstitialReady()) {
            lastInterstitialTime[trigger] = now
            admobProvider.showInterstitial(activity, onDismissed)
        } else if (csjProvider.isInterstitialReady()) {
            lastInterstitialTime[trigger] = now
            csjProvider.showInterstitial(activity, onDismissed)
        } else {
            onDismissed()
        }
    }

    fun showSplashAd(activity: Activity, onDismissed: () -> Unit) {
        // Show splash ad (AdMob app open ad or interstitial)
        admobProvider.showInterstitial(activity, onDismissed)
    }
}
```

- [ ] **Step 4: Verify build**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/
git commit -m "feat(app): add AdMob + CSJ ad integration"
```

---

## Task 16: Run All Tests

- [ ] **Step 1: Run all unit tests**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew test`
Expected: All tests PASSED

- [ ] **Step 2: Verify final build**

Run: `cd /Users/wangxiaolong/CleanMaster && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit**

```bash
git add .
git commit -m "chore: verify all tests pass and build succeeds"
```
