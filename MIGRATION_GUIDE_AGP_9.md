# Migration Guide to Android Gradle Plugin 9.0

## 🧠 Introduction: Why Migrate to AGP 9.0?

Migrating to AGP 9.0 is a fundamental step to modernize the build process for Android projects. This version introduces:

- **"Built-in Kotlin"**: The Kotlin plugin is directly integrated into AGP, simplifying configuration.
- **New KMP Plugin for Android**: `com.android.kotlin.multiplatform.library` replaces the combination of Android + KMP plugins.
- **Better Performance**: Optimizations in the build process and resource generation.
- **Mandatory Structural Separation**: For KMP projects, the Android app code must be in a separate module.

## ✔️ Prerequisites

Before starting, make sure your environment meets the following minimum versions:

| Component | Minimum Version | Notes |
|-----------|-----------------|-------|
| **Android Gradle Plugin (AGP)** | `9.0.0` | Required for new APIs |
| **Gradle** | `9.1.0` | AGP 9.0 requires Gradle 9.1+ |
| **Kotlin Gradle Plugin (KGP)** | `2.2.10` | AGP 9.0 automatically upgrades to this version |
| **JDK** | `17` | Minimum required by AGP 9.0 |
| **Compose Compiler Plugin** | `2.0.0+` | Required when Compose is enabled |

---

## 🧱 1. Migrating an Android Project (non-KMP)

This section focuses on a standard Android app or library.

### Key Changes in AGP 9.0

1. **Built-in Kotlin:** AGP 9.0 includes the Kotlin plugin. You no longer need to apply `org.jetbrains.kotlin.android` manually.
2. **Compose Compiler Plugin Required:** With Kotlin 2.0+, you must apply `org.jetbrains.kotlin.plugin.compose` when using Compose.
3. **Variant API Deprecated:** The `variant.outputs` and `applicationVariants` APIs have been removed. Use `androidComponents.onVariants {}` instead.

### Migration Steps

1. **Update Gradle Wrapper:**
   
   In `gradle/wrapper/gradle-wrapper.properties`:
   ```properties
   distributionUrl=https\://services.gradle.org/distributions/gradle-9.1.0-bin.zip
   ```

2. **Update AGP in the Version Catalog:**
   
   In `gradle/libs.versions.toml`:
   ```toml
   [versions]
   agp = "9.0.0"
   ```

3. **Add Compose Compiler Plugin (if using Compose):**
   
   In the module's `build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.androidApplication)
       alias(libs.plugins.compose.compiler) // Required with Kotlin 2.0+
   }
   ```

4. **Enable Compose in the android block:**
   ```kotlin
   android {
       buildFeatures {
           compose = true
       }
   }
   ```

### Temporary Compatibility Options

If you encounter issues, you can use these flags in `gradle.properties`:
```properties
# Disable the new modular DSL (temporary, will be removed in AGP 10.0)
android.newDsl=false

# Disable the built-in Kotlin plugin if it causes conflicts
android.builtInKotlin=false

# Enable legacy variant APIs (temporary, will be removed in AGP 10.0)
android.enableLegacyVariantApi=true
```

> ⚠️ **Warning:** These flags are temporary and will be removed in AGP 10.0. Do not rely on them long-term.

---

## 🤝 2. Migrating a Kotlin Multiplatform (KMP) Project

For KMP projects with an Android target, AGP 9.0 requires mandatory structural separation.

### What Changes with AGP 9.0?

You **can no longer apply an Android plugin (`com.android.application` or `com.android.library`) and the KMP plugin (`org.jetbrains.kotlin.multiplatform`) in the same module**.

The official solution is:
1. Separate the Android application into its own module
2. Use the new `com.android.kotlin.multiplatform.library` plugin in the shared module

### Recommended Project Structure

```
project/
├── androidApp/                    # Android application module
│   ├── build.gradle.kts           # com.android.application
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/.../MainActivity.kt
│       └── res/
├── composeApp/                    # KMP shared module
│   ├── build.gradle.kts           # com.android.kotlin.multiplatform.library
│   └── src/
│       ├── androidMain/
│       ├── commonMain/
│       │   └── composeResources/  # Compose Multiplatform resources
│       └── iosMain/
└── iosApp/                        # iOS application
```

### Migration Steps

#### Step 1: Update the Version Catalog

In `gradle/libs.versions.toml`:
```toml
[versions]
agp = "9.0.0"
kotlin = "2.3.0"  # or compatible version

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidKotlinMultiplatformLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

#### Step 2: Update the Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    // ... other plugins
}
```

#### Step 3: Create the androidApp Module

Create `androidApp/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler) // Required for Compose
}

