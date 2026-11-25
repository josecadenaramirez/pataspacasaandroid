plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

val hasQaGoogleServices = file("src/qa/google-services.json").isFile

android {
    namespace = "com.cadena.pataspacasa"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cadena.pataspacasa"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_VERSION", "\"v1\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create("qa") {
            dimension = "environment"
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            buildConfigField("String", "APP_ENVIRONMENT", "\"QA\"")
            buildConfigField("String", "API_BASE_URL", "\"https://qa.pataspacasa.com/api\"")
            buildConfigField("String", "PUBLIC_WEB_BASE_URL", "\"https://qa.pataspacasa.com\"")
            buildConfigField("boolean", "FIREBASE_ENABLED", hasQaGoogleServices.toString())
            resValue("string", "app_name", "Patas Pa’ Casa QA")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "APP_ENVIRONMENT", "\"PROD\"")
            buildConfigField("String", "API_BASE_URL", "\"https://pataspacasa.com/api\"")
            buildConfigField("String", "PUBLIC_WEB_BASE_URL", "\"https://pataspacasa.com\"")
            buildConfigField("boolean", "FIREBASE_ENABLED", "true")
        }
    }

    buildTypes {
        debug { }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    beforeVariants(
        selector().withBuildType("release").withFlavor("environment" to "qa")
    ) { variantBuilder ->
        variantBuilder.enable = false
    }
}

// QA needs its own Firebase Android app because it has an applicationId suffix.
// Until that real configuration is added, keep Google Services from generating
// production Firebase resources into the QA variant.
if (!hasQaGoogleServices) {
    tasks.configureEach {
        if (name.startsWith("processQa") && name.endsWith("GoogleServices")) {
            enabled = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.splashscreen)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics.ktx)

    // Image loading
    implementation(libs.coil.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
