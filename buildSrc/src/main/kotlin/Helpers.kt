import com.android.build.api.dsl.*
import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

private val flavorRegex = "(assemble|generate)\\w*(Release|Debug)".toRegex()
val Project.currentFlavor get() = gradle.startParameter.taskRequests.toString().let { task ->
    flavorRegex.find(task)?.groupValues?.get(2)?.lowercase() ?: "debug".also {
        println("Warning: No match found for $task")
    }
}

fun CommonExtension<*, *, *, *, *>.setupCore() {
    compileSdk = 34
    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        compileOptions.isCoreLibraryDesugaringEnabled = true
    }
    lint.apply {
        warning += "ExtraTranslation"
        warning += "ImpliedQuantity"
        warning += "RestrictedApi"
        informational += "MissingQuantity"
        informational += "MissingTranslation"
        disable += "BadConfigurationProvider"
        disable += "UseAppTint"
    }
    ndkVersion = "25.1.8937393"
}