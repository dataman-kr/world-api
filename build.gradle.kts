import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.4.0"
}

group = "com.dataman"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // 📦 Swagger UI + 자동 문서화
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val cityOutputDir = layout.buildDirectory.dir("generated/city")
val countryOutputDir = layout.buildDirectory.dir("generated/country")

// 💥 OpenAPI: City API
tasks.register<GenerateTask>("openApiGenerateCity") {
    generatorName.set("kotlin-spring")
    inputSpec.set(project.file("src/main/resources/openapi/world-city-api.yml").absolutePath)
    outputDir.set(cityOutputDir.map { it.asFile.absolutePath })
    apiPackage.set("com.example.worldapi.city.api")
    modelPackage.set("com.example.worldapi.city.model")
    invokerPackage.set("com.example.worldapi.city.invoker")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "useSwaggerAnnotations" to "false",
            "dateLibrary" to "java8"
        )
    )
}

// 💥 OpenAPI: Country API
tasks.register<GenerateTask>("openApiGenerateCountry") {
    generatorName.set("kotlin-spring")
    inputSpec.set(project.file("src/main/resources/openapi/world-country-api.yml").absolutePath)
    outputDir.set(countryOutputDir.map { it.asFile.absolutePath })
    apiPackage.set("com.example.worldapi.country.api")
    modelPackage.set("com.example.worldapi.country.model")
    invokerPackage.set("com.example.worldapi.country.invoker")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "useSwaggerAnnotations" to "false",
            "dateLibrary" to "java8"
        )
    )
}

// ✅ Kotlin 컴파일 전에 생성하도록 의존성 설정
tasks.named("compileKotlin") {
    dependsOn("openApiGenerateCity", "openApiGenerateCountry")
}

// 💡 OpenAPI YAML 파일 복사 (Swagger UI용)
tasks.register<Copy>("copyOpenApiSpec") {
    from("src/main/resources/openapi")
    into("src/main/resources/static/openapi")
    include("**/*.yaml", "**/*.yml")
    rename("world-city-api.yaml", "world-city-api.yml")
}

// 🔁 build나 processResources 전에 YAML 복사
tasks.named("build") {
    dependsOn("copyOpenApiSpec")
}

tasks.named("openApiGenerateCity") {
    dependsOn("copyOpenApiSpec")
}

tasks.named("openApiGenerateCountry") {
    dependsOn("copyOpenApiSpec")
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("copyOpenApiSpec")
}

sourceSets["main"].kotlin.srcDirs(
    layout.buildDirectory.dir("generated/city/src/main/kotlin").get().asFile,
    layout.buildDirectory.dir("generated/country/src/main/kotlin").get().asFile
)

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
