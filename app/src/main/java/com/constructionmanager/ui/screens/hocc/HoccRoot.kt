package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/** The six places in the app. Home is the way into a room; the room is where the work happens. */
enum class HoccTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Site("Site", Icons.Default.Place),
    Jobs("Jobs", Icons.Default.Work),
    Schedule("Schedule", Icons.Default.CalendarMonth),
    Caroline("Caroline", Icons.Default.Chat),
    Office("Office", Icons.Default.Settings)
}

/** The room key survives rotation and the fold (the Fold7 changes configuration when it opens). */
private val RoomKeySaver = Saver<RoomKey?, List<String>>(
    save = { k -> if (k == null) emptyList() else listOf(k.kind, k.value, k.projectIds.joinToString(","), k.startId?.toString() ?: "") },
    restore = { l -> if (l.size < 4) null else RoomKey(l[0], l[1], l[2].split(',').mapNotNull { it.toIntOrNull() }, l[3].toIntOrNull()) }
)

/** The app proper, once the phone is signed in: the tabs over the one record, and the room over the tabs. */
@Composable
fun HoccRoot() {
    val vm: HoccViewModel = hiltViewModel()
    val roomVm: RoomViewModel = hiltViewModel()
    var tabName by rememberSaveable { mutableStateOf(HoccTab.Home.name) }
    val tab = runCatching { HoccTab.valueOf(tabName) }.getOrDefault(HoccTab.Home)
    var roomKey by rememberSaveable(stateSaver = RoomKeySaver) { mutableStateOf<RoomKey?>(null) }
    val jobs by vm.jobs.collectAsState()

    // Home IS the board (Tyler, Nov 2 2026). Opening a job from anywhere lands on Home with that room up.
    val goRoom: (RoomKey) -> Unit = { roomKey = it; tabName = HoccTab.Home.name }

    MaterialTheme(colorScheme = Board.colors, typography = Board.typography) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                HoccTab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tabName = t.name },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label, maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                HoccTab.Home -> HomeScreen(vm, roomVm, roomKey, onOpenRoom = goRoom)
                HoccTab.Site -> SiteScreen(vm, onOpenJob = { id -> goRoom(oneJob(id, jobs.data?.objects().orEmpty())) })
                // the prototype's Leads and jobs list (Tyler, Nov 7 2026: "it shows it's basically a list of leads, not my jobs room"):
                // his jobs by where each stands, leads folded under them; a tap opens that job's room
                HoccTab.Jobs -> JobsTab(vm, roomKey?.startId ?: roomKey?.projectIds?.firstOrNull()) { id -> goRoom(oneJob(id, jobs.data?.objects().orEmpty())) }
                HoccTab.Schedule -> ScheduleScreen(vm)
                HoccTab.Caroline -> CarolineScreen(vm)
                HoccTab.Office -> OfficeScreen(vm)
            }
        }
    }
    }
}


/** The Jobs tab: the job room's Leads and jobs list, full screen. */
@Composable
private fun JobsTab(vm: HoccViewModel, currentId: Int?, onOpen: (Int) -> Unit) {
    val jobs by vm.jobs.collectAsState()
    androidx.compose.runtime.LaunchedEffect(Unit) { vm.loadJobs() }
    var q by rememberSaveable { mutableStateOf("") }
    var leads by rememberSaveable { mutableStateOf(false) }
    val all = jobs.data?.objects().orEmpty()
    Column(Modifier.fillMaxSize().background(Board.ground)) {
        if (jobs.loading && all.isEmpty()) LoadingLine("Reading your jobs…")
        jobs.error?.let { ErrorLine(it) { vm.loadJobs() } }
        FaceJobsList(RoomFaces.list(all, java.time.LocalDate.now()), currentId, q, { q = it }, onOpen, null, leads, { leads = it }, Modifier.fillMaxSize())
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
