import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven {
            name = "KotDis"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }
    }
}

repositories {
    maven {
        name = "KotDis"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

plugins {
    `maven-publish`

    kotlin("jvm") version "1.4.10"
    id("io.gitlab.arturbosch.detekt") version "1.13.1"
}

group = "com.kotlindiscord.kordex"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("io.github.microutils:kotlin-logging:2.0.3")
    testImplementation("org.codehaus.groovy:groovy:3.0.4")  // For logback config

    implementation(project(":ext-mappings"))
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"

        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        maven {
            name = "KotDis"
            url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
        }

        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }

        maven {
            name = "Bintray (Linkie)"
            url = uri("https://dl.bintray.com/shedaniel/linkie")
        }

        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
        }
    }

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.13.1")

        api("com.uchuhimo:konf:0.23.0")
        api("com.uchuhimo:konf-toml:0.23.0")

        implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.4.0-RC4")
        implementation("io.github.microutils:kotlin-logging:2.0.3")

        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    }

    detekt {
        buildUponDefaultConfig = true
        config = rootProject.files("detekt.yml")
    }
}
