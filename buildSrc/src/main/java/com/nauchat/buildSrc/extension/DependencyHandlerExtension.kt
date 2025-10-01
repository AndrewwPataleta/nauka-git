package extension

import Deps
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyHandler




fun DependencyHandler.addAppModuleDependencies() {

    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)

    
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.VIEWPAGER2)
    implementation(Deps.GMS_PLAY_LOCATION)

    
    implementation(platform(Deps.FIREBASE_PLATFORM_BOM))
    implementation(Deps.FIREBASE_ANALYTICS)
    implementation(Deps.FIREBASE_AUTH)
    implementation(Deps.FIREBASE_MESSAGE)

    
    implementation(Deps.LOTTIE)
    implementation(Deps.TIMBER)

    
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_RUNTIME_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_SAVEDSTATE)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)
    implementation(Deps.LIFECYCLE_SERVICE)
    implementation(Deps.LIFECYCLE_PROCESS)

    
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    
    implementation(Deps.DAGGER_HILT_ANDROID)
    implementation(Deps.DAGGER_HILT_NAVIGATION_COMPOSE)
    kapt(Deps.DAGGER_HILT_COMPILER)

    
    implementation(Deps.COROUTINES_CORE)
    implementation(Deps.COROUTINES_ANDROID)
    
    implementation(Deps.ROOM_RUNTIME)
    
    kapt(Deps.ROOM_COMPILER)
    
    implementation(Deps.ROOM_KTX)

    
    implementation(Deps.GSON)
    

    
    implementation(Deps.TEMPO)

    
    implementation(Deps.GLIDE)
    kapt(Deps.GLIDE_COMPILER)

    implementation(Deps.KOTLIN_DATETIME)

    implementation(Deps.viewBindingPropertyDelegate)
    implementation(Deps.viewBindingPropertyDelegateNoReflection)

    
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

    
}


fun DependencyHandler.addDataDependencies() {
    implementation(Deps.GRAPH_QL)
}




fun DependencyHandler.addCoreModuleDependencies() {
    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)

    
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.VIEWPAGER2)

    
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_SAVEDSTATE)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)
    implementation(Deps.LIFECYCLE_SERVICE)
    implementation(Deps.LIFECYCLE_PROCESS)

    
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    
    implementation(Deps.DAGGER_HILT_ANDROID)
    implementation(Deps.DAGGER_HILT_NAVIGATION_COMPOSE)
    kapt(Deps.DAGGER_HILT_COMPILER)

    
    implementation(Deps.COROUTINES_CORE)
    implementation(Deps.COROUTINES_ANDROID)

    implementation(Deps.OK_HTTP3)
    
    implementation(Deps.GLIDE)
    kapt(Deps.GLIDE_COMPILER)

    implementation(Deps.GSON)
    implementation(Deps.KOTLIN_DATETIME)

    
    implementation(Deps.composeRuntime)

    implementation(Deps.accompanistSystemUIController)
    implementation(Deps.chuckerInterceptor)
    implementation(Deps.reflection)
    
    implementation(Deps.yandexMaps)
}

fun DependencyHandler.addDSModuleDependencies() {
    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)

    
    implementation(Deps.APPCOMPAT)
    implementation(Deps.MATERIAL)
    implementation(Deps.CONSTRAINT_LAYOUT)
    implementation(Deps.RECYCLER_VIEW)
    implementation(Deps.VIEWPAGER2)

    
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_SAVEDSTATE)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)
    implementation(Deps.LIFECYCLE_SERVICE)
    implementation(Deps.LIFECYCLE_PROCESS)

    
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    
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
    
    

    implementation(Deps.glassmorphic)
    implementation(Deps.kotlin_immutable)
    implementation(Deps.KOTLIN_DATETIME)
    implementation(Deps.accompanistWebView)
    implementation("androidx.browser:browser:1.5.0")
}






fun DependencyHandler.addBaseDynamicFeatureModuleDependencies() {
    implementation(Deps.KOTLIN)
    implementation(Deps.ANDROIDX_CORE_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_KTX)
    implementation(Deps.ANDROIDX_ACTIVITY_COMPOSE)
    implementation(Deps.ANDROIDX_FRAGMENT_KTX)
    
    implementation(Deps.LIFECYCLE_LIVEDATA_KTX)
    implementation(Deps.LIFECYCLE_VIEWMODEL_KTX)
    implementation(Deps.LIFECYCLE_COMMON_JAVA8)

    
    implementation(Deps.NAVIGATION_FRAGMENT)
    implementation(Deps.NAVIGATION_UI)
    implementation(Deps.NAVIGATION_RUNTIME)
    implementation(Deps.NAVIGATION_DYNAMIC)

    
    implementation(Deps.DAGGER_HILT_ANDROID)
    implementation(Deps.DAGGER_HILT_NAVIGATION_COMPOSE)
    kapt(Deps.DAGGER_HILT_COMPILER)

    implementation(Deps.SWIPE_REFRESH_LAYOUT)

    implementation(Deps.GSON)
    
    implementation(Deps.RX_JAVA3)
    
    implementation(Deps.RX_JAVA3_ANDROID)

    
    implementation(Deps.COROUTINES_CORE)
    implementation(Deps.COROUTINES_ANDROID)

    
    implementation(Deps.TEMPO)

    
    implementation(Deps.ROOM_RUNTIME)
    kapt(Deps.ROOM_COMPILER)

    implementation(Deps.ROOM_KTX)
    implementation(Deps.ROOM_RXJAVA3)

    
    implementation(Deps.composeRuntime)

    implementation(Deps.accompanistSystemUIController)
}




fun DependencyHandler.addUnitTestDependencies() {


}

fun DependencyHandler.addInstrumentationTestDependencies() {

}






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
