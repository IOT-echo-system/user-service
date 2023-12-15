import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    id("jacoco")
}

group = "com.shiviraj.iot"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("./libs/logging-starter-0.0.1.jar"))
    implementation(files("./libs/iot-utils-starter-0.0.1.jar"))
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")


    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
}


dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Jacoco configuration`
jacoco {
    toolVersion = "0.8.7"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    finalizedBy(tasks.jacocoTestCoverageVerification)

}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport) // tests are required to run before generating the report
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = BigDecimal(0.86)
            }
            limit {
                counter = "BRANCH"
                minimum = BigDecimal(0.8)
            }
            limit {
                counter = "LINE"
                minimum = BigDecimal(0.89)
            }
            limit {
                counter = "METHOD"
                minimum = BigDecimal(0.75)
            }
            limit {
                counter = "CLASS"
                minimum = BigDecimal(0.85)
            }
        }
    }
}
