plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

apply(from = rootProject.file("config/common-android-library.gradle"))
apply(from = rootProject.file("config/uses-compose.gradle"))

android.namespace = "de.lexware.viewer"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":data:fetcher"))

    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.android)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}