// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.google.services)
    }
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven (url = "https://maven.google.com" )
    }
}


tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
