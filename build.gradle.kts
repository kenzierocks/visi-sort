import com.techshroom.inciseblue.commonLib
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    application
    kotlin("jvm") version "1.3.30"
    id("com.techshroom.incise-blue") version "0.3.10"
}
inciseBlue {
    util {
        setJavaVersion("11")
    }
    license()
    ide()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    commonLib("org.jetbrains.kotlinx", "kotlinx-coroutines", "1.2.0") {
        implementation(lib("core"))
        implementation(lib("jdk8"))
    }
    implementation(group = "com.techshroom", name = "UnplannedDescent.api", version = project.property("ud.version").toString())
    implementation(group = "com.techshroom", name = "UnplannedDescent.implementation", version = project.property("ud.version").toString())
    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.26")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    implementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.3")

    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")

    implementation(group = "com.techshroom", name = "jsr305-plus", version = "0.0.1")

    implementation(group = "org.biojava", name = "jcolorbrewer", version = "5.2")

    testImplementation(group = "junit", name = "junit", version = "4.12")
}

tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
        jvmTarget = "1.8"
    }
}

application.mainClassName = "me.kenzierocks.visisort.Main"
