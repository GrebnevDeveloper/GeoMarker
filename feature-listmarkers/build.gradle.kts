plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.grebnev.feature.listmarkers"
    compileSdk =
        libs.versions.androidCompileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.androidMinSdk
                .get()
                .toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
}

dependencies {
    implementation(project(":core-domain"))
    implementation(project(":core-common"))
    implementation(project(":feature-geomarker-api"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.ui)
    implementation(libs.bundles.decompose)
    implementation(libs.bundles.mvikotlin)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}