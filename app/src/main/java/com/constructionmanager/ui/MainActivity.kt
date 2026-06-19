package com.constructionmanager.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.constructionmanager.data.settings.SettingsStore
import com.constructionmanager.ui.navigation.ConstructionManagerNavigation
import com.constructionmanager.ui.screens.auth.LoginViewModel
import com.constructionmanager.ui.theme.ConstructionManagerTheme
import com.constructionmanager.update.PlayUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playUpdateManager: PlayUpdateManager

    @Inject
    lateinit var settingsStore: SettingsStore

    // Drives the "update downloaded — restart to install" prompt for the Play flexible flow.
    private val updateDownloaded = mutableStateOf(false)

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            updateDownloaded.value = true
        }
    }

    private lateinit var updateLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updateLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { /* Outcome is reflected via the install-state listener / Play UI. */ }

        playUpdateManager.manager.registerListener(installListener)
        maybeStartPlayUpdate()

        setContent {
            val darkTheme by settingsStore.darkTheme.collectAsState()
            ConstructionManagerTheme(darkTheme = darkTheme) {
                MainContent(
                    updateDownloaded = updateDownloaded.value,
                    onCompleteUpdate = { playUpdateManager.manager.completeUpdate() }
                )
            }
        }
    }

    /** Offers a Play flexible update when the app was installed from Google Play. No-op otherwise. */
    private fun maybeStartPlayUpdate() {
        lifecycleScope.launch {
            val info = playUpdateManager.checkForUpdate() ?: return@launch
            runCatching {
                playUpdateManager.manager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }.onFailure { Log.w(TAG, "Play update flow could not start", it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playUpdateManager.manager.unregisterListener(installListener)
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}

@Composable
private fun MainContent(
    updateDownloaded: Boolean,
    onCompleteUpdate: () -> Unit
) {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val loginUiState by loginViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateDownloaded) {
        if (updateDownloaded) {
            val result = snackbarHostState.showSnackbar(
                message = "Update downloaded.",
                actionLabel = "Restart"
            )
            if (result == SnackbarResult.ActionPerformed) onCompleteUpdate()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ConstructionManagerNavigation(
            modifier = Modifier.padding(innerPadding),
            isAuthenticated = loginUiState.isAuthenticated
        )
    }
}
