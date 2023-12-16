plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
}

android {
    setupCore()

    namespace = "me.offeex.exethirteen"
    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        targetSdk = 34
        applicationId = "me.offeex.exethirteen"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

dependencies {
    implementation(project(":core"))

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

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debugging
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
