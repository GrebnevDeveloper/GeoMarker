// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint.analisis) apply true
    alias(libs.plugins.detekt.analisis) apply true
    alias(libs.plugins.android.library) apply false
}
subprojects {
    plugins.apply("org.jlleitschuh.gradle.ktlint")
    plugins.apply("io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        enableExperimentalRules.set(true)
        verbose.set(true)
        android.set(true)
        filter {
            exclude("**/generated/**")
        }
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
        source.setFrom(files("src/main/java", "src/test/java"))
        buildUponDefaultConfig = true
        parallel = true
    }
}
dependencies {
    ktlint(libs.ktlint.rules)
}