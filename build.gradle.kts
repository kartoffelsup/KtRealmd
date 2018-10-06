import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
  id("org.jetbrains.kotlin.jvm") version ("1.2.71")
}

group = "com.arml"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    url = uri("https://dl.bintray.com/kotlin/exposed")
  }
}

val kotlinVersion: String by extra
val exposedVersion: String by extra
val mariaDbVersion: String by extra
val guavaVersion: String by extra
val argParserVersion: String by extra
val log4jVersion: String by extra

val jUnitVersion: String  by extra
val assertJVersion: String by extra

dependencies {
  compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  compile("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
  compile("org.jetbrains.exposed:exposed:$exposedVersion")
  compile("org.mariadb.jdbc:mariadb-java-client:$mariaDbVersion")
  compile("com.google.guava:guava:$guavaVersion")
  compile("com.xenomachina:kotlin-argparser:$argParserVersion")

  compile("org.apache.logging.log4j:log4j-api:$log4jVersion")
  compile("org.apache.logging.log4j:log4j-core:$log4jVersion")
  runtime("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

  testCompile("junit:junit:$jUnitVersion")
  testCompile("org.assertj:assertj-core:$assertJVersion")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks {
  register("bundle", Jar::class) {
    dependsOn("build")
    manifest {
      attributes["Main-Class"] = "com.arml.realmd.RealmdKt"
    }
    baseName = "realmd"
    from(
      configurations.compile.map {
        if (it.isDirectory)
          it
        else zipTree(it)
      }
    )
    with(tasks["jar"] as CopySpec)
  }
}
