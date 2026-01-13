import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.0"
  kotlin("plugin.spring") version "2.3.0"
  id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

val hmppsSpringBootVersion = "2.0.0"
val springdocVersion = "3.0.1"

dependencies {
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:$hmppsSpringBootVersion")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-restclient")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:$hmppsSpringBootVersion")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37") {
    exclude(group = "io.swagger.core.v3")
  }
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    freeCompilerArgs.addAll("-Xannotation-default-target=param-property")
  }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
  jvmTarget = "21"
}

configurations.matching { it.name == "detekt" }.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
    }
  }
}

detekt {
  config.setFrom("detekt/detekt.yml")
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }
}

buildscript {
  repositories {
    maven {
      url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
  }
  dependencies {
    classpath("dev.detekt:detekt-gradle-plugin:main-SNAPSHOT")
  }
}
