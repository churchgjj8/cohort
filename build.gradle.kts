import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
   repositories {
      mavenCentral()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      }
      maven {
         url = uri("https://plugins.gradle.org/m2/")
      }
   }
}

plugins {
   signing
   `maven-publish`
   kotlin("jvm").version(Libs.kotlinVersion)
}

allprojects {
   apply(plugin = "org.jetbrains.kotlin.jvm")

   repositories {
      mavenLocal()
      mavenCentral()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots")
      }
   }

   group = Libs.org
   version = Ci.version

   dependencies {
      implementation(Libs.Kotlin.stdlib)
      implementation(Libs.Kotlin.coroutines)
      implementation(Libs.Kotlin.coroutinesJdk8)
      implementation(Libs.Tabby.fp)
      testImplementation(Libs.Kotest.assertions)
      testImplementation(Libs.Kotest.junit5)
      api("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-22")
      api("org.slf4j:slf4j-api:2.0.7")
   }

   tasks.named<Test>("test") {
      useJUnitPlatform()
      testLogging {
         showExceptions = true
         showStandardStreams = true
         exceptionFormat = TestExceptionFormat.FULL
      }
   }

   tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions.jvmTarget = "11"
   }
}
