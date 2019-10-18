import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.50")
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

val exposedVersion: String by extra
val mariaDbVersion: String by extra
val guavaVersion: String by extra
val argParserVersion: String by extra
val log4jVersion: String by extra

val jUnitVersion: String by extra
val assertJVersion: String by extra

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("org.jetbrains.kotlin:kotlin-reflect")
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
    register("bundle", org.gradle.api.tasks.bundling.Jar::class.java) {
        dependsOn("build")
        manifest {
            attributes["Main-Class"] = "com.arml.realmd.RealmdKt"
        }
        archiveBaseName.set("realmd")
        from(
            configurations.runtimeClasspath.get().map {
                if (it.isDirectory)
                    it
                else zipTree(it)
            }
        )
        with(getTasks()["jar"] as CopySpec)
    }
}
