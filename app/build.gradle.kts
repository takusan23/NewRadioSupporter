plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
}

android {
    namespace = "io.github.takusan23.newradiosupporter"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.takusan23.newradiosupporter"
        minSdk = 30
        targetSdk = 37
        versionCode = 21
        versionName = "5.0.0 Beta 5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    implementation("androidx.glance:glance-appwidget:1.2.0")
    implementation("androidx.glance:glance-material3:1.2.0")

    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // navigation3
    implementation("androidx.navigation3:navigation3-runtime:1.1.3")
    implementation("androidx.navigation3:navigation3-ui:1.1.3")
    // navigation3 で kotlinx.serialization 使うので
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    // collectAsStateWithLifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // てすとこーど
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("io.mockk:mockk:1.13.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}