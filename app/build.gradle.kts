plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.johnsamte.pathianlaisiangthou"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.johnsamte.pathianlaisiangthou"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "5.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation ("com.larswerkman:HoloColorPicker:1.5")
    implementation(libs.androidx.core.splashscreen)

    implementation("com.github.skydoves:colorpickerview:2.4.0")


}