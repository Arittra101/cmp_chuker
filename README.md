Kotlin Multiplatform project (Android + iOS) — network inspector SDK.

## Modules

| Gradle path | Folder | Description |
|-------------|--------|-------------|
| `:shared` | `chucker-sdk/` | Shared KMP library (SQLDelight, Compose UI, Ktor plugin) |
| `:sample-app` | `sample-app/` | Android sample app |

> The library module is **`chucker-sdk`** on disk; Gradle exposes it as **`:shared`** so commands match the usual KMP template.

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

## Tests

```bash
./gradlew :shared:testDebugUnitTest
./gradlew :shared:iosSimulatorArm64Test
```
