plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    id("org.openapi.generator") version "7.26.0"
    application
}

repositories { mavenCentral() }

kotlin { jvmToolchain(17) }

openApiGenerate {
    generatorName = "kotlin"
    library = "jvm-retrofit2"
    inputSpec = layout.projectDirectory.file("openapi.yaml")
    outputDir = layout.buildDirectory.dir("generated")
    packageName = "repro.client"
    configOptions = mapOf(
        "serializationLibrary" to "kotlinx_serialization",
        "useCoroutines" to "true",
    )
    // `./gradlew run -PwithFix` uses api.mustache with only the suggested one-line change.
    if (providers.gradleProperty("withFix").isPresent) {
        templateDir = layout.projectDirectory.dir("fix")
    }
}

sourceSets.main { kotlin.srcDir(tasks.openApiGenerate.map { it.outputDir.dir("src/main/kotlin") }) }

dependencies {
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.1")
    implementation("com.squareup.okhttp3:logging-interceptor:5.5.1")
    implementation("com.squareup.okhttp3:mockwebserver3:5.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}

application { mainClass = "MainKt" }
