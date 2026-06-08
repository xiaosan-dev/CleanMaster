# ScreenshotManager 设计文档

> 日期：2026-06-08
> 状态：待审核

## 1. 项目概述

清理管家（CleanMaster）是一款 Android 存储清理 + 重复文件分析应用，面向普通用户和进阶用户。默认提供简单模式一键清理，同时支持高级模式查看详细分析结果。

### 核心功能

- 通用存储清理（缓存、垃圾文件、大文件、空文件夹）
- 重复文件检测（两阶段：文件名+大小 → 哈希精确确认）
- 截图专项分析（重复/相似/低质量/过期）
- 回收站保护机制
- 广告变现（启动广告、Banner、插屏）

### 技术栈

- Kotlin + Jetpack Compose
- Material 3 + Material You 动态取色
- 多模块架构

---

## 2. 模块划分与职责

```
ScreenshotManager/
├── app/                # 主入口，导航，DI 组装
├── core/               # 公共基础
│   ├── model/          # 数据模型（FileInfo, DuplicateGroup 等）
│   ├── util/           # 工具类（哈希计算、文件大小格式化等）
│   └── ui/             # 公共 Compose 组件（卡片、按钮样式等）
├── scanner/            # 扫描引擎
│   ├── FileScanner     # 通用文件扫描（缓存、大文件、空文件夹等）
│   └── MediaScanner    # 媒体扫描（照片、视频、截图）
├── duplicates/         # 重复文件分析
│   ├── QuickFilter     # 第一轮：文件名 + 大小快速筛选
│   └── HashVerifier    # 第二轮：哈希精确确认
├── cleaner/            # 清理功能
│   ├── CacheCleaner    # 缓存清理
│   ├── JunkCleaner     # 垃圾文件清理
│   └── LargeFileFinder # 大文件发现
└── trash/              # 回收站
    ├── TrashManager    # 移入/恢复/彻底删除
    └── TrashStorage    # 回收站存储管理
```

### 模块依赖关系

```
app → scanner, duplicates, cleaner, trash
scanner → core
duplicates → core, scanner
cleaner → core, scanner
trash → core
```

---

## 3. 核心数据模型

```kotlin
// 文件信息
data class FileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val mimeType: String,
    val hash: String? = null  // 延迟计算
)

// 重复文件组
data class DuplicateGroup(
    val id: String,
    val files: List<FileInfo>,
    val totalSize: Long,
    val fileHash: String
)

// 扫描结果
data class ScanResult(
    val cacheFiles: List<FileInfo>,      // 缓存文件
    val junkFiles: List<FileInfo>,       // 垃圾文件
    val largeFiles: List<FileInfo>,      // 大文件
    val emptyFolders: List<String>,      // 空文件夹
    val duplicateGroups: List<DuplicateGroup>, // 重复文件组
    val mediaFiles: List<FileInfo>,      // 媒体文件
    val totalSize: Long                  // 可清理总大小
)

// 回收站项目
data class TrashItem(
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val size: Long,
    val deletedAt: Long,
    val fileType: FileType
)

enum class FileType {
    CACHE, JUNK, LARGE_FILE, DUPLICATE, MEDIA, EMPTY_FOLDER
}
```

---

## 4. 扫描引擎设计

### 扫描流程

```
用户点击扫描
    ↓
并行启动多个扫描器
    ├── FileScanner: 扫描缓存、垃圾、空文件夹
    ├── MediaScanner: 扫描照片、视频、截图
    └── LargeFileFinder: 查找大文件
    ↓
合并结果 → ScanResult
    ↓
如果用户选择"重复分析" → 进入 duplicates 模块
    ↓
QuickFilter: 文件名+大小分组
    ↓
HashVerifier: 对候选组计算哈希，确认重复
    ↓
返回 DuplicateGroup 列表
```

### 扫描器接口

```kotlin
interface Scanner<T> {
    suspend fun scan(rootPath: String): Flow<ScanProgress>
    fun getResult(): T
}

data class ScanProgress(
    val scannedCount: Int,
    val currentPath: String,
    val foundSize: Long
)
```

### 关键设计点

- 使用 `Flow` 实时上报扫描进度，UI 可显示进度条
- 扫描在后台协程执行，不阻塞 UI
- 支持取消扫描（协程取消）

---

## 5. 重复文件检测

### 两阶段检测流程

```
阶段一：QuickFilter（快速筛选）
    输入：所有文件列表
    处理：按 (fileName, fileSize) 分组
    输出：候选重复组（size > 1 的组）

阶段二：HashVerifier（哈希验证）
    输入：候选重复组
    处理：对每组文件计算 SHA-256
    输出：确认的 DuplicateGroup 列表
```

