package com.santimattius.kmp.compose.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santimattius.kmp.compose.core.data.PictureRepository
import com.santimattius.kmp.compose.core.domain.Picture
import com.santimattius.resilient.composition.ResilientScope
import com.santimattius.resilient.composition.resilient
import com.santimattius.resilient.retry.ExponentialBackoff
import com.santimattius.kvs.Kvs
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
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
    val isDarkMode: Boolean = false,
    val data: Picture? = null,
)

class HomeViewModel(
    private val repository: PictureRepository,
    private val kvs: Kvs
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.onStart {
        initView()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
        _state.update { it.copy(isLoading = false, hasError = true) }
    }
    private var dataJob: Job? = null
    private var settingJob: Job? = null
    private val resilientScope = ResilientScope(Dispatchers.IO)
    private val resilientPolicy = resilient(resilientScope){
        retry {
            maxAttempts = 3
            backoffStrategy = ExponentialBackoff(initialDelay = 100.milliseconds)
        }
    }

    private fun initView() {
        _state.update { it.copy(isLoading = true, hasError = false) }
        settingJob?.cancel()
        settingJob = viewModelScope.launch(exceptionHandler) {
            val isDarkMode = kvs.getBoolean("is-dark-mode", false)
            _state.update {
                it.copy(isDarkMode = isDarkMode)
            }
        }
        fetchPicture()
    }

    private fun fetchPicture() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch(exceptionHandler) {
            val result = resilientPolicy.execute { repository.random() }
            result.onSuccess { picture ->
                _state.update { it.copy(isLoading = false, data = picture) }
            }.onFailure {
                _state.update { it.copy(isLoading = false, hasError = true) }
            }
        }


    fun randomImage() {
        _state.update { it.copy(isLoading = true, hasError = false) }
        fetchPicture()

    }

    fun darkMode() {
        viewModelScope.launch(exceptionHandler) {
            kvs.edit()
                .putBoolean("is-dark-mode", !state.value.isDarkMode)
                .commit()

            val isDarkMode = kvs.getBoolean(
                key = "is-dark-mode",
                defValue = false
            )
            _state.update {
                it.copy(isDarkMode = isDarkMode)
            }
        }
    }
}}