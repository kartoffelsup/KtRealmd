import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
  id("org.jetbrains.kotlin.jvm") version ("1.3-M2")
}

group = "com.arml"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    url = uri("https://dl.bintray.com/kotlin/exposed")
  }
  maven {
    url = uri("http://dl.bintray.com/kotlin/kotlin-eap")
  }
}

val kotlinVersion: String by extra

dependencies {
  compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
  compile("org.jetbrains.exposed:exposed:0.10.4")
  compile("org.mariadb.jdbc:mariadb-java-client:2.2.6")
  compile("com.google.guava:guava:26.0-jre")

  val jUnitVersion: String  by extra
  val assertJVersion: String by extra
  testCompile("junit:junit:$jUnitVersion")
  testCompile("org.assertj:assertj-core:$assertJVersion")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}
