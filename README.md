Kotlin Multiplatform project (Android + iOS) — network inspector SDK.

## Modules

| Gradle path | Folder | Description |
|-------------|--------|-------------|
| `:shared` | `chucker-sdk/` | Shared KMP library (Room KMP, Compose UI, Ktor plugin) |
| `:sample-app` | `sample-app/` | Compose Multiplatform sample app — shared UI in `commonMain`, thin Android + iOS entry points |

> The library module is **`chucker-sdk`** on disk; Gradle exposes it as **`:shared`** so commands match the usual KMP template.

The sample is the integration reference — it shows exactly what a host app has to write:

```
sample-app/src/
  commonMain/…/SampleApp.kt        ← all the UI, incl. CmpChuckerOverlay()
  androidMain/…/MainApplication.kt ← CmpChucker.init(this)
  androidMain/…/MainActivity.kt    ← setContent { SampleApp() }
  iosMain/…/MainViewController.kt  ← CmpChucker.init(); ComposeUIViewController { SampleApp() }
```

Only `init` is platform-specific — Android has an `Application`, iOS does not, so each entry point does it.

## Build commands

```bash
# Android library (AAR)
./gradlew :shared:assembleDebug

# Android sample APK
./gradlew :sample-app:assembleDebug

# Publish everything (Android + iOS) — bump libraryVersion in gradle.properties first
./gradlew publish

# Android-only host apps (skips iOS uploads)
./gradlew :shared:publishAndroid
```

### `409 Conflict` on publish

GitHub Packages **rejects re-uploading the same version**. Each target is versioned separately (`shared-iosarm64`, `shared`, etc.).

If publish fails partway, the first targets (often **iOS**) may already be uploaded. **Do not run `publish` again with the same version.**

1. Open `gradle.properties` and bump **`libraryVersion`** (e.g. `1.0.1` → `1.0.2`).
2. Run `./gradlew publish` **once** with the new version.

If only Android is missing and iOS for that version is already on GitHub:

```bash
./gradlew :shared:publishAndroid
```

To confirm what is already published: GitHub → repo **cmp_chuker** → **Packages**.

## iOS

- Open `iosApp/` in Xcode and run the app.
- iOS requires **Kotlin 2.3+** with Compose Multiplatform 1.11 (configured in `gradle/libs.versions.toml`).
- `iosApp` is a thin Swift shell. It builds **`:sample-app`**'s framework (`SampleApp`), not the SDK's:
  - build phase → `./gradlew :sample-app:embedAndSignAppleFrameworkForXcode`
  - `FRAMEWORK_SEARCH_PATHS` → `sample-app/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
  - `OTHER_LDFLAGS` → `-framework SampleApp`
  - `ContentView.swift` → `import SampleApp`, wrapping `MainViewControllerKt.MainViewController()`
- The frameworks are **static** (`isStatic = true`), so the app does the final link. No `-lsqlite3` is needed: `androidx.sqlite:sqlite-bundled` compiles SQLite into the binary.
- The SDK's own framework is named **`ChuckerSdk`** (`baseName` in `chucker-sdk/build.gradle.kts`) — relevant only if you consume it directly from Swift.

## Persistence (Room KMP)

Transactions are stored with **Room 2.8.5**, one schema for both platforms:

- `data/db/HttpTransaction.kt` — `@Entity(tableName = "http_transactions")`, also the model the UI renders
- `data/db/ChuckerDao.kt` — `@Dao`; `selectAll()` returns a `Flow`, so the inspector list updates live
- `data/db/ChuckerDatabase.kt` — `@Database` + `@ConstructedBy(ChuckerDatabaseConstructor::class)`; Room's KSP processor generates the `actual` object per target
- `data/db/DatabaseBuilderFactory.*.kt` — the only platform-specific piece: Android resolves `getDatabasePath()`, iOS resolves `NSDocumentDirectory`
- `di/ChuckerModule.kt` — builds the database with `BundledSQLiteDriver()` (same driver on Android and iOS) and `Dispatchers.IO`

Room's compiler runs per KMP target, so the SDK's `build.gradle.kts` wires it up three times:

```kotlin
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
```

Schemas are exported to `chucker-sdk/schemas/` — commit them, and bump `version` in `@Database` whenever the table changes.

**Upgrading from an SQLDelight build (≤ 1.0.2):** the database file is now `chucker_room.db`, not `chucker.db`. Room would fail its identity check on the old file and crash the host app, so the names are kept apart; the stale `chucker.db` is simply orphaned and can be deleted. The builder also uses `fallbackToDestructiveMigration(dropAllTables = true)` — captured traffic is disposable, so a schema change wipes the inspector rather than blocking the host app.

## Host app integration (Android)

### 1. Dependency

```kotlin
// settings.gradle.kts — GitHub Packages repo + credentials
// build.gradle.kts
dependencies {
    implementation("com.arittra101:shared:1.0.2")
    implementation("io.ktor:ktor-client-cio:3.5.0") // engine (required; pick cio/darwin/etc.)
}
```

When developing this repo locally: `implementation(projects.shared)`.

### 2. Application — init DB / Koin

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CmpChucker.init(this)
    }
}
```

Register in `AndroidManifest.xml`: `android:name=".MyApp"`.

If you already use Koin:

```kotlin
startKoin {
    androidContext(this@MyApp)
    modules(appModule, CmpChucker.koinModule(this@MyApp))
}
```

### 3. HTTP client — use the SDK client factory

```kotlin
import org.example.scol_chuker.createChuckerHttpClient

private val httpClient = createChuckerHttpClient()
// InspectorPlugin is installed automatically
```

Or manually: `HttpClient { install(org.example.scol_chuker.plugin.InspectorPlugin) }`.

**Important:** `CmpChucker.init()` must run before the first request, or Koin will not be ready.

### 4. UI — floating inspector button

Put it in your root composable — in a Compose Multiplatform app that is `commonMain`, so one line covers both platforms:

```kotlin
setContent {
    MyTheme {
        MyScreen()
        CmpChuckerOverlay()
    }
}
```

### 5. Manifest

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Host app integration (iOS)

Same three steps as Android — only `init` differs, because there is no `Context`.

```kotlin
// iosMain — see sample-app/src/iosMain/…/MainViewController.kt
fun MainViewController(): UIViewController {
    CmpChucker.init()                 // ← must run before the first request
    return ComposeUIViewController {
        App()                         // your shared UI, with CmpChuckerOverlay() inside
    }
}
```

HTTP client is identical: `createChuckerHttpClient()`. Add a Ktor engine in `iosMain`: `implementation("io.ktor:ktor-client-darwin:…")`.

If you already use Koin:

```kotlin
startKoin { modules(appModule, CmpChucker.koinModule()) }
```

`CmpChucker.init()` is idempotent and loads into an existing Koin if the host already started one.

**Swift-only hosts:** the SDK's API surface is Compose + Ktor types, which are not exported to Objective-C — drive it from Kotlin and hand Swift a `UIViewController`, as `:sample-app` does.

## Tests

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :shared:iosSimulatorArm64Test
```
