object Deps {

    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib:${PluginVersion.KOTLIN_VERSION}"

    const val ANDROIDX_CORE_KTX = "androidx.core:core-ktx:${Version.CORE_KTX_VERSION}"

    // Activity compose
    const val ANDROIDX_ACTIVITY_COMPOSE =
        "androidx.activity:activity-compose:${Version.ACTIVITY_COMPOSE_VERSION}"

    // Activity
    const val ANDROIDX_ACTIVITY_KTX =
        "androidx.activity:activity-ktx:${Version.ACTIVITY_KTX_VERSION}"

    // Fragments
    const val ANDROIDX_FRAGMENT_KTX =
        "androidx.fragment:fragment-ktx:${Version.FRAGMENT_KTX_VERSION}"

    // AppCompat
    const val APPCOMPAT = "androidx.appcompat:appcompat:${Version.APPCOMPAT_VERSION}"

    // Material
    const val MATERIAL = "com.google.android.material:material:${Version.MATERIAL_VERSION}"


    const val VK_CORE = "com.vk:android-sdk-core:${Version.VK_CORE}"
    const val VK_API = "com.vk:android-sdk-api:${Version.VK_API}"

    // ConstraintLayout
    const val CONSTRAINT_LAYOUT =
        "androidx.constraintlayout:constraintlayout:${Version.CONSTRAINT_LAYOUT_VERSION}"
    const val CONSTRAINT_LAYOUT_COMPOSE =
        "androidx.constraintlayout:constraintlayout-compose:${Version.CONSTRAINT_LAYOUT_COMPOSE_VERSION}"

    const val OPEN_STREET_MAP = "org.osmdroid:osmdroid-android:${Version.OPEN_STREET_MAP_VERSION}"


    const val DOTS_INDICATOR =
        "com.github.zhpanvip:viewpagerindicator:${Version.DOTS_INDICATOR_VERSION}"

    const val TIMBER = "com.jakewharton.timber:timber:${Version.TIMBER_VERSION}"

    const val TEMPO = "com.github.cesarferreira:tempo:${Version.TEMPO_VERSION}"


    const val SOCKETIO = "io.socket:socket.io-client:${Version.SOCKETIO_VERSION}"

    // RecyclerView
    const val RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Version.RECYCLER_VIEW_VERSION}"

    // ViewPager2
    const val VIEWPAGER2 = "androidx.viewpager2:viewpager2:${Version.VIEWPAGER2_VERSION}"

    // SwipeRefreshLayout
    const val SWIPE_REFRESH_LAYOUT =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Version.SWIPE_REFRESH_LAYOUT_VERSION}"


    //Firebase
    const val FIREBASE_PLATFORM_BOM =
        "com.google.firebase:firebase-bom:${Version.FIREBASE_PLATFORM_BOM_VERSION}"

    const val FIREBASE_MESSAGE =
        "com.google.firebase:firebase-messaging:${Version.FIREBASE_MESSAGE}"

    const val FIREBASE_CRASH =
        "com.google.firebase:firebase-crashlytics:${Version.FIREBASE_CRASH}"

    const val FIREBASE_AUTH =
        "com.google.firebase:firebase-auth-ktx:${Version.FIREBASE_AUTH_VERSION}"

    const val FIREBASE_ANALYTICS = "com.google.firebase:firebase-analytics-ktx"

    // Lifecycle, ViewModel and LiveData

    // ViewModel
    const val LIFECYCLE_VIEWMODEL_KTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.LIFECYCLE_VERSION}"

    // LiveData
    const val LIFECYCLE_LIVEDATA_KTX =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Version.LIFECYCLE_VERSION}"


    const val LIFECYCLE_RUNTIME_KTX =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Version.LIFECYCLE_VERSION_KTX}"

    // Saved state module for ViewModel
    const val LIFECYCLE_VIEWMODEL_SAVEDSTATE =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Version.LIFECYCLE_VERSION}"

    // Annotation processor
