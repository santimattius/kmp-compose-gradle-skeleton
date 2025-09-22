package com.santimattius.kmp.compose.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santimattius.kmp.compose.core.data.PictureRepository
import com.santimattius.kmp.compose.core.domain.Picture
import com.santimattius.kvs.Kvs
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


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
            val picture = repository.random().getOrNull()
            _state.update {
                it.copy(isLoading = false, data = picture, hasError = picture == null)
            }
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
}