// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Define versions for plugins used throughout the project.
    // These plugins are applied with 'apply true' in module-level build.gradle.kts files, or 'apply false' here if only version is managed.
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false // Updated for OkHttp 5.3.0 compatibility
}

allprojects {
    // Configure buildDir for sub-modules to be placed under rootProject.buildDir/modules/
    if (rootProject.name != project.name) {
        layout.buildDirectory.set(File(rootProject.layout.buildDirectory.asFile.get(), "modules/${project.name}"))
    }
    // Repositories for dependencies are now managed centrally in settings.gradle.kts.
    // The 'repositories' block from the old build.gradle has been removed from here.
}

// The 'buildscript' block from the old build.gradle is no longer needed here.
// Its 'repositories' are managed by 'pluginManagement' in settings.gradle.kts.
// Its 'dependencies' (classpath) are handled by the 'plugins' block above.