### 为什么分两阶段

- 哈希计算是 CPU 密集操作，对所有文件计算太慢
- 先用文件名+大小筛选，大幅减少需要计算哈希的文件数量
- 大部分重复文件天然满足"同名同大小"条件

### 哈希计算

```kotlin
class HashVerifier {
    // 分块读取计算哈希，避免大文件 OOM
    suspend fun calculateHash(file: File): String {
        return withContext(Dispatchers.IO) {
            val buffer = ByteArray(8192)
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().buffered().use { stream ->
                var bytes = stream.read(buffer)
                while (bytes != -1) {
                    digest.update(buffer, 0, bytes)
                    bytes = stream.read(buffer)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
```

---

## 6. 截图专项分析

### 扫描流程

```
扫描截图目录 (DCIM/Screenshots, Pictures/Screenshots 等)
    ↓
┌─────────────────────────────────────────┐
│  并行分析引擎                             │
│  ├── 重复检测：哈希精确匹配                │
│  ├── 相似检测：感知哈希 (pHash) 相似度     │
│  ├── 质量检测：模糊/黑屏/纯色识别          │
│  └── 过期检测：按时间阈值筛选              │
└─────────────────────────────────────────┘
    ↓
汇总 → ScreenshotAnalysisResult
```

### 核心接口

```kotlin
data class ScreenshotAnalysisResult(
    val duplicates: List<DuplicateGroup>,        // 完全重复
    val similar: List<SimilarGroup>,             // 相似截图组
    val lowQuality: List<LowQualityScreenshot>,  // 低质量截图
    val expired: List<ExpiredScreenshot>,        // 过期截图
    val totalCount: Int,
    val recoverableSize: Long
)

data class SimilarGroup(
    val screenshots: List<ScreenshotInfo>,
    val similarity: Float  // 0.0 ~ 1.0
)

data class ScreenshotInfo(
    val fileInfo: FileInfo,
    val width: Int,
    val height: Int,
    val phash: String?,       // 感知哈希
    val blurScore: Float?,    // 模糊程度 0~1
    val isBlackScreen: Boolean,
    val isSolidColor: Boolean
)

data class ExpiredScreenshot(
    val screenshot: ScreenshotInfo,
    val ageInDays: Int
)
```

### 相似度检测算法（pHash）

```kotlin
class PerceptualHasher {
    // 1. 缩小到 32x32
    // 2. 转灰度
    // 3. DCT 变换
    // 4. 取左上 8x8 低频部分
    // 5. 计算均值，大于均值为 1，否则为 0
    // 6. 生成 64 位哈希
    suspend fun calculatePHash(bitmap: Bitmap): Long

    // 汉明距离比较相似度
    fun similarity(hash1: Long, hash2: Long): Float
}
```

### 质量检测策略

```kotlin
class ScreenshotQualityAnalyzer {
    // 模糊检测：拉普拉斯方差法
    fun blurScore(bitmap: Bitmap): Float

    // 黑屏检测：平均亮度 < 阈值
    fun isBlackScreen(bitmap: Bitmap): Boolean

    // 纯色检测：颜色直方图集中度
    fun isSolidColor(bitmap: Bitmap): Boolean
}
```

### 过期截图配置

```kotlin
data class ExpiredConfig(
    val enabled: Boolean = true,
    val thresholdDays: Int = 30,  // 默认 30 天
    val excludeStarred: Boolean = true  // 排除用户标记的
)
```

---

## 7. 清理功能设计

### 清理类型与策略

| 类型 | 扫描策略 | 清理方式 |
|------|----------|----------|
| 缓存文件 | 扫描 `/cache`, `/code_cache`, 应用缓存目录 | 直接删除 |
| 垃圾文件 | 扫描 `.tmp`, `.log`, `.bak` 等临时文件 | 移到回收站 |
| 大文件 | 按大小阈值（默认 100MB）筛选 | 用户选择后移到回收站 |
| 空文件夹 | 递归扫描空目录 | 移到回收站 |
| 媒体文件 | 扫描 DCIM, Pictures, Movies 等目录 | 用户选择后移到回收站 |

### Cleaner 接口

```kotlin
interface Cleaner {
    suspend fun scan(): Flow<CleanProgress>
    fun getItems(): List<CleanableItem>
    suspend fun clean(items: List<CleanableItem>): CleanResult
}

data class CleanableItem(
    val fileInfo: FileInfo,
    val type: CleanType,
    val riskLevel: RiskLevel
)

enum class RiskLevel {
    LOW,      // 缓存、临时文件，安全删除
    MEDIUM,   // 大文件、空文件夹，可能有用
    HIGH      // 媒体文件，用户需确认
}
```

