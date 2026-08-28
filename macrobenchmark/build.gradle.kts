plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.example.macrobenchmark"
    compileSdk { version = release(36) { minorApiLevel = 1 } }

    defaultConfig {
        minSdk = 24
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Macrobenchmark needs to run against a build it can profile. Since this
    // project's CI only ever produces a debug build (no release signing is
    // configured), we benchmark the debug build directly rather than adding a
    // whole extra signed "benchmark" build type - self-instrumenting lets
    // Macrobenchmark treat an otherwise-debuggable build as profileable.
    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("debug")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Macrobenchmark tests only make sense against the release-fallback (debug)
// variant - this skips generating any other build variant for this module.
androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
