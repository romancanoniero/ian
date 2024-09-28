buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id ("org.sonarqube") version "4.4.1.3373"
}

sonar {
    properties {
        property("sonar.projectName", "IAN")
        property("sonar.projectKey", "IAN")
        property("sonar.language", "kotlin")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.host.url", "http://localhost:9000/")
        property("sonar.token", "sqp_9827a2e7f86fa64893a12358b3511616dda51b0d")
    }
}