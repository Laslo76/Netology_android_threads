import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "ru.netology.nmedia"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.netology.nmedia"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["usesCleartextTraffic"] = false
        }
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = true
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildToolsVersion = "30.0.2"
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.maps)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.gson)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room)
    ksp(libs.androidx.room.compiler)
    implementation(platform(libs.firebase))
    implementation(libs.firebase.messaging)
    implementation(libs.play.services)
    implementation(libs.okhttp)
    coreLibraryDesugaring(libs.desugaring)
    implementation(libs.glide)
    implementation(libs.androidx.exifinterface)
    //annotationProcessor(libs.compiler)
    //implementation("com.squareup.picasso:picasso:2.8")
    //implementation(libs.coil)
    //implementation(libs.coil.view)
}