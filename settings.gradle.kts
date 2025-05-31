rootProject.name = "MagicMail"
include(":common")
include(":api")
include(":core")

pluginManagement {
    repositories {
        gradlePluginPortal()
        /* PaperMC */
        maven("https://repo.papermc.io/repository/maven-public/")
        /* PlaceholderApi */
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

