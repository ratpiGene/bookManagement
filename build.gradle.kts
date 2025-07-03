import info.solidsoft.gradle.pitest.PitestPluginExtension
import io.gitlab.arturbosch.detekt.Detekt

plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
	id("java")
	id("info.solidsoft.pitest") version "1.19.0-rc.1"
	id("io.gitlab.arturbosch.detekt") version ("1.23.8")
}

group = "com.group"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

jacoco {
	toolVersion = "0.8.13"
}

testing {
	suites {
		val testIntegration by registering(JvmTestSuite::class) {
			sources {
				kotlin {
					setSrcDirs(listOf("src/testIntegration/kotlin"))
				}
				compileClasspath += sourceSets.main.get().output
				runtimeClasspath += sourceSets.main.get().output
			}
		}
	}
}


val testIntegrationImplementation: Configuration by configurations.getting {
	extendsFrom(configurations.implementation.get())
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.liquibase:liquibase-core")
	implementation("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")
	testImplementation("io.mockk:mockk:1.14.4")
	testImplementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
	testImplementation("io.kotest.extensions:kotest-extensions-pitest:1.2.0")

	testIntegrationImplementation("io.mockk:mockk:1.13.8")
	testIntegrationImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testIntegrationImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testIntegrationImplementation("com.ninja-squad:springmockk:4.0.2")
	testIntegrationImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testIntegrationImplementation("org.testcontainers:postgresql:1.19.1")
	testIntegrationImplementation("org.testcontainers:jdbc-test:1.12.0")
	testIntegrationImplementation("org.testcontainers:testcontainers:1.19.1")
	testIntegrationImplementation("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
	testIntegrationImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
	testIntegrationImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
	testIntegrationImplementation("io.kotest.extensions:kotest-extensions-pitest:1.2.0")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<JacocoReport>("jacocoFullReport") {
	executionData(tasks.named("test").get(), tasks.named("testIntegration").get())
	sourceSets(sourceSets["main"])

	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

configure<PitestPluginExtension> {
	targetClasses.set(listOf("com.group.library.*"))
}

pitest {
	targetClasses.add("com.group.library.*")
	junit5PluginVersion.set("1.2.0")
	avoidCallsTo.set(setOf("kotlin.jvm.internal"))
	mutators.set(setOf("STRONGER"))
	threads.set(4)
	jvmArgs.add("-Xmx1024m")
	testSourceSets.addAll(sourceSets["test"])
	mainSourceSets.addAll(sourceSets["main"])
	outputFormats.addAll("XML", "HTML")
	excludedClasses.add("**LibraryApplication")
}

detekt {
	toolVersion = "1.23.8"
	config.setFrom("$projectDir/config/detekt.yml")
	buildUponDefaultConfig = true
	allRules = false
	ignoreFailures = true
	basePath = rootProject.projectDir.absolutePath
}

tasks.withType<Detekt>().configureEach {
	basePath = rootProject.projectDir.absolutePath
	reports {
		xml.required.set(true)
		html.required.set(true)
		txt.required.set(true)
		sarif.required.set(true)
		md.required.set(true)
	}
}
