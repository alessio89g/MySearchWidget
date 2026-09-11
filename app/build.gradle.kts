plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.plugin.compose")
 id("org.jetbrains.kotlin.plugin.serialization")
}
android {
 namespace = "com.alessio89g.mysearchwidget"
 compileSdk = 37
 defaultConfig {
  applicationId = "com.alessio89g.mysearchwidget"
  minSdk = 31 // Material You requires Android 12; the input technique does not.
  targetSdk = 37
  versionCode = 11
  versionName = "1.6.1"
  testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 }
 buildFeatures { compose = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildTypes { release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt")) } }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2025.08.01"))
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.compose.material3:material3:1.5.0-alpha03")
 implementation("androidx.compose.material:material-icons-core")
 implementation("androidx.datastore:datastore-preferences:1.1.7")
 implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
 testImplementation("junit:junit:4.13.2")
 androidTestImplementation("androidx.test:runner:1.6.2")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
