plugins {
    id(Plugins.ANDROID_LIBRARY_PLUGIN)
    id(Plugins.KOTLIN_ANDROID_PLUGIN)
//    id(Plugins.KOTLIN_ANDROID_EXTENSIONS_PLUGIN)
    id(Plugins.JETBRAINS_KOTLIN)
    id(Plugins.KOTLIN_KAPT_PLUGIN)
    id(Plugins.KOTLIN_PARCELIZE_PLUGIN)
}

android {

    defaultConfig {
        compileSdk = AndroidVersion.COMPILE_SDK_VERSION
        minSdk = AndroidVersion.MIN_SDK_VERSION
        targetSdk = AndroidVersion.TARGET_SDK_VERSION
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "YANDEX_MAP_KEY", "\"key\"")

        }

        debug {
            isMinifyEnabled = false

        }
        create("stage") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    packagingOptions {
        jniLibs {
            excludes += setOf("META-INF/licenses/**")
        }
        resources {
            excludes += setOf(
                "**/attach_hotspot_windows.dll",
                "META-INF/licenses/**",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }


    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    namespace = "com.nauchat.core"

}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

}
