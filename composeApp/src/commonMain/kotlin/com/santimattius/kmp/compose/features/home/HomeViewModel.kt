package com.santimattius.kmp.compose.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santimattius.kmp.compose.core.data.PictureRepository
import com.santimattius.kmp.compose.core.domain.Picture
import com.santimattius.resilient.composition.ResilientScope
import com.santimattius.resilient.composition.resilient
import com.santimattius.resilient.retry.ExponentialBackoff
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


data class HomeUiState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val data: Picture? = null,
)

class HomeViewModel(
    private val repository: PictureRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.onStart {
        randomImage()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        _state.update { it.copy(isLoading = false, hasError = true) }
    }

    private val resilientScope = ResilientScope(Dispatchers.IO)
    private val resilientPolicy = resilient(resilientScope){
        retry {
            maxAttempts = 3
            backoffStrategy = ExponentialBackoff(initialDelay = 100.milliseconds)
        }
    }

    fun randomImage() {
        _state.update { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch(exceptionHandler) {
            val result = resilientPolicy.execute { repository.random() }
            result.onSuccess { picture ->
                _state.update { it.copy(isLoading = false, data = picture) }
            }.onFailure {
                _state.update { it.copy(isLoading = false, hasError = true) }
            }
        }
    }
}