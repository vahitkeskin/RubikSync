import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Generates a common Kotlin constant from the single source of truth in
// gradle/libs.versions.toml so every platform reflects the real app version.
val generateAppVersion = tasks.register("generateAppVersion") {
    description = "Generates AppVersion.kt from the version catalog."
    val versionName = libs.versions.appVersionName.get()
    val outputDir = layout.buildDirectory.dir("generated/appVersion/commonMain/kotlin")
    inputs.property("versionName", versionName)
    outputs.dir(outputDir)
    doLast {
        val pkgDir = outputDir.get().asFile.resolve("com/vahitkeskin/rubiksync")
        pkgDir.mkdirs()
        pkgDir.resolve("AppVersion.kt").writeText(
            """
            package com.vahitkeskin.rubiksync

            // Generated from gradle/libs.versions.toml — do not edit by hand.
            object AppVersion {
                const val NAME: String = "$versionName"
            }

            """.trimIndent() + "\n"
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    androidLibrary {
       namespace = "com.vahitkeskin.rubiksync.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generateAppVersion)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.timber)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.uiTest)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}