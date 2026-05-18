plugins {
    id("java")
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.razumoff"
version = "0.0.1-SNAPSHOT"
description = "tutor-courses"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    maven {
        name = "MinIO"
        url = uri("https://minio.razum0ff.ru/maven-repository")
        isAllowInsecureProtocol = true
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("ru.razumoff.common:api-errors:1.0.5")
    implementation("ru.razumoff.common:base-utils:1.0.0")
    implementation("ru.razumoff.common:jwt:1.0.2")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("io.minio:minio:9.0.0")
    implementation("org.modelmapper:modelmapper:3.2.6")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging{
        events("passed")
    }
}