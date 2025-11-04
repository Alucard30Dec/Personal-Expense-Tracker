plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.gms.services) // ✅ THÊM DÒNG NÀY
}

android {
    namespace = "com.example.quanlychitieu"
    compileSdk = 34 // Dùng SDK 34 cho tương thích

    defaultConfig {
        applicationId = "com.example.quanlychitieu"
        minSdk = 24 // Hạ minSdk xuống 24 để tương thích rộng hơn
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.mpandroidchart)
    implementation(libs.play.services.auth)
    // Room dependencies
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}