//    const val LIFECYCLE_COMPILER = "androidx.lifecycle:lifecycle-compiler:${Version.LIFECYCLE_VERSION}"

    // alternately - if using Java8, use the following instead of lifecycle-compiler
    const val LIFECYCLE_COMMON_JAVA8 =
        "androidx.lifecycle:lifecycle-common-java8:${Version.LIFECYCLE_VERSION}"

    // optional - helpers for implementing LifecycleOwner in a Service
    const val LIFECYCLE_SERVICE =
        "androidx.lifecycle:lifecycle-service:${Version.LIFECYCLE_VERSION}"

    // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
    const val LIFECYCLE_PROCESS =
        "androidx.lifecycle:lifecycle-process:${Version.LIFECYCLE_VERSION}"

    // optional - ReactiveStreams support for LiveData
    const val LIFECYCLE_REACTIVE_STREAMS =
        "androidx.lifecycle:lifecycle-reactivestreams:${Version.LIFECYCLE_VERSION}"


    // Navigation Components
    const val NAVIGATION_FRAGMENT =
        "androidx.navigation:navigation-fragment-ktx:${Version.NAVIGATION_VERSION}"
    const val NAVIGATION_UI = "androidx.navigation:navigation-ui-ktx:${Version.NAVIGATION_VERSION}"
    const val NAVIGATION_RUNTIME =
        "androidx.navigation:navigation-runtime-ktx:${Version.NAVIGATION_VERSION}"

    // Dynamic Feature Module Support
    const val NAVIGATION_DYNAMIC =
        "androidx.navigation:navigation-dynamic-features-fragment:${Version.NAVIGATION_VERSION}"

    // Dagger Hilt
    const val DAGGER_HILT_ANDROID = "com.google.dagger:hilt-android:${Version.DAGGER_VERSION}"
    const val DAGGER_HILT_COMPILER =
        "com.google.dagger:hilt-android-compiler:${Version.DAGGER_VERSION}"

    const val DAGGER_HILT_NAVIGATION_COMPOSE =
        "androidx.hilt:hilt-navigation-compose:${Version.DAGGER_NAVIGATION_COMPOSE}"


    // RxJava3
    const val RX_JAVA3 = "io.reactivex.rxjava3:rxjava:${Version.RX_JAVA3_VERSION}"

    // RxJava3 Android
    const val RX_JAVA3_ANDROID =
        "io.reactivex.rxjava3:rxandroid:${Version.RX_JAVA3_ANDROID_VERSION}"

    // Coroutines
    const val COROUTINES_CORE =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.COROUTINES_VERSION}"
    const val COROUTINES_ANDROID =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.COROUTINES_VERSION}"

    // Retrofit
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${Version.RETROFIT_VERSION}"
    const val RETROFIT_GSON_CONVERTER =
        "com.squareup.retrofit2:converter-gson:${Version.RETROFIT_VERSION}"
    const val RETROFIT_RX_JAVA2_ADAPTER = "com.jakewharton.retrofit:retrofit2-rxjava2-adapter:1.0.0"
    const val RETROFIT_RX_JAVA3_ADAPTER =
        "com.squareup.retrofit2:adapter-rxjava3:${Version.RETROFIT_RXJAVA3_ADAPTER}"

    const val OK_HTTP3 = "com.squareup.okhttp3:okhttp:${Version.OK_HTTP3_VERSION}"

    // Gson
    const val GSON = "com.google.code.gson:gson:${Version.GSON_VERSION}"

    // Room
    const val ROOM_RUNTIME = "androidx.room:room-runtime:${Version.ROOM_VERSION}"

    // For Kotlin use kapt instead of annotationProcessor
    const val ROOM_COMPILER = "androidx.room:room-compiler:${Version.ROOM_VERSION}"

    // optional - Kotlin Extensions and Coroutines support for Room
    const val ROOM_KTX = "androidx.room:room-ktx:${Version.ROOM_VERSION}"

    // optional - RxJava support for Room
    const val ROOM_RXJAVA2 = "androidx.room:room-rxjava2:${Version.ROOM_VERSION}"
    const val ROOM_RXJAVA3 = "androidx.room:room-rxjava3:${Version.ROOM_VERSION}"

    // glide
    const val GLIDE = "com.github.bumptech.glide:glide:${Version.GLIDE_VERSION}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${Version.GLIDE_VERSION}"

    // Lottie
    const val LOTTIE = "com.airbnb.android:lottie:${Version.LOTTIE_VERSION}"

    // MpChart
    const val MP_CHART = "com.github.PhilJay:MPAndroidChart:${Version.MP_CHART_VERSION}"

    // Preference Manager
    const val PREFERENCE_MANAGER =
        "androidx.preference:preference-ktx:${Version.PREFERENCE_MANAGER_VERSION}"

    // Chucker
    const val CHUCKER_DEBUG = "com.github.chuckerteam.chucker:library:${Version.CHUCKER_VERSION}"
    const val CHUCKER_RELEASE =
        "com.github.chuckerteam.chucker:library-no-op:${Version.CHUCKER_VERSION}"


    const val CIRCLE_IMAGE = "de.hdodenhof:circleimageview:${Version.circleImageVersion}"


    const val K_PERMISSION = "com.github.fondesa:kpermissions:${Version.kPermissionVersion}"


    const val GMS_PLAY_LOCATION =
        "com.google.android.gms:play-services-location:${Version.gmsPlayLocation}"


    const val GMS_PLAY_AUTH =
        "com.google.android.gms:play-services-auth:${Version.gmsPlayAuthVersion}"


    const val KOTLIN_DATETIME =
        "org.jetbrains.kotlinx:kotlinx-datetime:${Version.kotlinDatetimeVersion}"

    const val IN_APP_UPDATE =
        "com.google.android.play:app-update-ktx:${Version.inAppUpdateVersion}"

    const val PICASSO =
        "com.squareup.picasso:picasso:${Version.picassoVersion}"

    const val APP_METRICA =
        "com.yandex.android:mobmetricalib:${Version.appMetrica}"


    const val YANDEX_AD =
        "com.yandex.android:mobileads:${Version.yandexAdVersion}"


    //Compose
    const val compose = "androidx.compose.ui:ui:${Version.compose}" //Основа
    const val composeUITooling = "androidx.compose.ui:ui-tooling:${Version.compose}" //Предпросмотр
    const val composeViewbinding =
        "androidx.compose.ui:ui-viewbinding:${Version.compose}" //Предпросмотр
    const val composeFoundation =
        "androidx.compose.foundation:foundation:${Version.compose}" //(Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    const val composeMaterial =
        "androidx.compose.material:material:${Version.compose}" //Material for compose
    const val composeAnimations =
        "androidx.compose.animation:animation:${Version.compose}" //Animations for compose
    const val composeViewModel =
        "androidx.lifecycle:lifecycle-viewmodel-compose:${Version.composeViewModel}" //ViewModel for compose
    const val composeRuntimeLifecycle =
        "androidx.lifecycle:lifecycle-runtime-compose:${Version.composeRuntimeLifecycle}"
    const val composeNavigation =
        "androidx.navigation:navigation-compose:${Version.NAVIGATION_VERSION}" //Navigation for compose
    const val composeCoilImage =
        "io.coil-kt:coil-compose:${Version.composeCoil}" //AsyncImageLoader
    const val composeAnimatedVector =
        "androidx.compose.animation:animation-graphics:${Version.compose}" //Анимированные drawable
    const val composeDebugCustomView =
        "androidx.customview:customview:${Version.composeCustomView}"
    const val composeDebugPolingContainer =
        "androidx.customview:customview-poolingcontainer:${Version.composePolingContainer}"
    const val composeAccompanistPermissions =
        "com.google.accompanist:accompanist-permissions:${Version.composeAccomponist}"
    const val composeM3 =
        "androidx.compose.material:material3:${Version.composeM3}"

    const val composem3 =
        "androidx.compose.material3:material3:1.1.2"

    //ViewBindingPropertyDelegate
    const val viewBindingPropertyDelegate =
        "com.github.kirich1409:viewbindingpropertydelegate:${Version.viewBindingPropertyDelegate}"
    const val viewBindingPropertyDelegateNoReflection =
        "com.github.kirich1409:viewbindingpropertydelegate-noreflection:${Version.viewBindingPropertyDelegate}"

    const val GRAPH_QL =
        "com.apollographql.apollo3:apollo-runtime:${Version.graphQlVersion}"

    const val composeRuntime =
        "androidx.compose.runtime:runtime:${Version.compose}"

    const val accompanistSystemUIController =
        "com.google.accompanist:accompanist-systemuicontroller:${Version.accompanist}"

    const val permissionX =
        "com.guolindev.permissionx:permissionx:${Version.permissionXVersion}"

    const val chuckerInterceptor =
        "com.github.chuckerteam.chucker:library:${Version.chuckerVersion}"

    const val yandexMaps = "com.yandex.android:maps.mobile:${Version.yandexMaps}"

    const val composeBlurForAllAndroids = "com.github.skydoves:cloudy:0.1.2"

    const val composeLandscapistGlide =
        "com.github.skydoves:landscapist-glide:${Version.composeLandscapistGlide}"
    const val composeLandscapistPlaceholder =
        "com.github.skydoves:landscapist-placeholder:${Version.composeLandscapistGlide}"
    const val reflection = "org.jetbrains.kotlin:kotlin-reflect:${Version.coreKtx}"

    const val accompanistNav = "com.google.accompanist:accompanist-navigation-material:${Version.accompanistNav}"

    const val glassmorphic = "com.github.jakhongirmadaminov:glassmorphic-composables:0.0.7"
    const val kotlin_immutable = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5"
    const val image_viewer = "com.github.stfalcon-studio:StfalconImageViewer:v1.0.1"

    const val accompanistWebView =
        "com.google.accompanist:accompanist-webview:${Version.accompanist}"
}

