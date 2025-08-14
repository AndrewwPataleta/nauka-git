package extension

import Deps
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Adds required dependencies to app module
 */
fun DependencyHandler.addAppModuleDependencies() {

    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)

    // Support and Widgets
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.VIEWPAGER2)
    implementation(Deps.GMS_PLAY_LOCATION)

    //Firebase
    implementation(platform(Deps.FIREBASE_PLATFORM_BOM))
    implementation(Deps.FIREBASE_ANALYTICS)
    implementation(Deps.FIREBASE_AUTH)
    implementation(Deps.FIREBASE_MESSAGE)

    // Views, Animations
    implementation(Deps.LOTTIE)
    implementation(Deps.TIMBER)

    // Lifecycle, LiveData, ViewModel
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_RUNTIME_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_SAVEDSTATE)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)
    implementation(Deps.LIFECYCLE_SERVICE)
    implementation(Deps.LIFECYCLE_PROCESS)

    // Navigation Components
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    // Dagger Hilt
    implementation(Deps.DAGGER_HILT_ANDROID)
    implementation(Deps.DAGGER_HILT_NAVIGATION_COMPOSE)
    kapt(Deps.DAGGER_HILT_COMPILER)

    // Coroutines
    implementation(Deps.COROUTINES_CORE)
    implementation(Deps.COROUTINES_ANDROID)
    // Room
    implementation(Deps.ROOM_RUNTIME)
    // For Kotlin use kapt instead of annotationProcessor
    kapt(Deps.ROOM_COMPILER)
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(Deps.ROOM_KTX)

    // Gson
    implementation(Deps.GSON)
    //implementation(Deps.CHUCKER_DEBUG)

    //Date working
    implementation(Deps.TEMPO)

    // Glide
    implementation(Deps.GLIDE)
    kapt(Deps.GLIDE_COMPILER)

    implementation(Deps.KOTLIN_DATETIME)

    implementation(Deps.viewBindingPropertyDelegate)
    implementation(Deps.viewBindingPropertyDelegateNoReflection)

    // Compose
    implementation(Deps.composeRuntime)
    implementation(Deps.composeViewbinding)
    implementation(Deps.composeFoundation)

    implementation(Deps.accompanistSystemUIController)

    implementation(Deps.permissionX)
    implementation(Deps.yandexMaps)

    implementation(Deps.kotlin_immutable)
    implementation(Deps.PICASSO)
    implementation(Deps.image_viewer)
    implementation(Deps.KOTLIN_DATETIME)
    implementation ("io.github.chochanaresh:filepicker:0.2.3")
    implementation("com.github.dhaval2404:imagepicker:2.1")

    //implementation("com.github.Mindinventory:Lassi:1.3.0")
}


fun DependencyHandler.addDataDependencies() {
    implementation(Deps.GRAPH_QL)
}

/**
 * Adds dependencies to core module
 */
fun DependencyHandler.addCoreModuleDependencies() {
    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)

    // Support and Widgets
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.VIEWPAGER2)

    // Lifecycle, LiveData, ViewModel
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_SAVEDSTATE)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)
    implementation(Deps.LIFECYCLE_SERVICE)
    implementation(Deps.LIFECYCLE_PROCESS)

    // Navigation Components
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    // Dagger Hilt
    implementation(Deps.DAGGER_HILT_ANDROID)
    implementation(Deps.DAGGER_HILT_NAVIGATION_COMPOSE)
    kapt(Deps.DAGGER_HILT_COMPILER)

    // Coroutines
    implementation(Deps.COROUTINES_CORE)
    implementation(Deps.COROUTINES_ANDROID)

    implementation(Deps.OK_HTTP3)
    // Glide
    implementation(Deps.GLIDE)
    kapt(Deps.GLIDE_COMPILER)

    implementation(Deps.GSON)
    implementation(Deps.KOTLIN_DATETIME)

    // Compose
    implementation(Deps.composeRuntime)

    implementation(Deps.accompanistSystemUIController)
    implementation(Deps.chuckerInterceptor)
    implementation(Deps.reflection)
    //implementation("com.github.Mindinventory:Lassi:1.3.0")
    implementation(Deps.yandexMaps)
}

