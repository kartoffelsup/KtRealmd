import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.mariadb.jdbc.MariaDbDataSource

buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        val queryDslVersion: String by rootProject.extra
        val mariaDbVersion: String by rootProject.extra

        classpath("io.github.kartoffelsup:querydsl-sql-codegen-gradle-plugin:0.0.3-SNAPSHOT") {
            exclude("com.querydsl", "querydsl-sql-codegen")
        }
        classpath("com.querydsl:querydsl-sql-codegen:$queryDslVersion")
        classpath("org.mariadb.jdbc:mariadb-java-client:$mariaDbVersion")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.72")
    id("idea")
}

group = "io.github.kartoffelsup"
version = "0.0.1-SNAPSHOT"

apply {
    plugin("io.github.kartoffelsup.querydsl.sql.codegen")
}

repositories {
    mavenLocal()
    mavenCentral()
}

val mariaDbVersion: String by extra
val hikariVersion: String by extra
val guavaVersion: String by extra
val queryDslVersion: String by extra
val hopliteVersion: String by extra
val log4jVersion: String by extra
val javaxAnnotationApiVersion: String by extra

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
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitVersion")
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

    test {
        useJUnitPlatform()
    }

    withType<io.github.kartoffelsup.querydsl.sql.codegen.GenerateQueryDslSqlSources> {
        val user = System.getProperty("db-user")
        val password = System.getProperty("db-pw")
        val host = System.getProperty("db-host")
        val ds: javax.sql.DataSource =
            MariaDbDataSource(host, 3306, "realmd").apply {
                this.user = user
                setPassword(password)
            }

        val file = file(generatedSourcesPath)
        target.set(file)
        packageName.set("io.github.kartoffelsup.realmd.sql")
        beanPackageName.set("io.github.kartoffelsup.realmd.bean")
        schema.set("realmd")
        dataSource.set(ds)
        configuration.set(com.querydsl.sql.Configuration(com.querydsl.sql.MySQLTemplates()).apply {
            register("realmcharacters", "acctid", com.querydsl.sql.types.IntegerType())
        })
        customizer.set { setBeanSuffix("Bean") }
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
