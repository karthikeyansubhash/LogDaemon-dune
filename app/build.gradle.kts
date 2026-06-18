plugins {
    id("com.android.application")
}

android {
    namespace = "com.hp.jetadvantage.link.logdaemon"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.hp.jetadvantage.link.logdaemon"
        minSdk = 26
        targetSdk = 31
        versionCode = 20
        versionName = "1.00.20-s.7+D20260615"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "LogDaemon-dune")
    }
    
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("ForBuild/keys/platform.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }

        create("debug_sim") {
            storeFile = rootProject.file("ForBuild/keys/platform.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }

        create("release") { // Changed from getByName to create as 'release' was not found
            storeFile = rootProject.file("ForBuild/keys/platform.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }

        create("release_sim") {
            storeFile = rootProject.file("ForBuild/keys/platform.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }

    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }

        create("debug_sim") {
            signingConfig = signingConfigs.getByName("debug_sim")
        }

        create("release_sim") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release_sim")
        }
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        disable.add("ExpiredTargetSdkVersion")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")
    implementation("org.jdeferred:jdeferred-core:1.0.0")
    implementation("com.google.guava:guava:18.0")
    implementation("com.google.code.gson:gson:2.13.2")

    testImplementation("junit:junit:4.12")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("org.mockito:mockito-inline:4.8.0")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}
