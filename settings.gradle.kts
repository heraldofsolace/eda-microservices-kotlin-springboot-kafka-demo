rootProject.name = "messaging-microservice"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("order-service")
include("inventory-service")
include("notification-service")
