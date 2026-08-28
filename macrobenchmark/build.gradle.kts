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

        // CI runs macrobenchmark on an emulator against a debuggable build
        // (no physical device or signed release build available in CI), so
        // both of Macrobenchmark's default accuracy checks would otherwise
        // hard-fail every run. Suppressing them turns those into warnings
        // instead - the numbers are just not representative of a real
        // device/release build, which is an accepted tradeoff for CI.
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,DEBUGGABLE"
    }

    // Macrobenchmark needs to run against a build it can profile. Since this
    // project's CI only ever produces a debug build (no release signing is
    // configured), we benchmark the debug build directly rather than adding a
    // whole extra signed build type - self-instrumenting lets Macrobenchmark
    // treat an otherwise-debuggable build as profileable.
    //
    // IMPORTANT: this build type must NOT be named "release" - Gradle prefers
    // an exact build-type-name match over matchingFallbacks, so a build type
    // named "release" here would pull in app's real signed release build
    // (and fail looking for a signing keystore that doesn't exist in CI)
    // instead of falling back to "debug". Naming it "benchmark" guarantees
    // no exact match against :app's build types, so the fallback is used.
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

// Macrobenchmark tests only make sense against the debug-fallback (benchmark)
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
