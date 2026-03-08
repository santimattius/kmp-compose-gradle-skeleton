package com.santimattius.kmp.compose.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.github.santimattius.persistent.cache.installPersistentCache
import io.github.santimattius.persistent.cache.CacheConfig

internal fun ktorHttpClient(baseUrl: String) = HttpClient {

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }

    installPersistentCache(
        config = CacheConfig(
            enabled = true,
            cacheDirectory = "http_cache",
            maxCacheSize = 10L * 1024 * 1024, // 10 MB
            cacheTtl = 60 * 60 * 1000,       // 1 hour
        )
    )

    defaultRequest {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
}