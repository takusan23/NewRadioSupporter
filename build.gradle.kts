// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application").version("7.1.2").apply(false)
    id("com.android.library").version("7.1.2").apply(false)
    id("org.jetbrains.kotlin.android").version("1.6.20").apply(false)
}

buildscript {
    /** Kotlinのバージョン */
    val kotlinVersion by extra("1.6.20")

    /** Composeのバージョン */
    val composeVersion by extra("1.2.0-alpha08")
}

tasks.register("clean") {
    doFirst {
        delete(rootProject.buildDir)
    }
}