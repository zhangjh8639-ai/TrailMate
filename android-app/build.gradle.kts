import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.trailmate.app"
    compileSdk = 36
    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(::load)
        }
    }
    val localAmapApiKey = localProperties.getProperty("TRAILMATE_AMAP_API_KEY").orEmpty()
    val localTrailMateServerBaseUrl = localProperties.getProperty("TRAILMATE_SERVER_BASE_URL").orEmpty()
    val localWechatAppId = localProperties.getProperty("TRAILMATE_WECHAT_APP_ID").orEmpty()
    val amapApiKey = providers.gradleProperty("TRAILMATE_AMAP_API_KEY")
        .orElse(providers.environmentVariable("TRAILMATE_AMAP_API_KEY"))
        .orElse(localAmapApiKey)
        .get()
    val trailMateServerBaseUrl = providers.gradleProperty("TRAILMATE_SERVER_BASE_URL")
        .orElse(providers.environmentVariable("TRAILMATE_SERVER_BASE_URL"))
        .orElse(localTrailMateServerBaseUrl)
        .get()
    val wechatAppId = providers.gradleProperty("TRAILMATE_WECHAT_APP_ID")
        .orElse(providers.environmentVariable("TRAILMATE_WECHAT_APP_ID"))
        .orElse(localWechatAppId)
        .get()
    val escapedAmapApiKey = amapApiKey
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    val escapedTrailMateServerBaseUrl = trailMateServerBaseUrl
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    val escapedWechatAppId = wechatAppId
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

    defaultConfig {
        applicationId = "com.trailmate.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["AMAP_API_KEY"] = amapApiKey
        buildConfigField("String", "TRAILMATE_AMAP_API_KEY", "\"$escapedAmapApiKey\"")
        buildConfigField("String", "TRAILMATE_SERVER_BASE_URL", "\"$escapedTrailMateServerBaseUrl\"")
        buildConfigField("String", "TRAILMATE_WECHAT_APP_ID", "\"$escapedWechatAppId\"")
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.amap.map.bundle)
    implementation(libs.maplibre.android.sdk)
    implementation(libs.wechat.sdk.android)

    testImplementation(libs.json.java)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