### 风险等级设计

- **LOW**：默认勾选，用户可一键清理
- **MEDIUM**：默认不勾选，用户自行选择
- **HIGH**：必须用户手动确认

---

## 8. 回收站设计

### 回收站工作流

```
用户选择删除
    ↓
TrashManager.moveToTrash(items)
    ├── 记录原始路径、文件名、大小、时间
    ├── 移动文件到 App 私有目录 /trash/
    └── 保存元数据到本地数据库
    ↓
用户查看回收站
    ├── 按类型筛选（缓存/重复/截图等）
    ├── 按时间排序
    └── 支持恢复或彻底删除
    ↓
用户恢复 → 移回原路径
用户彻底删除 → 物理删除
```

### 回收站存储结构

```
/data/data/com.screenshotmanager/
└── trash/
    ├── metadata.db     # SQLite 存储 TrashItem 元数据
    └── files/          # 实际文件暂存
        ├── {uuid}_1.jpg
        ├── {uuid}_2.png
        └── ...
```

### 核心接口

```kotlin
class TrashManager(
    private val trashDao: TrashDao,
    private val fileOperator: FileOperator
) {
    suspend fun moveToTrash(items: List<CleanableItem>): TrashResult
    suspend fun restore(items: List<TrashItem>): RestoreResult
    suspend fun permanentDelete(items: List<TrashItem>): DeleteResult
    suspend fun emptyTrash(): DeleteResult
    suspend fun getTrashInfo(): TrashInfo
}

data class TrashInfo(
    val itemCount: Int,
    val totalSize: Long,
    val oldestItemDate: Long
)

interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    fun getAllItems(): Flow<List<TrashItem>>

    @Insert
    suspend fun insert(item: TrashItem)

    @Delete
    suspend fun delete(item: TrashItem)
}
```

### 回收站配置（用户可在设置中调整）

```kotlin
data class TrashConfig(
    val maxSizeMB: Int = 2048,  // 默认 2GB
    val autoCleanWhenFull: Boolean = true,  // 满时自动清理最旧文件
    val confirmPermanentDelete: Boolean = true  // 彻底删除前确认
)
```

### 安全机制

- 回收站容量上限可由用户在设置中配置（1GB ~ 10GB，或不限制）
- 恢复时如果原路径已不存在，自动创建目录
- 彻底删除前二次确认弹窗

---

## 9. UI 设计 — 双模式架构

### 设计风格

- Material 3 + Material You 动态取色
- 大圆角卡片 + 毛玻璃效果
- 数据可视化（环形图、渐变进度条）
- 流畅动画 + 手势交互
- 底部弹出详情（Bottom Sheet）

### 导航结构

```
App
├── 首页 (HomeScreen)
│   └── 扫描结果 (ScanResultScreen)
├── 截图分析 (ScreenshotScreen)
│   ├── 重复截图
│   ├── 相似截图
│   ├── 低质量截图
│   └── 过期截图
├── 回收站 (TrashScreen)
└── 设置 (SettingsScreen)
    ├── 回收站容量设置
    ├── 过期截图天数设置
    └── 主题/语言设置
```

### 简单模式 UI

```
┌──────────────────────────────────┐
│  ☀️ 早上好                        │
│                                  │
│  ┌────────────────────────────┐  │
│  │     ╭───────╮              │  │
│  │     │ 72%   │  已用 128GB  │  │
│  │     ╰───────╯  可用 50GB   │  │
│  │      ◠ 存储空间            │  │
│  └────────────────────────────┘  │
│                                  │
│  可清理 2.3 GB ✨                │
│                                  │
│  ┌────────┐ ┌────────┐         │
│  │ 🗑️     │ │ 📸     │         │
│  │ 缓存   │ │ 截图   │         │
│  │ 800MB  │ │ 400MB  │         │
│  └────────┘ └────────┘         │
│  ┌────────┐ ┌────────┐         │
│  │ 📋     │ │ 🗂️     │         │
│  │ 重复   │ │ 垃圾   │         │
│  │ 650MB  │ │ 450MB  │         │
│  └────────┘ └────────┘         │
│                                  │
│  [     ✨ 一键清理     ]        │
│                                  │
│  ┌───┬───┬───┬───┐             │
│  │ 🏠│ 📸│ 🗑️│ ⚙️ │             │
│  └───┴───┴───┴───┘             │
└──────────────────────────────────┘
```

