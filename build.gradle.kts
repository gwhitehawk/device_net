plugins {
    id("java")
    id("org.springframework.boot") version "3.2.6"
    id("io.spring.dependency-management") version "1.1.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runClientMain") {
    group = "application"
    description = "Runs the Main class in the client package"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.client.Main")
}