object TestDeps {

    // (Required) Writing and executing Unit Tests on the JUnit Platform
    const val JUNIT5_API = "org.junit.jupiter:junit-jupiter-api:${TestVersion.junit5Version}"
    const val JUNIT5_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${TestVersion.junit5Version}"

    // (Optional) If you need "Parameterized Tests"
    const val JUNIT5_PARAMS = "org.junit.jupiter:junit-jupiter-params:${TestVersion.junit5Version}"

    const val ANDROIDX_CORE_TESTING =
        "androidx.arch.core:core-testing:${TestVersion.archTestingVersion}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${TestVersion.robolectricVersion}"


    const val ANDROIDX_ESPRESSO =
        "androidx.test.espresso:espresso-contrib:${TestVersion.espressoVersion}"

    // Coroutines test
    const val COROUTINES_TEST =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${TestVersion.coroutinesTestVersion}"

    // MockWebServer
    const val MOCK_WEB_SERVER =
        "com.squareup.okhttp3:mockwebserver:${TestVersion.mockWebServerVersion}"

    // Gson
    const val GSON = "com.google.code.gson:gson:${Version.GSON_VERSION}"

    // MockK
    const val MOCK_K = "io.mockk:mockk:${TestVersion.mockKVersion}"

    // Truth
    const val TRUTH = "com.google.truth:truth:${TestVersion.truthVersion}"


    // Espresso
//    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${TestVersion.espressoVersion}"

    // Testing Navigation
    const val NAVIGATION_TEST =
        "androidx.navigation:navigation-testing:${Version.NAVIGATION_VERSION}"

}