### 高级模式 — 截图分析

```
┌──────────────────────────────────┐
│  ← 截图分析                       │
│                                  │
│  ┌────────────────────────────┐  │
│  │ 发现 23 组相似截图          │  │
│  │ 可释放 1.2 GB              │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌─ 相似截图 ────────────── 12组 ┐│
│  │ ┌─────┐ ┌─────┐ ┌─────┐    ││
│  │ │ 📷  │ │ 📷  │ │ 📷  │    ││
│  │ │     │ │ ≈   │ │     │    ││
│  │ └─────┘ └─────┘ └─────┘    ││
│  │  微信截图 x3      95%相似   ││
│  └────────────────────────────┘│
│                                  │
│  ┌─ 低质量 ─────────────── 8张 ─┐│
│  │ ┌─────┐ ┌─────┐ ┌─────┐    ││
│  │ │ 🌑  │ │ ░░░ │ │ ⬛  │    ││
│  │ │黑屏 │ │模糊 │ │纯色 │    ││
│  │ └─────┘ └─────┘ └─────┘    ││
│  └────────────────────────────┘│
│                                  │
│  ┌─ 过期截图 ──────────── 15张 ─┐│
│  │ 30天前  ·  共 450MB         ││
│  │ [ 查看全部 → ]              ││
│  └────────────────────────────┘│
└──────────────────────────────────┘
```

### 交互细节

- 卡片点击 → 底部弹出详情（Bottom Sheet），可左右滑动切换
- 长按 → 多选模式，底部出现操作栏
- 左滑卡片 → 快速删除
- 截图预览支持双指缩放
- 清理按钮有波纹 + 成功动画

### 配色方案

- 跟随系统壁纸动态取色（Material You）
- 深色/浅色模式自动切换
- 数据可视化使用渐变色（蓝→紫→粉）

---

## 10. 权限与错误处理

### Android 权限模型

```kotlin
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

### 错误处理策略

```kotlin
sealed class AppError {
    data class PermissionDenied(val permission: String) : AppError()
    data class FileNotFound(val path: String) : AppError()
    data class InsufficientSpace(val required: Long) : AppError()
    data class ScanFailed(val reason: String) : AppError()
    data class TrashFull(val currentSize: Long, val maxSize: Long) : AppError()
}

class ErrorHandler {
    fun handle(error: AppError): UserMessage {
        return when (error) {
            is AppError.PermissionDenied -> "需要存储权限才能扫描文件"
            is AppError.FileNotFound -> "文件不存在：${error.path}"
            is AppError.InsufficientSpace -> "存储空间不足"
            is AppError.ScanFailed -> "扫描失败：${error.reason}"
            is AppError.TrashFull -> "回收站已满，请清理后重试"
        }
    }
}
```

### 边界情况处理

- 扫描中用户退出 → 协程自动取消
- 文件被占用 → 跳过并记录日志
- 回收站已满 → 提示用户扩容或清理
- 存储权限被拒 → 引导用户到系统设置

---

## 11. 广告设计

### 广告类型与触发时机

| 广告类型 | 触发时机 | 位置 |
|----------|----------|------|
| 启动广告 | App 冷启动 | 全屏，显示 3-5 秒可跳过 |
| Banner 广告 | 常驻 | 各页面底部，导航栏上方 |
| 插屏广告 | 操作完成后 | 扫描完成、清理完成、恢复文件后 |

### 各页面广告位布局

**启动广告（全屏）：**
```
┌──────────────────────────────────┐
│  ┌────────────────────────────┐  │
│  │                            │  │
│  │       广告内容              │  │
│  │                            │  │
│  └────────────────────────────┘  │
│                     [跳过 3s]    │
└──────────────────────────────────┘
```

**Banner 广告（各页面底部常驻）：**
```
┌──────────────────────────────────┐
│         页面内容                  │
├──────────────────────────────────┤
│  ┌────────────────────────────┐  │
│  │      Banner 广告           │  │
│  └────────────────────────────┘  │
│  ┌───┬───┬───┬───┐             │
│  │ 🏠│ 📸│ 🗑️│ ⚙️ │             │
│  └───┴───┴───┴───┘             │
└──────────────────────────────────┘
```

**插屏广告（操作完成后弹出）：**
```
┌──────────────────────────────────┐
│         清理完成！                │
│         已释放 2.3 GB            │
│  ┌────────────────────────────┐  │
│  │       插屏广告              │  │
│  └────────────────────────────┘  │
│           [继续]                 │
└──────────────────────────────────┘
```

### 广告 SDK 与聚合

使用 **AdMob + 穿山甲（CSJ）** 双 SDK，通过 AdMob Mediation 聚合管理。

```
┌─────────────────────────────────────┐
│           AdMob Mediation           │
│  ┌─────────────┐ ┌───────────────┐  │
│  │   AdMob     │ │   穿山甲 CSJ   │  │
│  │  (海外流量)  │ │  (国内流量)    │  │
│  └─────────────┘ └───────────────┘  │
│        ↓ 竞价/瀑布流 ↓               │
│        选择最优广告源填充             │
└─────────────────────────────────────┘
```

### 广告管理接口

```kotlin
class AdManager(
    private val admobProvider: AdMobProvider,
    private val csjProvider: CSJProvider,
    private val mediationConfig: MediationConfig
) {
    // 启动广告（优先 AdMob，穿山甲作为备用）
    fun showSplashAd(activity: Activity, onDismissed: () -> Unit)

    // Banner 广告
    fun loadBannerAd(adView: AdView)

    // 插屏广告（预加载，聚合竞价）
    fun preloadInterstitial()
    fun showInterstitialIfReady(activity: Activity, onDismissed: () -> Unit)
}

