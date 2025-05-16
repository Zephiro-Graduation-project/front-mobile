plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.frontzephiro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.frontzephiro"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        jvmTarget = "11"
    }


}

dependencies {

    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.github.tehras:charts:0.2.2-alpha")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //Implementation para que los lotti funcionen
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Implementation para hacer llamadas al al backend (Retrofit)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // Para manejar logs (opcional)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    // Para manejar fechas con LocalDate en JSON
    implementation ("com.google.code.gson:gson:2.10.1")
    //Implementacion para materialDesign
    implementation ("com.google.android.material:material:1.11.0")
    //Implementacion para poder leer token JWT
    implementation ("com.auth0.android:jwtdecode:2.0.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")

    implementation("com.github.prolificinteractive:material-calendarview:1.4.3")

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.core:core:1.10.1")

    // Jetpack Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.runtime.livedata)
    //para que abra el google chrome
    implementation(libs.androidx.browser)

// Solo para desarrollo
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}