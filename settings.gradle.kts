pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google(); mavenCentral()
        maven("https://raw.githubusercontent.com/guardianproject/gpmaven/master") { content { includeGroup("info.guardianproject") } }
    }
}
rootProject.name = "BuscadorLocal"
include(":app", ":lib")
project(":lib").projectDir = file("vendor/LFM3Local/android/lib")