fun DependencyHandler.addDSModuleDependencies() {
    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)

    // Support and Widgets
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.VIEWPAGER2)

    // Lifecycle, LiveData, ViewModel
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_SAVEDSTATE)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)
    implementation(Deps.LIFECYCLE_SERVICE)
    implementation(Deps.LIFECYCLE_PROCESS)

    // Navigation Components
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    // Compose
    implementation(Deps.compose)
    implementation(Deps.composeRuntime)
    implementation(Deps.composeRuntimeLifecycle)
    implementation(Deps.composeNavigation)
    implementation(Deps.composeUITooling)
    implementation(Deps.composeFoundation)
    implementation(Deps.composeMaterial)
    implementation(Deps.composeAnimations)
    implementation(Deps.composeAnimatedVector)
    implementation(Deps.composeDebugCustomView)
    implementation(Deps.composeDebugPolingContainer)
    implementation(Deps.composem3)
    implementation(Deps.composeViewModel)
    implementation(Deps.CONSTRAINT_LAYOUT_COMPOSE)

    implementation(Deps.accompanistSystemUIController)

    implementation(Deps.composeBlurForAllAndroids)

    implementation(Deps.composeLandscapistGlide)
    implementation(Deps.composeLandscapistPlaceholder)
    //implementation(Deps.composeM3)
    //implementation(Deps.accompanistNav)

    implementation(Deps.glassmorphic)
    implementation(Deps.kotlin_immutable)
    implementation(Deps.KOTLIN_DATETIME)
    implementation(Deps.accompanistWebView)
    implementation("androidx.browser:browser:1.5.0")
}

/**
 * Adds core dependencies such as kotlin, appcompat, navigation and dagger-hilt to Dynamic
 * Feature modules.
 *
 */
fun DependencyHandler.addBaseDynamicFeatureModuleDependencies() {
    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)
    // Lifecycle, LiveData, ViewModel
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)

    // Navigation Components
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    // Dagger Hilt
    implementation(Deps.DAGGER_HILT_ANDROID)
    implementation(Deps.DAGGER_HILT_NAVIGATION_COMPOSE)
    kapt(Deps.DAGGER_HILT_COMPILER)

    implementation(Deps.SWIPE_REFRESH_LAYOUT)

    implementation(Deps.GSON)
    // RxJava
    implementation(Deps.RX_JAVA3)
    // RxAndroid
    implementation(Deps.RX_JAVA3_ANDROID)

    // Coroutines
    implementation(Deps.COROUTINES_CORE)
    implementation(Deps.COROUTINES_ANDROID)

    //Date working
    implementation(Deps.TEMPO)

    // Room
    implementation(Deps.ROOM_RUNTIME)
    kapt(Deps.ROOM_COMPILER)

    implementation(Deps.ROOM_KTX)
    implementation(Deps.ROOM_RXJAVA3)

    // Compose
    implementation(Deps.composeRuntime)

    implementation(Deps.accompanistSystemUIController)
}

/**
 * Adds Unit test dependencies
 */
fun DependencyHandler.addUnitTestDependencies() {


}

fun DependencyHandler.addInstrumentationTestDependencies() {

}

/*
 * These extensions mimic the extensions that are generated on the fly by Gradle.
 * They are used here to provide above dependency syntax that mimics Gradle Kotlin DSL
 * syntax in module\build.gradle.kts files.
 */
@Suppress("detekt.UnusedPrivateMember")
private fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

@Suppress("detekt.UnusedPrivateMember")
private fun DependencyHandler.api(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)

@Suppress("detekt.UnusedPrivateMember")
private fun DependencyHandler.kapt(dependencyNotation: Any): Dependency? =
    add("kapt", dependencyNotation)

private fun DependencyHandler.testImplementation(dependencyNotation: Any): Dependency? =
    add("testImplementation", dependencyNotation)

private fun DependencyHandler.debugImplementation(dependencyNotation: Any): Dependency? =
    add("debugImplementation", dependencyNotation)

private fun DependencyHandler.testRuntimeOnly(dependencyNotation: Any): Dependency? =
    add("testRuntimeOnly", dependencyNotation)

private fun DependencyHandler.androidTestImplementation(dependencyNotation: Any): Dependency? =
    add("androidTestImplementation", dependencyNotation)

private fun DependencyHandler.project(
    path: String,
    configuration: String? = null,
): ProjectDependency {
    val notation = if (configuration != null) {
        mapOf("path" to path, "configuration" to configuration)
    } else {
        mapOf("path" to path)
    }

    return uncheckedCast(project(notation))
}

@Suppress("unchecked_cast", "nothing_to_inline", "detekt.UnsafeCast")
private inline fun <T> uncheckedCast(obj: Any?): T = obj as T
