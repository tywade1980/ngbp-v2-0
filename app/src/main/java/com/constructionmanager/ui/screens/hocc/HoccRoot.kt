package com.constructionmanager.ui.screens.hocc

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/** The six places in the app - one navigation system, the app's own, nothing mirrored from any web UI. */
enum class HoccTab(val label: String, val icon: ImageVector) {
    Hub("Hub", Icons.Default.Home),
    Site("Site", Icons.Default.Place),
    Jobs("Jobs", Icons.Default.Work),
    Schedule("Schedule", Icons.Default.CalendarMonth),
    Caroline("Caroline", Icons.Default.Chat),
    Office("Office", Icons.Default.Settings)
}

/** The app proper, once the phone is signed in: the six tabs over the one record. */
@Composable
fun HoccRoot() {
    val vm: HoccViewModel = hiltViewModel()
    var tabName by rememberSaveable { mutableStateOf(HoccTab.Hub.name) }
    val tab = runCatching { HoccTab.valueOf(tabName) }.getOrDefault(HoccTab.Hub)
    var openJobId by rememberSaveable { mutableStateOf<Int?>(null) }

    BackHandler(enabled = tab == HoccTab.Jobs && openJobId != null) { openJobId = null }

    Scaffold(
        bottomBar = {
            NavigationBar {
                HoccTab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = {
                            tabName = t.name
                            if (t != HoccTab.Jobs) openJobId = null
                        },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                HoccTab.Hub -> HubScreen(
                    vm,
                    onGoJobs = { tabName = HoccTab.Jobs.name },
                    onGoSite = { tabName = HoccTab.Site.name }
                )
                HoccTab.Site -> SiteScreen(vm, onOpenJob = { openJobId = it; tabName = HoccTab.Jobs.name })
                HoccTab.Jobs -> JobsScreen(
                    vm,
                    openJobId = openJobId,
                    onOpenJob = { openJobId = it },
                    onCloseJob = { openJobId = null }
                )
                HoccTab.Schedule -> ScheduleScreen(vm)
                HoccTab.Caroline -> CarolineScreen(vm)
                HoccTab.Office -> OfficeScreen(vm)
            }
        }
    }
}

/** A scrolling screen with its name and a way to read the record again - the shape every tab shares. */
@Composable
internal fun ScreenColumn(title: String, onRefresh: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (onRefresh != null) {
                IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
            }
        }
        content()
    }
}
