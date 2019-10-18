import com.querydsl.sql.types.IntegerType
import io.github.kartoffelsup.querydsl.sql.codegen.GenerateQueryDslSqlSources
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.github.kartoffelsup.querydsl.sql.codegen.QueryDslSqlCodeGen

buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        classpath("io.github.kartoffelsup:querydsl-sql-codegen-gradle-plugin:0.0.1")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.50")
    id("idea")
}

group = "com.arml"
version = "0.0.1-SNAPSHOT"

apply<QueryDslSqlCodeGen>()

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/exposed")
    }
}

val mariaDbVersion: String by extra
val hikariVersion: String by extra
val guavaVersion: String by extra
val queryDslVersion: String by extra
val hopliteVersion: String by extra
val log4jVersion: String by extra

val jUnitVersion: String by extra
val assertJVersion: String by extra
val mockKVersion: String by extra

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.querydsl:querydsl-sql:$queryDslVersion")
    implementation("org.mariadb.jdbc:mariadb-java-client:$mariaDbVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-json:$hopliteVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtime("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    testImplementation("junit:junit:$jUnitVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation("io.mockk:mockk:$mockKVersion")
}

val generatedSourcesPath = file("src/generated/kotlin")

sourceSets.main {
    java {
        srcDir(generatedSourcesPath)
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedSourcesPath)
    }
}

tasks {
    withType<KotlinCompile> {
        dependsOn("generateQueryDslSqlSources")
        kotlinOptions.jvmTarget = "1.8"
    }

    register("generateQueryDslSqlSources", GenerateQueryDslSqlSources::class) {
        target = generatedSourcesPath
        packageName = "io.github.kartoffelsup.realmd.sql"
        beanPackageName = "io.github.kartoffelsup.realmd.bean"
        beanNameSuffix = "Bean"
        schema = "realmd"
        jdbcUrl = System.getProperty("jdbcUrl", "jdbc:mariadb://localhost")
        username = System.getProperty("username", "realmd")
        password = System.getProperty("password", "realmd")
        driverClassName = "org.mariadb.jdbc.Driver"
        configurationCustomizer = {
            register("realmcharacters", "acctid", IntegerType())
        }
    }

    getByName("clean") {
        doLast {
            delete(generatedSourcesPath.absoluteFile)
        }
    }

    register("bundle", org.gradle.api.tasks.bundling.Jar::class.java) {
        dependsOn("build")
        manifest {
            attributes["Main-Class"] = "io.github.kartoffelsup.realmd.RealmdKt"
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
