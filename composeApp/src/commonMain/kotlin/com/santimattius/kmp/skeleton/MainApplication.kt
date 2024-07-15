package com.santimattius.kmp.skeleton

import androidx.compose.runtime.Composable
import com.santimattius.kmp.skeleton.di.applicationModules
import com.santimattius.kmp.skeleton.navigation.Navigation
import org.koin.compose.KoinApplication

@Composable
fun MainApplication() {
    KoinApplication(application = {
        modules(applicationModules())
    }) {
        Navigation()
    }
}
