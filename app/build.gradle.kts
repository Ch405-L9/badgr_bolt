import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics.plugin) apply false
}

// Firebase config is intentionally NOT committed.
// Only apply Google Services / Crashlytics plugins when `app/google-services.json` exists,
// so fresh clones can still sync/build without private config files.
val hasGoogleServicesJson = file("google-services.json").exists()
if (hasGoogleServicesJson) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// Load keystore credentials from keystore.properties (gitignored — never committed)
val keystoreProps = Properties().also { props ->
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { props.load(it) }
}

val releaseKeystorePath = keystoreProps.getProperty("storeFile") ?: "../badgr_release.jks"
val releaseKeystoreFile = rootProject.file(releaseKeystorePath).takeIf { it.exists() }
val releaseStorePassword =
    keystoreProps.getProperty("storePassword")
        ?: providers.gradleProperty("STORE_PASSWORD").orNull
        ?: System.getenv("STORE_PASSWORD")
        ?: ""
val releaseKeyPassword =
    keystoreProps.getProperty("keyPassword")
        ?: providers.gradleProperty("KEY_PASSWORD").orNull
        ?: System.getenv("KEY_PASSWORD")
        ?: ""
val hasReleaseSigning =
    releaseKeystoreFile != null && releaseStorePassword.isNotBlank() && releaseKeyPassword.isNotBlank()

android {
    namespace  = "com.badgr.orbreader"
    compileSdk = 35

    defaultConfig {
        applicationId  = "com.badgr.orbreader"
        minSdk         = 26
        targetSdk      = 35
        versionCode    = 26
        versionName    = "3.2.6"

        buildConfigField(
            "String",
            "BACKEND_BASE_URL",
            "\"https://badgr-text-service.onrender.com\""
        )
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystoreFile
                storePassword = releaseStorePassword
                keyAlias = keystoreProps.getProperty("keyAlias") ?: "badgr_bolt"
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        release {
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                // Allows `assembleRelease` to run for local/dev validation without private keystore material.
                signingConfigs.getByName("debug")
            }
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.google.material)

    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.coil.compose)

    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.billing)
}
