import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Load OneStep credentials from the gitignored local.properties so no keys live in source.
// Missing keys fall back to empty strings, so the project still compiles on a fresh clone.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun localProperty(key: String): String = localProperties.getProperty(key, "")

android {
    namespace = "com.onestep.quickstart"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.onestep.quickstart"
        minSdk = 26 // Min SDK version required to use OneStepSDK
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // OneStep credentials sourced from local.properties (never committed).
        buildConfigField("String", "CLIENT_TOKEN", "\"${localProperty("onestep.clientToken")}\"")
        buildConfigField("String", "CUSTOMER_PATIENT_ID", "\"${localProperty("onestep.customerPatientId")}\"")
        buildConfigField("String", "IDENTITY_VERIFICATION", "\"${localProperty("onestep.identityVerification")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Signed with the debug key so the minified release build installs locally for
            // testing. Use your own signing config in a real app.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.oneStep.sdk)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
}
