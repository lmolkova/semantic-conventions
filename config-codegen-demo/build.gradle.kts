plugins {
  java
  application
}

repositories {
  mavenCentral()
}

val otelVersion = "1.55.0"
val otelAlphaVersion = "1.55.0-alpha"

dependencies {
  implementation("io.opentelemetry:opentelemetry-api:$otelVersion")
  implementation("io.opentelemetry:opentelemetry-api-incubator:$otelAlphaVersion")
  implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.37.0")
  implementation("io.opentelemetry:opentelemetry-sdk:$otelVersion")
  implementation("io.opentelemetry:opentelemetry-sdk-extension-incubator:$otelAlphaVersion")
  implementation("io.opentelemetry:opentelemetry-exporter-logging:$otelVersion")
  implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:$otelVersion")

  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:$otelVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
  testImplementation("org.assertj:assertj-core:3.26.3")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
  mainClass.set("io.opentelemetry.semconv.prototype.demo.Demo")
}

tasks.named<JavaExec>("run") {
  environment("OTEL_SERVICE_NAME", "config-codegen-demo")
  environment("OTEL_TRACES_EXPORTER", "console")
  environment("OTEL_METRICS_EXPORTER", "none")
  environment("OTEL_LOGS_EXPORTER", "none")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

sourceSets {
  main {
    java {
      srcDir("generated/java")
    }
  }
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "failed")
  }
}
