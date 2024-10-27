plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    id("jacoco")
}

group = "com.robotutor.iot"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()

    fun githubMavenRepository(name: String) {
        maven {
            url = uri("https://maven.pkg.github.com/IOT-echo-system/$name")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    githubMavenRepository("robotutor-tech-utils")
    githubMavenRepository("mqtt-starter")
    githubMavenRepository("web-client-starter")
    githubMavenRepository("logging-starter")
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.springframework.integration:spring-integration-mqtt:6.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-crypto:5.6.4")
    implementation("com.robotutor:logging-starter:1.0.0")
    implementation("com.robotutor:robotutor-tech-utils:1.0.0")
    implementation("com.robotutor:web-client-starter:1.0.1")
    implementation("com.robotutor:mqtt-starter:1.0.1")

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


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

//tasks.withType<Test> {
//    useJUnitPlatform()
//}

// Jacoco configuration
/*
jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = BigDecimal(0.93)
            }
            limit {
                counter = "BRANCH"
                minimum = BigDecimal(1)
            }
            limit {
                counter = "LINE"
                minimum = BigDecimal(0.95)
            }
            limit {
                counter = "METHOD"
                minimum = BigDecimal(0.80)
            }
            limit {
                counter = "CLASS"
                minimum = BigDecimal(0.93)
            }
        }
    }
}
*/