android {
    namespace = "com.example.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":composeApp"))
    
    // Compose dependencies for the Android module
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.12.2")
}
```

#### Step 4: Configure the Shared Module (composeApp)

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary) // New plugin
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

// Compose Multiplatform resources configuration
compose.resources {
    publicResClass = true
    packageOfResClass = "com.example.shared.resources"
    generateResClass = always
}

kotlin {
    applyDefaultHierarchyTemplate()
    
    // Android target configuration using the new DSL
    androidLibrary {
        namespace = "com.example.shared"
        compileSdk = 36
        minSdk = 24
        
        // ⚠️ IMPORTANT: Enable Android resources
        androidResources {
            enable = true
        }
        
        withHostTestBuilder { }
        withDeviceTestBuilder { }
    }

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Android-specific dependencies
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
        iosMain.dependencies {
            // iOS-specific dependencies
        }
    }
}
```

#### Step 5: Move Files to the androidApp Module

Move the following files from the shared module to the new `androidApp`:

| File/Directory | Source | Destination |
|----------------|--------|-------------|
| `MainActivity.kt` | `composeApp/src/androidMain/kotlin/...` | `androidApp/src/main/kotlin/...` |
| `AndroidManifest.xml` | `composeApp/src/androidMain/` | `androidApp/src/main/` |
| `res/` (Android resources) | `composeApp/src/androidMain/res/` | `androidApp/src/main/res/` |

#### Step 6: Simplify the Shared Module's AndroidManifest

In `composeApp/src/androidMain/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Empty manifest for library -->
</manifest>
```

#### Step 7: Update settings.gradle.kts

```kotlin
include(":composeApp")
include(":androidApp")
```

---

## 🎨 3. Handling Compose Multiplatform Resources

### Resource Configuration

Compose Multiplatform resources are placed in `commonMain/composeResources/`:

```
composeApp/src/commonMain/composeResources/
├── drawable/
│   └── icon.xml
├── values/
│   └── strings.xml
└── font/
    └── custom_font.ttf
```

### Required Configuration for Android

For resources to be properly packaged in the APK, **you must enable `androidResources`**:

```kotlin
kotlin {
    androidLibrary {
        // ⚠️ CRITICAL: Without this, resources won't be included in the APK
        androidResources {
            enable = true
        }
    }
}
```

### Using Resources in Code

```kotlin
import com.example.shared.resources.Res
import com.example.shared.resources.drawable.icon
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyIcon() {
    Image(
        painter = painterResource(Res.drawable.icon),
        contentDescription = null
    )
}
```

---

## 📊 4. Post-Migration Verification Checklist

### Build Verifications

- [ ] `./gradlew clean` runs without errors
- [ ] `./gradlew :androidApp:assembleDebug` generates APK correctly
- [ ] Compose resources are in the APK (`unzip -l app.apk | grep composeResources`)
- [ ] The app runs without resource errors at runtime

### Configuration Verifications

- [ ] Gradle wrapper updated to 9.1.0+
- [ ] AGP updated to 9.0.0
- [ ] `com.android.kotlin.multiplatform.library` plugin applied in shared module
- [ ] `androidResources { enable = true }` configured
- [ ] Compose Compiler Plugin applied in modules with Compose
- [ ] Unique namespaces for each module

### Best Practices

- [ ] Don't use temporary flags (`android.newDsl=false`, etc.) as a permanent solution
- [ ] Migrate from KAPT to KSP if applicable
- [ ] Run the complete test suite
- [ ] Verify CI/CD in a clean environment
- [ ] Create a Git branch before migrating for rollback

---

## 🔗 5. Official References

- [Android Gradle Plugin 9.0.0 Release Notes](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Set up the Android Gradle Library Plugin for KMP](https://developer.android.com/kotlin/multiplatform/plugin)
- [Updating multiplatform projects with Android apps to use AGP 9](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html)
- [Compose Multiplatform Resources Setup](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html)
- [Migrate to Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)

---

## ⚠️ 6. Common Issues and Solutions

### Error: "Missing resource with path: composeResources/..."

**Cause:** Compose resources are not being packaged in the APK.

**Solution:** Make sure you have `androidResources { enable = true }` in the `androidLibrary` block:
```kotlin
androidLibrary {
    androidResources {
        enable = true
    }
}
```

### Error: "Compose Compiler Gradle plugin is required"

**Cause:** AGP 9.0 with Kotlin 2.0+ requires the Compose Compiler plugin explicitly.

**Solution:** Add the plugin in modules that use Compose:
```kotlin
plugins {
    alias(libs.plugins.compose.compiler)
}
```

### Error: "Minimum supported Gradle version is 9.1"

**Cause:** AGP 9.0 requires Gradle 9.1.0 minimum.

**Solution:** Update `gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.1.0-bin.zip
```

### Error: "Namespace is used in multiple modules"

**Cause:** The shared module and app namespaces are the same.

**Solution:** Use unique namespaces:
```kotlin
// In composeApp (library)
androidLibrary {
    namespace = "com.example.shared"
}

// In androidApp (application)
android {
    namespace = "com.example.app"
}
```

### Error: "Unresolved reference" in MainActivity

**Cause:** Compose dependencies are not available in the androidApp module.

**Solution:** Add Compose dependencies to the androidApp module:
```kotlin
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
}
```
