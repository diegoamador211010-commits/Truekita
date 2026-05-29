
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Plugin necesario para Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}