plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
val uiSmoke = providers.gradleProperty("uiSmoke").orNull == "true"
android {
    namespace = "br.local.busca"
    compileSdk { version = release(37) { minorApiLevel = 2 } }
    defaultConfig { applicationId = "br.local.busca"; minSdk = 30; targetSdk = 35; versionCode = 6; versionName = "0.3.2"; ndk { abiFilters += if (uiSmoke) "x86_64" else "arm64-v8a" } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    packaging { jniLibs { useLegacyPackaging = true } }
}
dependencies {
    implementation("info.guardianproject:tor-android:0.4.9.17")
    implementation("info.guardianproject:jtorctl:0.4.5.7")
    implementation(project(":lib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.19.1")
}
