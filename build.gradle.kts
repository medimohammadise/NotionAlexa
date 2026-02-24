plugins {
    java
    id("org.springframework.boot") version "4.1.0-M2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.notionalexa"
version = "1.0.0-SNAPSHOT"
description = "Serverless Spring Modulith application powering an Amazon Alexa skill to read Notion notes"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springModulithVersion"] = "1.4.1"
extra["springCloudVersion"] = "2025.0.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

dependencies {
    // Spring Boot Web (RestClient)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Modulith
    implementation("org.springframework.modulith:spring-modulith-starter-core")

    // Spring Cloud Function + AWS Lambda adapter
    implementation("org.springframework.cloud:spring-cloud-function-adapter-aws")

    // Notion Java SDK (type-safe models + Java 11 HttpClient transport)
    implementation("com.github.seratch:notion-sdk-jvm-core:1.11.1")
    implementation("com.github.seratch:notion-sdk-jvm-httpclient:1.11.1")

    // Alexa Skills Kit SDK
    implementation("com.amazon.alexa:ask-sdk:2.86.0")

    // Configuration processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
