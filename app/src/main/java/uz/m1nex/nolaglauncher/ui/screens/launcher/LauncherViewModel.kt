package uz.m1nex.nolaglauncher.ui.screens.launcher

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.m1nex.nolaglauncher.domain.model.HomeApp
import uz.m1nex.nolaglauncher.domain.repository.AppsRepository
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val repository: AppsRepository
) : ViewModel() {

    private val firstSyncDone = MutableStateFlow(false)

    val state: StateFlow<LauncherContract.State> =
        combine(repository.observeHome(), firstSyncDone) { apps, synced ->
            if (apps.isEmpty() && !synced) {
                LauncherContract.State.Loading
            } else {
                LauncherContract.State.Ready(apps.intoPages())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LauncherContract.State.Loading
        )

    init {
        viewModelScope.launch {
            repository.syncApps()
            firstSyncDone.value = true
        }
    }

    fun onIntent(intent: LauncherContract.Intent) {
        when (intent) {
            is LauncherContract.Intent.LaunchApp -> repository.launchApp(intent.componentName)
        }
    }

    suspend fun loadIcon(app: HomeApp): ImageBitmap? = repository.loadIcon(app)?.asImageBitmap()

    private fun List<HomeApp>.intoPages(): List<List<HomeApp>> =
        groupBy { it.page }.toSortedMap().values.toList()
}
