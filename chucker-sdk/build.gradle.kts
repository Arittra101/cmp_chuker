import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    id("maven-publish")  // ✅ here
}

group = "com.arittra101"
version = findProperty("libraryVersion")?.toString() ?: "1.0.2"

val localProps = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

sqldelight {
    databases {
        create("ChuckerDatabase") {
            packageName.set("org.example.scolchuker")
        }
    }
}

android {
    namespace = "org.example.scol_chuker.chucker_sdk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        // ✅ tell KMP what to publish
        publishLibraryVariants("release")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ChuckerSdk"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.sqldelight.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native)
        }
        commonMain.dependencies {
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            api(libs.ktor.client.core)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// ✅ publishing block MUST come AFTER kotlin {} block
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Arittra101/cmp_chuker")
            credentials {
                username = localProps["github.username"] as String
                password = localProps["github.token"] as String
            }
        }
    }
}

/** Android + KMP metadata only — use when iOS artifacts for this version are already on GitHub. */
tasks.register("publishAndroid") {
    group = "publishing"
    description = "Publish androidRelease + kotlinMultiplatform to GitHub Packages (skips iOS)"
    dependsOn(
        "publishKotlinMultiplatformPublicationToGitHubPackagesRepository",
        "publishAndroidReleasePublicationToGitHubPackagesRepository",
    )
}

/*tasks.register("publishIos") {
    group = "publishing"
    description = "Publish iOS artifacts to GitHub Packages (skips Android)"
    dependsOn(
        "publishKotlinMultiplatformPublicationToGitHubPackagesRepository",
        "publishIosArm64PublicationToGitHubPackagesRepository",
        "publishIosSimulatorArm64PublicationToGitHubPackagesRepository",
    )
}*/

//    ./gradlew publishAndroidAndroid AAR + KMP metadata
//    ./gradlew publishIosiOS frameworks + KMP metadata
//    ./gradlew publishEverything (Android + iOS)