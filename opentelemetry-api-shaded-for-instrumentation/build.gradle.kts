plugins {
    id("project-conventions")
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

description = "opentelemetry-api shaded for internal javaagent usage"

dependencies {
    implementation("io.opentelemetry:opentelemetry-api:1.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// OpenTelemetry API shaded so that it can be used in instrumentation of OpenTelemetry API itself,
// and then its usage can be unshaded after OpenTelemetry API is shaded
// (see more explanation in opentelemetry-api-1.0.gradle)
tasks {
    shadowJar {
        relocate("io.opentelemetry", "application.io.opentelemetry")
    }
}

artifacts {
    shadow
}
