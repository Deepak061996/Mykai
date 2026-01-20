// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    dependencies {
//        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("com.android.tools.build:gradle:8.6.0")

        classpath ("com.google.firebase:firebase-crashlytics-gradle:3.0.2")
    }
}

plugins {
    id("com.android.library") version "7.3.1" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
//    id("com.android.application") version "8.2.2" apply false
    id("com.android.application") version "8.6.0" apply false

    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}