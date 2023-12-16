import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.androidPlugin)
    implementation(libs.kotlinPlugin)
}