// 聚合配置
data class MediationConfig(
    val adMobAppId: String,
    val csjAppId: String,
    val waterfallOrder: List<AdSource>,  // 瀑布流顺序
    val biddingEnabled: Boolean = true   // 是否启用竞价
)

enum class AdSource {
    ADMOB, CSJ
}

enum class AdTrigger {
    SCAN_COMPLETE,      // 扫描完成
    CLEAN_COMPLETE,     // 清理完成
    RESTORE_COMPLETE,   // 恢复完成
    TRASH_EMPTIED       // 清空回收站
}
```

### 用户体验优化

- 插屏广告频率限制：同一操作 5 分钟内不重复展示
- 清理文件少于 10MB 时不弹插屏广告
- Banner 广告加载失败时隐藏，不显示空白占位

---

## 12. 多语言支持

支持 **中文（简体）** 和 **英文**，跟随系统语言自动切换，用户也可在设置中手动选择。

### 资源结构

```
app/src/main/res/
├── values/
│   └── strings.xml          # 默认（英文）
├── values-zh/
│   └── strings.xml          # 中文（简体）
```

### 实现方式

```kotlin
// 使用 Android 原生资源系统 + Compose
// strings.xml 中定义所有文案

// 示例 strings.xml (英文)
<string name="app_name">CleanMaster</string>
<string name="scan_button">Scan</string>
<string name="clean_button">Clean</string>
<string name="storage_used">Used: %1$s</string>
<string name="storage_available">Available: %1$s</string>
<string name="duplicates_found">Found %1$d duplicate groups</string>
<string name="trash_full">Trash is full, please clean up</string>

// 示例 strings.xml (中文)
<string name="app_name">截图管家</string>
<string name="scan_button">扫描</string>
<string name="clean_button">清理</string>
<string name="storage_used">已用：%1$s</string>
<string name="storage_available">可用：%1$s</string>
<string name="duplicates_found">发现 %1$d 组重复文件</string>
<string name="trash_full">回收站已满，请清理后重试</string>
```

### 语言切换

```kotlin
class LanguageManager(private val context: Context) {
    // 获取当前语言
    fun getCurrentLanguage(): Locale

    // 设置语言（保存到 SharedPreferences）
    fun setLanguage(locale: Locale)

    // 应用语言（Activity 重建）
    fun applyLanguage(activity: Activity)
}

// 设置页提供语言选择
enum class Language(val displayName: String, val locale: Locale) {
    SYSTEM("跟随系统", Locale.getDefault()),
    ENGLISH("English", Locale.ENGLISH),
    CHINESE("中文", Locale.CHINESE)
}
```

### 多语言覆盖范围

- 所有 UI 文案（按钮、标题、提示、错误信息）
- 通知文本（如有）
- 广告相关提示文案
- 设置项描述

---

## 13. 测试策略

使用 JUnit + Mockk 进行单元测试，覆盖核心逻辑。

### 关键测试用例

```kotlin
// 重复检测
@Test
fun `should detect exact duplicates by hash`()
@Test
fun `should group similar files by name and size first`()

// 截图分析
@Test
fun `should detect blurry screenshots`()
@Test
fun `should detect black screen screenshots`()
@Test
fun `should calculate pHash similarity correctly`()

// 回收站
@Test
fun `should move files to trash and restore`()
@Test
fun `should auto clean oldest when trash full`()

// 边界情况
@Test
fun `should handle empty directories`()
@Test
fun `should skip locked files gracefully`()
```
