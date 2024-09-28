pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven ( url = "https://jitpack.io" )
        maven ( url = "https://oss.sonatype.org/content/repositories/snapshots" )
        maven ( url = "https://jcenter.bintray.com")
        maven { url = uri("http://oss.jfrog.org/artifactory/oss-snapshot-local/")
            isAllowInsecureProtocol = true
             }
    }
}

rootProject.name = "IAN"
include(":app")
 