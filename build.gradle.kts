plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.puhovin.intellijplugin"
version = "1.1.0"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2.1")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    patchPluginXml {
        pluginName = "ToolWindow Manager"
        sinceBuild = "242"
        untilBuild = "999.*"
    }

    signPlugin {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        token = System.getenv("PUBLISH_TOKEN")
    }

    wrapper {
        gradleVersion = "8.12"
    }

    register("printVersion") {
        val versionProvider = providers.provider { project.version }
        doLast {
            println(versionProvider.get())
        }
    }
}