plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.mihnati.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mihnati.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "2.4.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        try {
            val method = variantBuilder.javaClass.methods.firstOrNull { it.name == "setEnableUnitTest" || it.name == "setUnitTestEnabled" }
            method?.invoke(variantBuilder, true)
        } catch (_: Exception) {}
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Google ML Kit On-Device Translation
    implementation("com.google.mlkit:translate:17.0.3")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    // Firebase BOM & Cloud Services
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // Google Play Services Auth (Real Google Sign-In)
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
