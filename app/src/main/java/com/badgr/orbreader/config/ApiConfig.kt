package com.badgr.orbreader.config

import com.badgr.orbreader.BuildConfig

/**
 * Single source of truth for backend configuration.
 *
 * To point at a different server, change BACKEND_BASE_URL in app/build.gradle.kts
 * under defaultConfig / a buildType override – no code changes needed.
 */
object ApiConfig {
    /** Base URL read from the build-time constant injected by Gradle. */
    val BASE_URL: String = BuildConfig.BACKEND_BASE_URL

    /** POST /convert  – accepts multipart file, returns ConversionResponse JSON. */
    const val CONVERT_ENDPOINT = "convert"
}
