package com.santimattius.kmp.compose

import androidx.compose.runtime.Composable
import com.santimattius.kmp.compose.di.applicationModules
import com.santimattius.kmp.compose.navigation.Navigation
import org.koin.compose.KoinApplication
import org.koin.dsl.koinApplication

fun koinConfiguration() = koinApplication {
    modules(applicationModules())
}

@Composable
fun MainApplication() {
    KoinApplication(::koinConfiguration) {
        Navigation()
    }
}
