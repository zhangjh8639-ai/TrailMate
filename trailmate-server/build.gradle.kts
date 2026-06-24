plugins {
    java
    alias(libs.plugins.spring.boot)
}

group = "com.trailmate"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql.database)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.h2.database)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
