package com.maksymdietkov.calma.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.maksymdietkov.calma.CalmaApp
import com.maksymdietkov.calma.data.CheckRepository
import com.maksymdietkov.calma.data.model.CheckSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration

sealed interface HomeUiState {
    data object Loading : HomeUiState

    /** Активной проверки нет. last — последняя завершённая (или null). */
    data class Idle(val last: CheckSession?) : HomeUiState

    /** Идёт проверка. isStale — висит дольше 2 часов, спрашиваем пользователя. */
    data class InProgress(val session: CheckSession, val isStale: Boolean) : HomeUiState
}

class HomeViewModel(
    private val repository: CheckRepository,
    private val clock: Clock,
) : ViewModel() {

    private val staleDismissed = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeActive(),
        repository.observeLastCompleted(),
        staleDismissed,
    ) { active, last, dismissed ->
        if (active != null) {
            val age = Duration.between(active.startedAt, clock.instant())
            HomeUiState.InProgress(
                session = active,
                isStale = !dismissed && age > STALE_AFTER,
            )
        } else {
            HomeUiState.Idle(last)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    fun startNew() {
        staleDismissed.value = false
        viewModelScope.launch { repository.startNewSession() }
    }

    fun continueStale() {
        staleDismissed.value = true
    }

    fun toggle(entryId: Long) {
        val session = (uiState.value as? HomeUiState.InProgress)?.session ?: return
        viewModelScope.launch { repository.toggleEntry(session, entryId) }
    }

    fun complete() {
        val session = (uiState.value as? HomeUiState.InProgress)?.session ?: return
        viewModelScope.launch { repository.completeSession(session.id) }
    }

    companion object {
        private val STALE_AFTER: Duration = Duration.ofHours(2)

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CalmaApp
                HomeViewModel(
                    repository = app.container.checkRepository,
                    clock = app.container.clock,
                )
            }
        }
    }
}