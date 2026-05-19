package com.santimattius.kmp.compose

import androidx.compose.runtime.Composable
import com.santimattius.kmp.compose.di.applicationModules
import com.santimattius.kmp.compose.navigation.Navigation
import org.koin.compose.KoinApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
@Composable
fun MainApplication() {
    KoinApplication(
        configuration = koinConfiguration { modules(applicationModules()) },
        content = {
            Navigation()
        }
    )
}
