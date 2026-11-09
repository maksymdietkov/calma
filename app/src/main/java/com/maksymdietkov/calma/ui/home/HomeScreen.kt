package com.maksymdietkov.calma.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maksymdietkov.calma.data.model.CheckEntry
import com.maksymdietkov.calma.data.model.CheckSession
import com.maksymdietkov.calma.ui.format.formatDayTime
import com.maksymdietkov.calma.ui.format.formatTime

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onStartNew = viewModel::startNew,
        onContinueStale = viewModel::continueStale,
        onToggle = viewModel::toggle,
        onComplete = viewModel::complete,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onStartNew: () -> Unit,
    onContinueStale: () -> Unit,
    onToggle: (Long) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Перед выходом",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.padding(top = 8.dp))

        when (state) {
            HomeUiState.Loading -> Unit
            is HomeUiState.Idle -> IdleContent(state.last, onStartNew)
            is HomeUiState.InProgress -> InProgressContent(
                session = state.session,
                isStale = state.isStale,
                onContinueStale = onContinueStale,
                onStartNew = onStartNew,
                onToggle = onToggle,
                onComplete = onComplete,
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.IdleContent(
    last: CheckSession?,
    onStartNew: () -> Unit,
) {
    if (last?.completedAt != null) {
        Text(
            text = "Проверка завершена ${formatDayTime(last.completedAt)}",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Отмечено ${last.checkedCount} из ${last.totalCount}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        last.editedAt?.let {
            Text(
                text = "Изменено ${formatDayTime(it)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.padding(top = 8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(last.entries, key = { it.id }) { entry ->
                EntryRow(entry = entry, onToggle = null)
            }
        }
    } else {
        Text(
            text = "Проверок ещё не было",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }

    PrimaryButton(text = "Новая проверка", onClick = onStartNew)
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.InProgressContent(
    session: CheckSession,
    isStale: Boolean,
    onContinueStale: () -> Unit,
    onStartNew: () -> Unit,
    onToggle: (Long) -> Unit,
    onComplete: () -> Unit,
) {
    Text(
        text = "Начата ${formatDayTime(session.startedAt)}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (isStale) {
        Spacer(Modifier.padding(top = 8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Незавершённая проверка ${formatDayTime(session.startedAt)}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onStartNew) { Text("Начать новую") }
                    TextButton(onClick = onContinueStale) { Text("Продолжить") }
                }
            }
        }
    }

    Spacer(Modifier.padding(top = 8.dp))
    LazyColumn(modifier = Modifier.weight(1f)) {
        items(session.entries, key = { it.id }) { entry ->
            EntryRow(entry = entry, onToggle = { onToggle(entry.id) })
        }
    }

    PrimaryButton(text = "Готово, выхожу", onClick = onComplete)
}

/** onToggle == null — строка только для чтения (завершённая проверка). */
@Composable
private fun EntryRow(
    entry: CheckEntry,
    onToggle: (() -> Unit)?,
) {
    val haptic = LocalHapticFeedback.current

    val interaction = if (onToggle != null) {
        Modifier.toggleable(
            value = entry.isChecked,
            role = Role.Checkbox,
            onValueChange = { nowChecked ->
                haptic.performHapticFeedback(
                    if (nowChecked) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                )
                onToggle()
            },
        )
    } else {
        Modifier.semantics(mergeDescendants = true) {}
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(interaction)
            .padding(vertical = 8.dp),
    ) {
        Checkbox(checked = entry.isChecked, onCheckedChange = null)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = entry.title, style = MaterialTheme.typography.bodyLarge)
            entry.checkedAt?.let {
                Text(
                    text = "Отмечено в ${formatTime(it)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}