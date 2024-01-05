import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    id("org.mozilla.rust-android-gradle.rust-android") version "0.9.3"
    kotlin("kapt")
    id("kotlin-parcelize")
//    id("com.google.gms.google-services")
}

android {
    setupCore()
    lint.disable += "RemoveWorkManagerInitializer"

    namespace = "me.offeex.exethirteen"
    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        targetSdk = 34
        applicationId = "me.offeex.exethirteen"
        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations.plus(listOf("en", "ru"))

        externalNativeBuild.ndkBuild {
            abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            arguments("-j${Runtime.getRuntime().availableProcessors()}")
        }

        kapt.arguments {
            arg("room.incremental", true)
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    externalNativeBuild.ndkBuild.path("src/main/jni/Android.mk")

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isPseudoLocalesEnabled = true
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependencies.add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.2")
}

cargo {
    module = "src/main/rust/shadowsocks-rust"
    libname = "sslocal"
    targets = listOf("arm", "arm64", "x86", "x86_64")
    profile = findProperty("CARGO_PROFILE")?.toString() ?: currentFlavor
    extraCargoBuildArguments = listOf("--bin", libname!!)
    featureSpec.noDefaultBut(
        arrayOf(
            "stream-cipher",
            "aead-cipher-extra",
            "logging",
            "local-flow-stat",
            "local-dns",
            "aead-cipher-2022",
        )
    )
    exec = { spec, toolchain ->
        run {
            try {
                Runtime.getRuntime().exec("python3 -V >/dev/null 2>&1")
                spec.environment("RUST_ANDROID_GRADLE_PYTHON_COMMAND", "python3")
                project.logger.lifecycle("Python 3 detected.")
            } catch (e: java.io.IOException) {
                project.logger.lifecycle("No python 3 detected.")
                try {
                    Runtime.getRuntime().exec("python -V >/dev/null 2>&1")
                    spec.environment("RUST_ANDROID_GRADLE_PYTHON_COMMAND", "python")
                    project.logger.lifecycle("Python detected.")
                } catch (e: java.io.IOException) {
                    throw GradleException("No any python version detected. You should install the python first to compile project.")
                }
            }
            spec.environment(
                "RUST_ANDROID_GRADLE_LINKER_WRAPPER_PY",
                "$projectDir/$module/../linker-wrapper.py"
            )
            spec.environment(
                "RUST_ANDROID_GRADLE_TARGET",
                "target/${toolchain.target}/$profile/lib$libname.so"
            )
        }
    }
}

tasks.configureEach {
    when (name) {
        "mergeDebugJniLibFolders", "mergeReleaseJniLibFolders" -> dependsOn("cargoBuild")
    }
}

tasks.register<Exec>("cargoClean") {
    executable("cargo")     // cargo.cargoCommand
    args("clean")
    workingDir("$projectDir/${cargo.module}")
}
tasks.clean.dependsOn("cargoClean")

dependencies {
    // :core 🤮🤮🤮🤮
    val coroutinesVersion = "1.6.4"
    val roomVersion = "2.5.0"
    val workVersion = "2.7.1"
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.work:work-multiprocess:$workVersion")
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("dnsjava:dnsjava:3.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // compose-destinations
    implementation("io.github.raamcosta.compose-destinations:core:1.8.42-beta")
    ksp("io.github.raamcosta.compose-destinations:ksp:1.8.42-beta")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(libs.lifecycle)
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    val appcompatVersion = "1.6.1"
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")

    // Debugging
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
